package com.cham.collector.schedule;

import com.cham.collector.config.CollectorProperties;
import com.cham.collector.domain.CollectJob;
import com.cham.collector.repository.CollectJobRepository;
import com.cham.collector.service.JobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 언제 수집하는가.
 *   - 매일 새벽 3시 정기 수집. 기관이 언제 올릴지 몰라 매일 목록 1페이지를 보고, 이미 받은 게시물·파일은 건너뛴다
 *   - 1분마다 관리자 요청(REQUESTED)이 있는지 DB 만 확인한다. 기관 사이트에는 요청하지 않는다
 * 스케줄 스레드가 하나라(spring.task.scheduling.pool.size=1) 둘이 동시에 돌지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectScheduler {

    private final CollectJobRepository jobRepository;
    private final JobRunner jobRunner;
    private final CollectorProperties props;

    @Scheduled(cron = "${collector.cron}", zone = "Asia/Seoul")
    public void daily() {
        if (props.runOnceMode()) return;
        if (jobRepository.existsRunning()) {
            log.warn("실행 중인 수집이 있어 오늘 정기 수집을 건너뜁니다");
            return;
        }
        CollectJob job = jobRepository.startNew("SCHEDULED", null, null, null);
        log.info("정기 수집 시작 job={}", job.id());
        jobRunner.run(job);
        log.info("정기 수집 끝 job={}", job.id());
    }

    @Scheduled(fixedDelayString = "${collector.poll-interval-ms}", initialDelay = 15000)
    public void pollRequested() {
        if (props.runOnceMode()) return;
        Optional<CollectJob> job = jobRepository.claimNextRequested();
        if (job.isEmpty()) return;
        log.info("요청 수집 시작 job={} source={} target={}", job.get().id(), job.get().sourceId(), job.get().target());
        jobRunner.run(job.get());
        log.info("요청 수집 끝 job={}", job.get().id());
    }
}
