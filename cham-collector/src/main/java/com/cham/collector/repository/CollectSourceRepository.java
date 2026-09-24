package com.cham.collector.repository;

import com.cham.collector.domain.CollectSource;
import com.cham.collector.domain.EngineType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CollectSourceRepository {

    private static final String SELECT = """
            SELECT CHAM_MONIMAP_COLLECT_SOURCE_ID, CHAM_MONIMAP_COLLECT_SOURCE_CODE, CHAM_MONIMAP_COLLECT_SOURCE_NAME,
                   CHAM_MONIMAP_COLLECT_SOURCE_ENGINE, CHAM_MONIMAP_COLLECT_SOURCE_LIST_URL, CHAM_MONIMAP_COLLECT_SOURCE_DETAIL_URL,
                   CHAM_MONIMAP_COLLECT_SOURCE_BOARD_ID, CHAM_MONIMAP_COLLECT_SOURCE_PAGE_PARAM, CHAM_MONIMAP_COLLECT_SOURCE_EXTRA_PARAM,
                   CHAM_MONIMAP_COLLECT_SOURCE_ALLOW_EXT, CHAM_MONIMAP_COLLECT_SOURCE_ENABLED,
                   CHAM_MONIMAP_COLLECT_SOURCE_ATTACH_INCLUDE, CHAM_MONIMAP_COLLECT_SOURCE_ATTACH_EXCLUDE
            FROM CHAM_MONIMAP_COLLECT_SOURCE
            """;

    private static final RowMapper<CollectSource> MAPPER = (rs, i) -> new CollectSource(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            EngineType.valueOf(rs.getString(4)),
            rs.getString(5),
            rs.getString(6),
            rs.getString(7),
            rs.getString(8),
            rs.getString(9),
            Arrays.stream(rs.getString(10).split(","))
                    .map(String::trim).map(String::toLowerCase).filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet()),
            rs.getString(12),
            rs.getString(13),
            rs.getBoolean(11));

    private final JdbcTemplate jdbc;

    public List<CollectSource> findEnabled() {
        return jdbc.query(SELECT + " WHERE CHAM_MONIMAP_COLLECT_SOURCE_ENABLED = 1"
                + " ORDER BY CHAM_MONIMAP_COLLECT_SOURCE_SORT, CHAM_MONIMAP_COLLECT_SOURCE_ID", MAPPER);
    }

    public Optional<CollectSource> findById(long id) {
        return jdbc.query(SELECT + " WHERE CHAM_MONIMAP_COLLECT_SOURCE_ID = ?", MAPPER, id).stream().findFirst();
    }

    public Optional<CollectSource> findByCode(String code) {
        return jdbc.query(SELECT + " WHERE CHAM_MONIMAP_COLLECT_SOURCE_CODE = ?", MAPPER, code).stream().findFirst();
    }

    public void markSuccess(long id) {
        jdbc.update("""
                UPDATE CHAM_MONIMAP_COLLECT_SOURCE
                SET CHAM_MONIMAP_COLLECT_SOURCE_LAST_SUCCESS = ?, CHAM_MONIMAP_COLLECT_SOURCE_LAST_ERROR = NULL, MODIFY_DATE = ?
                WHERE CHAM_MONIMAP_COLLECT_SOURCE_ID = ?
                """, LocalDateTime.now(), LocalDateTime.now(), id);
    }

    public void markError(long id, String error) {
        jdbc.update("""
                UPDATE CHAM_MONIMAP_COLLECT_SOURCE
                SET CHAM_MONIMAP_COLLECT_SOURCE_LAST_ERROR = ?, MODIFY_DATE = ?
                WHERE CHAM_MONIMAP_COLLECT_SOURCE_ID = ?
                """, cut(error, 2000), LocalDateTime.now(), id);
    }

    static String cut(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
