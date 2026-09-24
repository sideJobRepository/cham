package com.cham.collector.schedule;

import com.cham.collector.config.CollectorProperties;
import com.cham.collector.domain.CollectJob;
import com.cham.collector.domain.CollectSource;
import com.cham.collector.repository.CollectJobRepository;
import com.cham.collector.repository.CollectSourceRepository;
import com.cham.collector.service.JobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

/**
 * 기동할 때 한 번.
 *   - 배포로 죽으면서 RUNNING 으로 남은 작업을 FAILED 로 정리한다
 *   - --collector.run-once=기관코드 면 그 기관만 한 번 돌리고 끝낸다 (로컬 확인용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final CollectJobRepository jobRepository;
    private final CollectSourceRepository sourceRepository;
    private final JobRunner jobRunner;
    private final CollectorProperties props;
    private final ApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        if (!props.runOnceMode()) {
            int cleaned = jobRepository.failInterrupted();
            if (cleaned > 0) log.warn("중단된 수집 작업 {}건을 실패로 정리했습니다", cleaned);
            log.info("수집기 대기 중. 정기 수집 cron={}, {}분부터", props.cron(), props.minPeriod());
            return;
        }

        CollectSource source = sourceRepository.findByCode(props.runOnce().trim())
                .orElseThrow(() -> new IllegalArgumentException("없는 기관 코드: " + props.runOnce()));
        YearMonth target = props.runOncePeriod() == null || props.runOncePeriod().isBlank()
                ? null : YearMonth.parse(props.runOncePeriod().trim());

        CollectJob job = jobRepository.startNew("MANUAL", source.id(),
                target == null ? null : target.getYear(), target == null ? null : target.getMonthValue());
        log.info("한 번 실행: {} {} job={}", source.name(), target == null ? "" : target, job.id());
        jobRunner.run(job);
        log.info("한 번 실행 끝. 결과는 수집관리 탭 수집 이력 job={} 에서 확인", job.id());
        System.exit(SpringApplication.exit(context, () -> 0));
    }
}
