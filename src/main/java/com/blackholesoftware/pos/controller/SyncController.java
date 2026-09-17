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
import java.time.OffsetDateTime;
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

    private void saveOrUpdateRecord(String tableName, Map<String, Object> row) throws Exception {
        Object id = row.get("id");
        if (id == null) {
            throw new IllegalArgumentException("Record missing 'id' field for table: " + tableName);
        }

        String checkSql = "SELECT COUNT(*) FROM " + tableName + " WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, id);

        if (count != null && count > 0) {
            StringBuilder updateSql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("id")) {
                    String columnName = camelToSnakeCase(entry.getKey());

                    // Column name එක අනුව Postgres Type Cast එක dynamic ලෙස එකතු කිරීම
                    if (isTimestampColumn(columnName)) {
                        updateSql.append(columnName).append(" = ?::timestamp, ");
                    } else if (isBooleanColumn(columnName)) {
                        updateSql.append(columnName).append(" = ?::boolean, ");
                    } else {
                        updateSql.append(columnName).append(" = ?, ");
                    }

                    params.add(formatValue(entry.getValue()));
                }
            }
            updateSql.setLength(updateSql.length() - 2);
            updateSql.append(" WHERE id = ?");
            params.add(id);

            jdbcTemplate.update(updateSql.toString(), params.toArray());
        } else {
            StringBuilder columns = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            List<Object> params = new ArrayList<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String columnName = camelToSnakeCase(entry.getKey());
                columns.append(columnName).append(", ");

                // Column name එක අනුව Postgres Type Cast එක dynamic ලෙස එකතු කිරීම
                if (isTimestampColumn(columnName)) {
                    placeholders.append("?::timestamp, ");
                } else if (isBooleanColumn(columnName)) {
                    placeholders.append("?::boolean, ");
                } else {
                    placeholders.append("?, ");
                }

                params.add(formatValue(entry.getValue()));
            }

            columns.setLength(columns.length() - 2);
            placeholders.setLength(placeholders.length() - 2);

            String insertSql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
            jdbcTemplate.update(insertSql, params.toArray());
        }
    }

    private boolean isTimestampColumn(String columnName) {
        return columnName.endsWith("_at") || columnName.contains("date") || columnName.contains("time");
    }

    private boolean isBooleanColumn(String columnName) {
        return columnName.startsWith("is_") || columnName.startsWith("has_") || columnName.equals("active");
    }

    private String camelToSnakeCase(String str) {
        if (str == null) return "";
        return str.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    private Object formatValue(Object value) throws Exception {
        if (value == null) {
            return null;
        }

        if (value instanceof Map || value instanceof List) {
            return objectMapper.writeValueAsString(value);
        }

        return value;
    }
}