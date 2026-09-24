package com.cham.collect.parser;

/**
 * 원본에 없는 칸을 채울 기관 기본값과, 경고를 붙일 때 쓰는 대상 연·월.
 *
 * @param position    카드 승인자 직위(분류). 파일 값과 상관없이 늘 이 값이다
 * @param region      지역
 * @param user        사용자(직함) 기본값. 사용자 칸이 없고 시트 이름도 직함이 아닐 때 쓴다 (구청장)
 * @param name        이름 기본값. null 이면 비워 두고 반영할 때 기존 자료에서 찾는다
 * @param targetYear  게시물 제목에서 읽은 대상 연도. 모르면 null
 * @param targetMonth 대상 월. 모르면 null
 */
public record SourceDefaults(String position, String region, String user, String name,
                             Integer targetYear, Integer targetMonth) {
}
