package com.blackholesoftware.pos.service;

import com.blackholesoftware.pos.entity.BaseSyncEntity;
import com.blackholesoftware.pos.repository.BaseSyncRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GenericSyncService {

    @Value("${pos.cloud.api.base-url:https://pos-cloud-backend-pv4k.onrender.com/api}")
    private String cloudApiUrl;

    @Value("${pos.terminal.id:POS-TERM-01}")
    private String terminalId;

    @Value("${pos.terminal.secret-key:SECRET_KEY}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

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

            // Response Status එක Check කරන්න
            ResponseEntity<String> response = restTemplate.postForEntity(syncEndpoint, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                for (T entity : unsyncedRecords) {
                    entity.setIsSynced(true);
                    entity.setUpdatedAt(LocalDateTime.now());
                }
                repository.saveAll(unsyncedRecords);
                System.out.println("[Sync Engine] Synced table: " + tableName + " (" + unsyncedRecords.size() + " items)");
            } else {
                System.err.println("[Sync Engine Failed] HTTP Status: " + response.getStatusCode() + " for table: " + tableName);
            }

        } catch (HttpStatusCodeException e) {
            // Render 502/500 වගේ HTML Error එකක් ආවොත් ලොකු Log එකක් වැටෙන්නේ නැතුව Clean එරර් එක පෙන්වයි
            System.err.println("[Sync Engine Error] Cloud Server Returned: " + e.getStatusCode() + " for table: " + tableName);
        } catch (Exception e) {
            System.err.println("[Sync Engine Error] Table: " + tableName + " - " + e.getMessage());
        }
    }
}