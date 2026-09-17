package com.blackholesoftware.pos.service;

import com.blackholesoftware.pos.entity.BaseSyncEntity;
import com.blackholesoftware.pos.repository.BaseSyncRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GenericSyncService {

    private static final Logger logger = LoggerFactory.getLogger(GenericSyncService.class);

    @Value("${pos.cloud.api.base-url:https://pos-cloud-backend-pv4k.onrender.com/api}")
    private String cloudApiUrl;

    @Value("${pos.terminal.id:POS-TERM-01}")
    private String terminalId;

    @Value("${pos.terminal.secret-key:SECRET_KEY}")
    private String secretKey;

    private final RestTemplate restTemplate;

    public GenericSyncService() {
        // Configure Jackson to serialize LocalDateTime as ISO Strings instead of JSON Arrays
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter(objectMapper));
    }

    public <T extends BaseSyncEntity> void syncTableToCloud(BaseSyncRepository<T, ?> repository, String tableName) {

        List<T> unsyncedRecords = repository.findByIsSyncedFalse();

        if (unsyncedRecords.isEmpty()) {
            logger.debug("[SYNC CHECK] Table '{}': No unsynced records found.", tableName);
            return;
        }

        logger.info("[SYNC START] Table '{}': Found {} unsynced record(s) to push.", tableName, unsyncedRecords.size());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Terminal-ID", terminalId);
            headers.set("X-Terminal-Secret", secretKey);

            String syncEndpoint = cloudApiUrl + "/sync/" + tableName;
            HttpEntity<List<T>> request = new HttpEntity<>(unsyncedRecords, headers);

            logger.info("[SYNC REQUEST] Endpoint: {} | Terminal-ID: {}", syncEndpoint, terminalId);

            ResponseEntity<String> response = restTemplate.postForEntity(syncEndpoint, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                for (T entity : unsyncedRecords) {
                    entity.setIsSynced(true);
                    entity.setUpdatedAt(LocalDateTime.now());
                }
                repository.saveAll(unsyncedRecords);
                logger.info("[SYNC SUCCESS] Table '{}': Successfully synced {} items to Cloud.", tableName, unsyncedRecords.size());
            } else {
                logger.warn("[SYNC FAILED] Table '{}': HTTP Status: {}", tableName, response.getStatusCode());
            }

        } catch (HttpStatusCodeException e) {
            logger.error("[SYNC HTTP ERROR] Table '{}' | Status: {} | Body: {}", tableName, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("[SYNC EXCEPTION] Table '{}' failed: {}", tableName, e.getMessage(), e);
        }
    }
}