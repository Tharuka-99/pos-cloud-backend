package com.blackholesoftware.pos.controller;

import com.blackholesoftware.pos.dto.ApiResponse;
import com.blackholesoftware.pos.entity.CashSession;
import com.blackholesoftware.pos.repository.CashSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cash-sessions")
public class CashSessionController {

    @Autowired
    private CashSessionRepository cashSessionRepository;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<CashSession>> getActiveSession() {
        try {
            List<CashSession> activeSessions = cashSessionRepository.findSessionsByStatus("OPEN");
            if (!activeSessions.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Active session found", activeSessions.get(0)));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "No active session", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Server Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<CashSession>> startSession(@RequestBody CashSession sessionRequest) {
        try {
            List<CashSession> activeSessions = cashSessionRepository.findSessionsByStatus("OPEN");
            if (!activeSessions.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Session already active!", null));
            }

            sessionRequest.setStatus("OPEN");
            sessionRequest.setIsSynced(false);
            if (sessionRequest.getOpenedAt() == null) {
                sessionRequest.setOpenedAt(LocalDateTime.now());
            }

            CashSession savedSession = cashSessionRepository.save(sessionRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cash Session Started Successfully!", savedSession));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to start session: " + e.getMessage(), null));
        }
    }

    @PostMapping("/close/{id}")
    public ResponseEntity<ApiResponse<CashSession>> closeSession(@PathVariable String id, @RequestBody CashSession closeData) {
        try {
            Optional<CashSession> optionalSession = cashSessionRepository.findById(id);
            if (optionalSession.isPresent()) {
                CashSession session = optionalSession.get();
                session.setStatus("CLOSED");
                session.setIsSynced(false);

                if (closeData.getClosingActualCash() != null) {
                    session.setClosingActualCash(closeData.getClosingActualCash());
                }
                if (closeData.getNotes() != null) {
                    session.setNotes(closeData.getNotes());
                }
                session.setClosedAt(LocalDateTime.now());

                CashSession updated = cashSessionRepository.save(session);
                return ResponseEntity.ok(new ApiResponse<>(true, "Session Closed Successfully", updated));
            }
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Session not found", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to close session: " + e.getMessage(), null));
        }
    }

    @PostMapping("/sync/{id}")
    public ResponseEntity<ApiResponse<CashSession>> syncSession(@PathVariable String id) {
        Optional<CashSession> optionalSession = cashSessionRepository.findById(id);
        if (optionalSession.isPresent()) {
            CashSession session = optionalSession.get();
            session.setIsSynced(true);
            CashSession updated = cashSessionRepository.save(session);
            return ResponseEntity.ok(new ApiResponse<>(true, "Synced successfully", updated));
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Session not found", null));
    }
}