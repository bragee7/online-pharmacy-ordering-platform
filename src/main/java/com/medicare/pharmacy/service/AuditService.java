package com.medicare.pharmacy.service;

import com.medicare.pharmacy.entity.AuditLog;
import com.medicare.pharmacy.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog log(Long userId, String action, String entityType, Long entityId, String details) {
        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        return auditLogRepository.save(entry);
    }

    @Transactional
    public AuditLog log(String action, String entityType, Long entityId, String details) {
        return log(null, action, entityType, entityId, details);
    }

    public List<AuditLog> recent(int limit) {
        int size = limit <= 0 ? 20 : limit;
        return auditLogRepository
                .findAll(PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "timestamp")))
                .getContent();
    }
}
