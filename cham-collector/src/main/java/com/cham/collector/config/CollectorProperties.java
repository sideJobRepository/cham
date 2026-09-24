package com.cham.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.YearMonth;

@ConfigurationProperties("collector")
public record CollectorProperties(
        String cron,
        long pollIntervalMs,
        String minPeriod,
        long requestDelayMs,
        int connectTimeoutMs,
        int readTimeoutMs,
        int retries,
        long maxFileBytes,
        int scheduledPageLimit,
        int manualPageLimit,
        int targetPageLimit,
        String userAgent,
        String s3Prefix,
        String runOnce,
        String runOncePeriod
) {

    public YearMonth minYearMonth() {
        return YearMonth.parse(minPeriod);
    }

    public boolean runOnceMode() {
        return runOnce != null && !runOnce.isBlank();
    }
}
