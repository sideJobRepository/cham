package com.cham.collector.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

/**
 * 수집 파일 기록. 수집기는 넣기만 하고 상태를 바꾸지 않는다(상태는 cham-api 관리자 화면이 바꾼다).
 */
@Repository
@RequiredArgsConstructor
public class CollectFileRepository {

    public record NewFile(long sourceId, Long jobId, YearMonth period, String postKey, String postTitle,
                          LocalDate postDate, String detailUrl, String attachKey, String sourceUrl,
                          String originName, String ext, String s3Key, String sha256, long size,
                          String status, Long dupOfId) {
    }

    public record ShaHit(long fileId, String s3Key) {
    }

    private final JdbcTemplate jdbc;

    /** 이 게시물에서 이미 받은 파일이 있는지. 있으면 정기 실행은 상세를 열지 않는다 */
    public boolean existsPost(long sourceId, String postKey) {
        return exists("""
                SELECT 1 FROM CHAM_MONIMAP_COLLECT_FILE
                WHERE CHAM_MONIMAP_COLLECT_SOURCE_ID = ? AND CHAM_MONIMAP_COLLECT_FILE_POST_KEY = ? LIMIT 1
                """, sourceId, postKey);
    }

    public boolean existsAttach(long sourceId, String postKey, String attachKey) {
        return exists("""
                SELECT 1 FROM CHAM_MONIMAP_COLLECT_FILE
                WHERE CHAM_MONIMAP_COLLECT_SOURCE_ID = ? AND CHAM_MONIMAP_COLLECT_FILE_POST_KEY = ?
                  AND CHAM_MONIMAP_COLLECT_FILE_ATTACH_KEY = ? LIMIT 1
                """, sourceId, postKey, attachKey);
    }

    /** 이 기관의 그 달 자료를 이미 받았는지. 정기 실행에서 직전 달이 있으면 그 기관을 건너뛴다 */
    public boolean existsPeriod(long sourceId, YearMonth period) {
        return exists("""
                SELECT 1 FROM CHAM_MONIMAP_COLLECT_FILE
                WHERE CHAM_MONIMAP_COLLECT_SOURCE_ID = ? AND CHAM_MONIMAP_COLLECT_FILE_YEAR = ?
                  AND CHAM_MONIMAP_COLLECT_FILE_MONTH = ? LIMIT 1
                """, sourceId, period.getYear(), period.getMonthValue());
    }

    /** 내용이 같은 파일(해시)을 이미 받았으면 그 원본 */
    public Optional<ShaHit> findOriginalBySha(String sha256) {
        return jdbc.query("""
                        SELECT CHAM_MONIMAP_COLLECT_FILE_ID, CHAM_MONIMAP_COLLECT_FILE_S3_KEY FROM CHAM_MONIMAP_COLLECT_FILE
                        WHERE CHAM_MONIMAP_COLLECT_FILE_SHA256 = ? AND CHAM_MONIMAP_COLLECT_FILE_STATUS <> 'DUPLICATE'
                        ORDER BY CHAM_MONIMAP_COLLECT_FILE_ID LIMIT 1
                        """,
                (rs, i) -> new ShaHit(rs.getLong(1), rs.getString(2)), sha256).stream().findFirst();
    }

    public void insert(NewFile f) {
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("""
                        INSERT INTO CHAM_MONIMAP_COLLECT_FILE
                        (CHAM_MONIMAP_COLLECT_SOURCE_ID, CHAM_MONIMAP_COLLECT_JOB_ID, CHAM_MONIMAP_COLLECT_FILE_YEAR,
                         CHAM_MONIMAP_COLLECT_FILE_MONTH, CHAM_MONIMAP_COLLECT_FILE_POST_KEY, CHAM_MONIMAP_COLLECT_FILE_POST_TITLE,
                         CHAM_MONIMAP_COLLECT_FILE_POST_DATE, CHAM_MONIMAP_COLLECT_FILE_DETAIL_URL, CHAM_MONIMAP_COLLECT_FILE_ATTACH_KEY,
                         CHAM_MONIMAP_COLLECT_FILE_SOURCE_URL, CHAM_MONIMAP_COLLECT_FILE_ORIGIN_NAME, CHAM_MONIMAP_COLLECT_FILE_EXT,
                         CHAM_MONIMAP_COLLECT_FILE_S3_KEY, CHAM_MONIMAP_COLLECT_FILE_SHA256, CHAM_MONIMAP_COLLECT_FILE_SIZE,
                         CHAM_MONIMAP_COLLECT_FILE_COLLECTED_AT, CHAM_MONIMAP_COLLECT_FILE_STATUS, CHAM_MONIMAP_COLLECT_FILE_DUP_OF_ID,
                         REGIST_DATE, MODIFY_DATE)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                f.sourceId(), f.jobId(),
                f.period() == null ? null : f.period().getYear(),
                f.period() == null ? null : f.period().getMonthValue(),
                f.postKey(), CollectSourceRepository.cut(f.postTitle(), 500), f.postDate(),
                CollectSourceRepository.cut(f.detailUrl(), 1000), CollectSourceRepository.cut(f.attachKey(), 300),
                CollectSourceRepository.cut(f.sourceUrl(), 1000), CollectSourceRepository.cut(f.originName(), 500),
                f.ext(), f.s3Key(), f.sha256(), f.size(), now, f.status(), f.dupOfId(), now, now);
    }

    private boolean exists(String sql, Object... args) {
        return !jdbc.queryForList(sql, Integer.class, args).isEmpty();
    }
}
