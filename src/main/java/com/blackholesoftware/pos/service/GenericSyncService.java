package com.blackholesoftware.pos.service;

import com.blackholesoftware.pos.entity.BaseSyncEntity;
import com.blackholesoftware.pos.repository.BaseSyncRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GenericSyncService {

    @Value("${pos.cloud.api.base-url:https://your-cloud-api.com/api}")
    private String cloudApiUrl;

    @Value("${pos.terminal.id:POS-TERM-01}")
    private String terminalId;

    @Value("${pos.terminal.secret-key:SECRET_KEY}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 🔴 මෙතන BaseSyncRepository<T, ?> ලෙස Type Parameter එක 2ක් වන සේ Update කරන්න
    @Transactional
    public <T extends BaseSyncEntity> void syncTableToCloud(BaseSyncRepository<T, ?> repository, String tableName) {
        List<T> unsyncedRecords = repository.findByIsSyncedFalse();

        if (unsyncedRecords.isEmpty()) return;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Terminal-ID", terminalId);
            headers.set("X-Terminal-Secret", secretKey);

            String syncEndpoint = cloudApiUrl + "/sync/" + tableName;
            HttpEntity<List<T>> request = new HttpEntity<>(unsyncedRecords, headers);

            restTemplate.postForEntity(syncEndpoint, request, String.class);

            for (T entity : unsyncedRecords) {
                entity.setIsSynced(true);
                entity.setUpdatedAt(LocalDateTime.now());
            }
            repository.saveAll(unsyncedRecords);

            System.out.println("[Sync Engine] Synced table: " + tableName + " (" + unsyncedRecords.size() + " items)");

        } catch (Exception e) {
            System.err.println("[Sync Engine Error] Table: " + tableName + " - " + e.getMessage());
        }
    }
}