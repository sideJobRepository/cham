package com.cham.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 업무추진비 자동수집기.
 *
 * 기관 게시판을 매일 한 번 돌며 원본 첨부를 S3 에 보관하고 CHAM_MONIMAP_COLLECT_FILE 에 기록한다.
 * 원본을 지도 자료로 바꾸는 일(파싱·반영)은 하지 않는다. 관리자가 cham-api 수집관리 탭에서 한다.
 * cham-api 와 따로 도는 이유는 수집기가 죽거나 느려져도 서비스는 살아 있어야 해서다.
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class CollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }
}
