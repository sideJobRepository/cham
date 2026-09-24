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
        String attachInclude,   // 받을 첨부 파일명 정규식. null 이면 전부
        String attachExclude,   // 뺄 첨부 파일명 정규식. null 이면 없음
        String postInclude,     // 받을 게시물 제목 정규식. null 이면 전부 (서구의회: 다른 글이 섞인 게시판)
        boolean enabled
) {

    public boolean wantsPost(String title) {
        return postInclude == null || postInclude.isBlank()
                || java.util.regex.Pattern.compile(postInclude).matcher(title == null ? "" : title).find();
    }

    /** 확장자와 파일명 규칙을 모두 통과하는 첨부인지 (대전시: 시장·부시장만, '정무' 는 뺀다) */
    public boolean wants(String fileName, String ext) {
        if (!allowExt.contains(ext)) return false;
        String n = fileName == null ? "" : fileName;
        if (attachInclude != null && !attachInclude.isBlank()
                && !java.util.regex.Pattern.compile(attachInclude).matcher(n).find()) return false;
        return attachExclude == null || attachExclude.isBlank()
                || !java.util.regex.Pattern.compile(attachExclude).matcher(n).find();
    }

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
