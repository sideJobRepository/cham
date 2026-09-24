package com.cham.caruse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoPlaceFinderTest {

    @Test
    void 장소명_정리() {
        assertThat(KakaoPlaceFinder.cleanName("워낭명가 외1")).isEqualTo("워낭명가");
        assertThat(KakaoPlaceFinder.cleanName("워낭명가 외 1곳")).isEqualTo("워낭명가");
        assertThat(KakaoPlaceFinder.cleanName("이디야 대전시청점, 이디야 대전청사점")).isEqualTo("이디야 대전시청점");
    }

    @Test
    void 카카오_이름과_같은_곳인지() {
        // 2026-09-24 카카오 실제 결과로 확인한 짝
        assertThat(KakaoPlaceFinder.nameMatches("이디야 탄방점", "이디야커피 대전탄방동점")).isTrue();
        assertThat(KakaoPlaceFinder.nameMatches("백두에서한라까지식당", "백두에서한라까지")).isTrue();
        assertThat(KakaoPlaceFinder.nameMatches("워낭명가", "워낭명가 유성본점")).isTrue();
        assertThat(KakaoPlaceFinder.nameMatches("이디야 탄방점", "이디야커피 대전둔산이편한세상점")).isFalse();
        assertThat(KakaoPlaceFinder.nameMatches("약선쪽쪽 둔산점", "약초마을")).isFalse();
    }

    @Test
    void 검색_범위() {
        assertThat(KakaoPlaceFinder.areaOf("대전 중구", null)).isEqualTo("대전");
        assertThat(KakaoPlaceFinder.areaOf("대전", "서구")).isEqualTo("대전 서구");
        assertThat(KakaoPlaceFinder.areaOf("대전", "대전 유성구")).isEqualTo("대전 유성구");
        assertThat(KakaoPlaceFinder.inArea("대전 서구 문정로40번길 64", "대전 서구")).isTrue();
        assertThat(KakaoPlaceFinder.inArea("대전 유성구 계룡로105번길 10", "대전 서구")).isFalse();
        assertThat(KakaoPlaceFinder.inArea("서울 서구 어딘가 1", "대전")).isFalse();
    }

    @Test
    void 찾아볼_만한_장소명() {
        assertThat(KakaoPlaceFinder.isSearchable("이디야 탄방점")).isTrue();
        assertThat(KakaoPlaceFinder.isSearchable(CardUseDefaults.ADDR_NAME)).isFalse();
        assertThat(KakaoPlaceFinder.isSearchable("경조사 직원")).isFalse();
        assertThat(KakaoPlaceFinder.isSearchable(" ")).isFalse();
    }
}
