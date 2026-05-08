package com.superasync.service;

import com.superasync.entity.ScheduledJobLogEntity;
import com.superasync.repository.ScheduledJobLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduledJobLogService {

    private final ScheduledJobLogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long scheduledJobId, String level, String message) {
        ScheduledJobLogEntity entry = new ScheduledJobLogEntity();
        entry.setScheduledJobId(scheduledJobId);
        entry.setLevel(level);
        entry.setMessage(message);
        logRepository.save(entry);
    }

    public Page<ScheduledJobLogEntity> listByJobId(Long scheduledJobId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return logRepository.findByScheduledJobId(scheduledJobId, pageRequest);
    }
}
