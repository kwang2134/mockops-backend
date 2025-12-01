package com.mockops.domain.mock.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

/**
 * Mock API 대량 생성 이벤트
 * 비동기 Worker에게 작업을 위임하기 위한 이벤트
 */
@Getter
public class BulkCreationEvent extends ApplicationEvent {

    private final Long serverId;
    private final MultipartFile file;
    private final Long jobId;

    public BulkCreationEvent(Object source, Long serverId, MultipartFile file, Long jobId) {
        super(source);
        this.serverId = serverId;
        this.file = file;
        this.jobId = jobId;
    }
}