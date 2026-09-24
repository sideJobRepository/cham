package com.cham.collect.enumeration;

/**
 * 수집 파일 상태.
 * COLLECTED / DUPLICATE 는 수집기가 넣고, 나머지는 관리자가 수집관리 탭에서 바꾼다.
 */
public enum CollectFileStatus {
    COLLECTED,  // 받아 둠, 검수 대기
    DUPLICATE,  // 이미 받은 파일과 내용이 같음(해시). 반영 대상 아님
    IMPORTED,   // 지도 자료로 반영함 (비공개로 들어감)
    IGNORED,    // 관리자가 반영하지 않기로 함
    FAILED      // 반영하다 실패함
}
