package com.blackholesoftware.pos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate; // Cloud DB එකට Dynamic Injection එකක් කරන්න ලේසිම ක්‍රමය

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/{tableName}")
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
                // Cloud DB එකේ Record එක දැනටමත් තියෙනවා නම් UPDATE, නැත්නම් INSERT කරන්න logic එක
                saveOrUpdateRecord(tableName, row);
            }

            logger.info("[CLOUD SYNC SUCCESS] Table '{}': {} items saved to Cloud DB.", tableName, dataList.size());
            return ResponseEntity.ok(Map.of("message", tableName + " synced successfully"));

        } catch (Exception e) {
            logger.error("[CLOUD SYNC ERROR] Failed to save data for table '{}': {}", tableName, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private void saveOrUpdateRecord(String tableName, Map<String, Object> row) throws Exception {
        // ID එක base කරගෙන Record එකක් Cloud DB එකේ තියෙනවද බලනවා
        Object id = row.get("id");
        if (id == null) {
            throw new IllegalArgumentException("Record missing 'id' field for table: " + tableName);
        }

        // DB එක PostgreSql / MySQL වුණාට SQL Syntax handle වෙනවා
        String checkSql = "SELECT COUNT(*) FROM " + tableName + " WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, id);

        if (count != null && count > 0) {
            // UPDATE Logic
            StringBuilder updateSql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
            List<Object> params = new java.util.ArrayList<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase("id")) {
                    updateSql.append(entry.getKey()).append(" = ?, ");
                    params.add(formatValue(entry.getValue()));
                }
            }
            // අන්තිම කොමාව කපනවා
            updateSql.setLength(updateSql.length() - 2);
            updateSql.append(" WHERE id = ?");
            params.add(id);

            jdbcTemplate.update(updateSql.toString(), params.toArray());
        } else {
            // INSERT Logic
            StringBuilder columns = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            List<Object> params = new java.util.ArrayList<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                columns.append(entry.getKey()).append(", ");
                placeholders.append("?, ");
                params.add(formatValue(entry.getValue()));
            }

            columns.setLength(columns.length() - 2);
            placeholders.setLength(placeholders.length() - 2);

            String insertSql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
            jdbcTemplate.update(insertSql, params.toArray());
        }
    }

    private Object formatValue(Object value) throws Exception {
        // Complex Objects / Nested Lists එහෙම JSON string එකක් විදියට Convert කරගන්නවා
        if (value instanceof Map || value instanceof List) {
            return objectMapper.writeValueAsString(value);
        }
        return value;
    }
}