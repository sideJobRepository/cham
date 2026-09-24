package com.cham.collect.enumeration;

public enum CollectJobStatus {
    REQUESTED,  // 관리자가 요청함. 수집기가 1분 안에 가져간다
    RUNNING,
    DONE,
    FAILED
}
