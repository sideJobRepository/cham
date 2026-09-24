package com.cham.collect.dto;

/**
 * 반영 요청. 둘 다 비워도 된다.
 *
 * @param deleteKey   비우면 '수집-기관명-연-월' 로 만든다
 * @param defaultName 원본에 이름이 없고 기존 자료에서도 못 찾은 줄에 넣을 이름
 */
public record CollectImportRequest(String deleteKey, String defaultName) {
}
