package com.cham.collector.domain;

import java.util.Set;

/** CHAM_MONIMAP_COLLECT_SOURCE 한 줄 */
public record CollectSource(
        Long id,
        String code,
        String name,
        EngineType engine,
        String listUrl,
        String detailUrl,   // {postKey} 치환. null 이면 어댑터가 목록 링크를 그대로 쓴다
        String boardId,
        String pageParam,
        String extraParam,
        Set<String> allowExt,
        boolean enabled
) {

    public String detailUrlOf(String postKey) {
        return detailUrl == null ? null : detailUrl.replace("{postKey}", postKey);
    }

    /** 목록 N 페이지 URL. 1 페이지는 목록 URL 그대로 */
    public String pageUrl(int page) {
        StringBuilder url = new StringBuilder(listUrl);
        char sep = listUrl.contains("?") ? '&' : '?';
        if (extraParam != null && !extraParam.isBlank()) {
            url.append(sep).append(extraParam);
            sep = '&';
        }
        if (page > 1 && pageParam != null && !pageParam.isBlank()) {
            url.append(sep).append(pageParam).append('=').append(page);
        }
        return url.toString();
    }
}
