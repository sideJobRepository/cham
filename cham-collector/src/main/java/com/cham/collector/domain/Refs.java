package com.cham.collector.domain;

import java.time.LocalDate;

/** 어댑터가 주고받는 값들 */
public final class Refs {

    /** 목록의 게시물 하나 */
    public record PostRef(String postKey, String title, LocalDate postDate, String detailUrl) {
    }

    /**
     * 상세의 첨부 하나.
     *
     * @param attachKey   같은 게시물 안에서 첨부를 가르는 값 (atchFileId:fileSn, uid, no=0 …)
     * @param displayName 화면에 보이는 파일명. 확장자를 여기서 먼저 본다
     * @param referer     다운로드 때 붙인다. 상세에서 누른 것처럼 보여야 받아주는 곳이 있다
     */
    public record AttachmentRef(String attachKey, String url, String displayName, String referer) {
    }

    /** 받은 파일 */
    public record Downloaded(byte[] body, String headerFilename, String contentType) {
    }

    private Refs() {
    }
}
