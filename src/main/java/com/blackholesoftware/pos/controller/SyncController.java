package com.blackholesoftware.pos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/{tableName}")
    @Transactional
    public ResponseEntity<?> syncTable(
            @PathVariable String tableName,
            @RequestHeader(value = "X-Terminal-ID", required = false) String terminalId,
            @RequestBody List<Map<String, Object>> dataList) {

        logger.info("[CLOUD SYNC RECEIVED] Table: '{}' | Records: {} | Terminal: {}", tableName, dataList.size(), terminalId);

        if (dataList == null || dataList.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No data to sync"));
        }

        try {
            for (Map<String, Object> row : dataList) {
                saveOrUpdateRecord(tableName, row);
            }

            logger.info("[CLOUD SYNC SUCCESS] Table '{}': {} items saved.", tableName, dataList.size());
            return ResponseEntity.ok(Map.of("message", tableName + " synced successfully"));

        } catch (Exception e) {
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            String detailedError = String.format("Message: %s | Root Cause: %s", e.getMessage(), rootCause.getMessage());
            logger.error("[CLOUD SYNC ERROR] Table '{}' failed -> {}", tableName, detailedError, e);

            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "SQL Execution Failed",
                    "details", detailedError,
                    "table", tableName
            ));
        }
    }

    private void saveOrUpdateRecord(String rawTableName, Map<String, Object> row) throws Exception {
        Object id = row.get("id");
        if (id == null) {
            throw new IllegalArgumentException("Record missing 'id' field for table: " + rawTableName);
        }

        String tableName = resolveRealTableName(rawTableName);

        Map<String, Object> recordData = new LinkedHashMap<>(row);
        Map<String, Object> processedData = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : recordData.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();

            if (val instanceof List<?>) {
                continue;
            }

            if (val instanceof Map<?, ?> nestedMap) {
                if (nestedMap.containsKey("id")) {
                    String mappedCol = mapColumnName(key);
                    if (!mappedCol.endsWith("_id")) {
                        mappedCol = mappedCol + "_id";
                    }
                    processedData.put(mappedCol, nestedMap.get("id"));
                }
            } else {
                processedData.put(mapColumnName(key), val);
            }
        }

        if ("cash_sessions".equalsIgnoreCase(tableName)) {
            processedData.remove("username");
            processedData.remove("user_name");
        }

        if ("sales".equalsIgnoreCase(tableName)) {
            if (processedData.containsKey("cashier_id")) {
                Object cashierVal = processedData.remove("cashier_id");
                processedData.put("user_id", cashierVal);
            }
        }

        if ("app_users".equalsIgnoreCase(tableName)) {
            Object usernameVal = processedData.get("username");
            if (usernameVal != null) {
                processedData.put("username", usernameVal);
                processedData.put("user_name", usernameVal);
            }
        }

        String safeTableName = "\"" + tableName.replace("\"", "") + "\"";

        String checkSql;
        Integer count = 0;

        if ("app_users".equalsIgnoreCase(tableName) && processedData.containsKey("username")) {
            checkSql = "SELECT COUNT(*) FROM " + safeTableName + " WHERE id = ? OR username = ?";
            count = jdbcTemplate.queryForObject(checkSql, Integer.class, id, processedData.get("username"));
        } else {
            checkSql = "SELECT COUNT(*) FROM " + safeTableName + " WHERE id = ?";
            count = jdbcTemplate.queryForObject(checkSql, Integer.class, id);
        }

        if (count != null && count > 0) {
            StringBuilder updateSql = new StringBuilder("UPDATE ").append(safeTableName).append(" SET ");
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : processedData.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("id")) {
                    String columnName = "\"" + entry.getKey() + "\"";

                    if (isTimestampColumn(entry.getKey())) {
                        updateSql.append(columnName).append(" = ?::timestamp, ");
                    } else if (isBooleanColumn(entry.getKey())) {
                        updateSql.append(columnName).append(" = ?::boolean, ");
                    } else {
                        updateSql.append(columnName).append(" = ?, ");
                    }

                    params.add(formatValue(entry.getValue()));
                }
            }
            updateSql.setLength(updateSql.length() - 2);

            if ("app_users".equalsIgnoreCase(tableName) && processedData.containsKey("username")) {
                updateSql.append(" WHERE id = ? OR username = ?");
                params.add(id);
                params.add(processedData.get("username"));
            } else {
                updateSql.append(" WHERE id = ?");
                params.add(id);
            }

            jdbcTemplate.update(updateSql.toString(), params.toArray());
        } else {
            StringBuilder columns = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : processedData.entrySet()) {
                String columnName = "\"" + entry.getKey() + "\"";
                columns.append(columnName).append(", ");

                if (isTimestampColumn(entry.getKey())) {
                    placeholders.append("?::timestamp, ");
                } else if (isBooleanColumn(entry.getKey())) {
                    placeholders.append("?::boolean, ");
                } else {
                    placeholders.append("?, ");
                }

                params.add(formatValue(entry.getValue()));
            }

            columns.setLength(columns.length() - 2);
            placeholders.setLength(placeholders.length() - 2);

            String insertSql = "INSERT INTO " + safeTableName + " (" + columns + ") VALUES (" + placeholders + ")";
            jdbcTemplate.update(insertSql, params.toArray());
        }
    }

    private String resolveRealTableName(String tableName) {
        if (tableName == null) return "";
        return tableName.replace("-", "_").toLowerCase();
    }

    private boolean isTimestampColumn(String columnName) {
        return columnName.endsWith("_at") || columnName.contains("date") || columnName.contains("time");
    }

    private boolean isBooleanColumn(String columnName) {
        return columnName.startsWith("is_") || columnName.startsWith("has_") || columnName.equals("active");
    }

    private String mapColumnName(String fieldName) {
        if (fieldName == null) return "";

        String snakeCase = fieldName.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();

        switch (snakeCase) {
            case "sale":
                return "sale_id";
            case "sales_return":
            case "salesreturn":
                return "sales_return_id";
            case "grn":
                return "grn_id";
            case "purchase_order":
            case "purchaseorder":
                return "purchase_order_id";
            case "customer":
                return "customer_id";
            case "product":
                return "product_id";
            case "batch":
                return "batch_id";
            case "cashier":
                return "cashier_id";
            case "user":
                return "user_id";
            case "category":
                return "category_id";
            case "brand":
                return "brand_id";
            case "unit":
                return "unit_id";
            case "supplier":
                return "supplier_id";
            case "username":
            case "user_name":
                return "username";
            default:
                return snakeCase;
        }
    }

    private Object formatValue(Object value) throws Exception {
        if (value == null) {
            return null;
        }

        if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Number) {
            if (list.size() >= 3) {
                int year = ((Number) list.get(0)).intValue();
                int month = ((Number) list.get(1)).intValue();
                int day = ((Number) list.get(2)).intValue();
                int hour = list.size() > 3 ? ((Number) list.get(3)).intValue() : 0;
                int minute = list.size() > 4 ? ((Number) list.get(4)).intValue() : 0;
                int second = list.size() > 5 ? ((Number) list.get(5)).intValue() : 0;
                int nano = list.size() > 6 ? ((Number) list.get(6)).intValue() : 0;

                LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second, nano);
                return ldt.toString();
            }
        }

        if (value instanceof Map || value instanceof List) {
            return objectMapper.writeValueAsString(value);
        }

        return value;
    }
}