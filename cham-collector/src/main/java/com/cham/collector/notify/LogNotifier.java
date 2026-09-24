package com.cham.collector.notify;

import com.cham.collector.domain.CollectSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LogNotifier implements Notifier {

    @Override
    public void sourceFailed(CollectSource source, String error) {
        log.error("[수집 실패] {}({}): {}", source.name(), source.code(), error);
    }

    @Override
    public void jobFinishedWithFailures(long jobId, List<String> failedSources) {
        log.error("[수집 작업 {}] 실패한 기관: {}", jobId, failedSources);
    }
}
