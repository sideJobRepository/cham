package com.cham.collector.repository;

import com.cham.collector.domain.CollectJob;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollectJobRepository {

    private static final RowMapper<CollectJob> MAPPER = (rs, i) -> new CollectJob(
            rs.getLong("CHAM_MONIMAP_COLLECT_JOB_ID"),
            rs.getString("CHAM_MONIMAP_COLLECT_JOB_TRIGGER"),
            (Long) rs.getObject("CHAM_MONIMAP_COLLECT_SOURCE_ID", Long.class),
            (Integer) rs.getObject("CHAM_MONIMAP_COLLECT_JOB_PAGE_LIMIT", Integer.class),
            (Integer) rs.getObject("CHAM_MONIMAP_COLLECT_JOB_TARGET_YEAR", Integer.class),
            (Integer) rs.getObject("CHAM_MONIMAP_COLLECT_JOB_TARGET_MONTH", Integer.class));

    private static final String SELECT = """
            SELECT CHAM_MONIMAP_COLLECT_JOB_ID, CHAM_MONIMAP_COLLECT_JOB_TRIGGER, CHAM_MONIMAP_COLLECT_SOURCE_ID,
                   CHAM_MONIMAP_COLLECT_JOB_PAGE_LIMIT, CHAM_MONIMAP_COLLECT_JOB_TARGET_YEAR, CHAM_MONIMAP_COLLECT_JOB_TARGET_MONTH
            FROM CHAM_MONIMAP_COLLECT_JOB
            """;

    private final JdbcTemplate jdbc;

    /** 수집기가 스스로 시작하는 작업. 바로 RUNNING 으로 넣는다 */
    public CollectJob startNew(String trigger, Long sourceId, Integer targetYear, Integer targetMonth) {
        LocalDateTime now = LocalDateTime.now();
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO CHAM_MONIMAP_COLLECT_JOB
                    (CHAM_MONIMAP_COLLECT_JOB_TRIGGER, CHAM_MONIMAP_COLLECT_SOURCE_ID, CHAM_MONIMAP_COLLECT_JOB_TARGET_YEAR,
                     CHAM_MONIMAP_COLLECT_JOB_TARGET_MONTH, CHAM_MONIMAP_COLLECT_JOB_STATUS, CHAM_MONIMAP_COLLECT_JOB_STARTED_AT,
                     REGIST_DATE, MODIFY_DATE)
                    VALUES (?, ?, ?, ?, 'RUNNING', ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, trigger);
            setNullable(ps, 2, sourceId);
            setNullable(ps, 3, targetYear);
            setNullable(ps, 4, targetMonth);
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setTimestamp(7, Timestamp.valueOf(now));
            return ps;
        }, key);
        return new CollectJob(key.getKey().longValue(), trigger, sourceId, null, targetYear, targetMonth);
    }

    /**
     * 관리자가 요청한 작업 중 가장 오래된 것을 가져온다.
     * 조건부 UPDATE 로 가져가서, 수집기가 둘 떠 있어도(배포 중 잠깐) 한쪽만 잡는다.
     */
    public Optional<CollectJob> claimNextRequested() {
        if (existsRunning()) return Optional.empty();
        Optional<CollectJob> next = jdbc.query(SELECT
                + " WHERE CHAM_MONIMAP_COLLECT_JOB_STATUS = 'REQUESTED' ORDER BY CHAM_MONIMAP_COLLECT_JOB_ID LIMIT 1",
                MAPPER).stream().findFirst();
        if (next.isEmpty()) return Optional.empty();

        LocalDateTime now = LocalDateTime.now();
        int updated = jdbc.update("""
                UPDATE CHAM_MONIMAP_COLLECT_JOB
                SET CHAM_MONIMAP_COLLECT_JOB_STATUS = 'RUNNING', CHAM_MONIMAP_COLLECT_JOB_STARTED_AT = ?, MODIFY_DATE = ?
                WHERE CHAM_MONIMAP_COLLECT_JOB_ID = ? AND CHAM_MONIMAP_COLLECT_JOB_STATUS = 'REQUESTED'
                """, now, now, next.get().id());
        return updated == 1 ? next : Optional.empty();
    }

    public boolean existsRunning() {
        return !jdbc.queryForList("SELECT 1 FROM CHAM_MONIMAP_COLLECT_JOB WHERE CHAM_MONIMAP_COLLECT_JOB_STATUS = 'RUNNING' LIMIT 1",
                Integer.class).isEmpty();
    }

    public void updateProgress(long jobId, int postsSeen, int filesNew, int filesSkipped, int filesFailed, String log) {
        jdbc.update("""
                UPDATE CHAM_MONIMAP_COLLECT_JOB
                SET CHAM_MONIMAP_COLLECT_JOB_POSTS_SEEN = ?, CHAM_MONIMAP_COLLECT_JOB_FILES_NEW = ?,
                    CHAM_MONIMAP_COLLECT_JOB_FILES_SKIPPED = ?, CHAM_MONIMAP_COLLECT_JOB_FILES_FAILED = ?,
                    CHAM_MONIMAP_COLLECT_JOB_LOG = ?, MODIFY_DATE = ?
                WHERE CHAM_MONIMAP_COLLECT_JOB_ID = ?
                """, postsSeen, filesNew, filesSkipped, filesFailed, log, LocalDateTime.now(), jobId);
    }

    public void finish(long jobId, String status) {
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("""
                UPDATE CHAM_MONIMAP_COLLECT_JOB
                SET CHAM_MONIMAP_COLLECT_JOB_STATUS = ?, CHAM_MONIMAP_COLLECT_JOB_FINISHED_AT = ?, MODIFY_DATE = ?
                WHERE CHAM_MONIMAP_COLLECT_JOB_ID = ?
                """, status, now, now, jobId);
    }

    /** 배포로 프로세스가 죽으면 RUNNING 이 남는다. 기동할 때 정리한다 */
    public int failInterrupted() {
        LocalDateTime now = LocalDateTime.now();
        return jdbc.update("""
                UPDATE CHAM_MONIMAP_COLLECT_JOB
                SET CHAM_MONIMAP_COLLECT_JOB_STATUS = 'FAILED', CHAM_MONIMAP_COLLECT_JOB_FINISHED_AT = ?, MODIFY_DATE = ?,
                    CHAM_MONIMAP_COLLECT_JOB_LOG = CONCAT(COALESCE(CHAM_MONIMAP_COLLECT_JOB_LOG, ''), '\\n수집기 재시작으로 중단됨')
                WHERE CHAM_MONIMAP_COLLECT_JOB_STATUS = 'RUNNING'
                """, now, now);
    }

    private static void setNullable(PreparedStatement ps, int idx, Number value) throws java.sql.SQLException {
        if (value == null) ps.setNull(idx, Types.BIGINT);
        else ps.setLong(idx, value.longValue());
    }
}
