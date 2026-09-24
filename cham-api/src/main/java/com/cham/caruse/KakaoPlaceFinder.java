package com.cham.caruse;

import com.cham.caruse.dto.KakaoPlaceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 장소명만 있고 주소가 없을 때 카카오 키워드 검색으로 도로명주소를 찾는다.
 * ('이디야 탄방점' → '이디야커피 대전탄방동점', '대전 서구 문정로40번길 64')
 *
 * 엉뚱한 가게를 넣지 않는 게 먼저다. 그래서
 *  - 지역('대전', 원본에 구별 칸이 있으면 '대전 서구')을 붙여 찾고, 결과 주소도 그 지역이어야 한다
 *  - 장소명의 단어가 카카오 가게 이름에 모두 들어 있어야 한다(끝의 '점' 은 떼고 비교: 탄방점 ↔ 탄방동점)
 *  - 맞는 후보가 여럿이면(지점 없이 '워낭명가') 넣지 않는다
 * 같은 이름은 다시 묻지 않도록 기억해 둔다(미리보기를 여러 번 열어도 카카오를 다시 안 부른다).
 */
@Slf4j
@Component
public class KakaoPlaceFinder {

    public enum Status { FOUND, AMBIGUOUS, NOT_FOUND }

    /** @param placeName 카카오 가게 이름, @param address 도로명주소(없으면 지번) */
    public record Result(Status status, String placeName, String address, int candidates) {
        static final Result NOT_FOUND = new Result(Status.NOT_FOUND, null, null, 0);
    }

    private static final int CACHE_LIMIT = 5000;

    private final Map<String, Result> cache = new ConcurrentHashMap<>();

    @Value("${kakao.clientId}")
    private String kakaoClientId;

    /**
     * 검색 범위. 시·도(지역의 첫 단어)에 원본의 구가 있으면 붙인다('대전' + '서구' → '대전 서구').
     * 기관 지역이 '대전 중구' 여도 구까지 쓰지 않는다. 의원·단체장은 관내 밖에서도 쓰기 때문이다
     */
    public static String areaOf(String region, String district) {
        String city = firstWord(region);
        if (!StringUtils.hasText(district)) return city;
        String d = district.trim();
        return StringUtils.hasText(city) && !d.startsWith(city) ? city + " " + d : d;
    }

    public Result find(String area, String rawPlaceName) {
        String name = cleanName(rawPlaceName);
        if (!StringUtils.hasText(name)) return Result.NOT_FOUND;
        area = area == null ? "" : area.trim();
        String key = area + "|" + name;
        Result cached = cache.get(key);
        if (cached != null) return cached;

        Result result;
        try {
            result = search(area, name);
        } catch (RuntimeException e) {
            // 카카오가 잠깐 안 되면 그냥 못 찾은 것으로 둔다. 기억하지 않아 다음에 다시 묻는다
            log.warn("카카오 장소 검색 실패: {} ({})", name, e.getMessage());
            return Result.NOT_FOUND;
        }
        if (cache.size() > CACHE_LIMIT) cache.clear();
        cache.put(key, result);
        return result;
    }

    private Result search(String area, String name) {
        String query = StringUtils.hasText(area) ? area + " " + name : name;
        KakaoPlaceResponse body = RestClient.create().get()
                .uri(b -> b.scheme("https").host("dapi.kakao.com").path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .queryParam("size", 15)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoClientId)
                .retrieve()
                .body(KakaoPlaceResponse.class);
        if (body == null || body.getDocuments() == null) return Result.NOT_FOUND;

        List<KakaoPlaceResponse.Document> matches = new ArrayList<>();
        for (KakaoPlaceResponse.Document d : body.getDocuments()) {
            String address = StringUtils.hasText(d.getRoadAddressName()) ? d.getRoadAddressName() : d.getAddressName();
            if (!StringUtils.hasText(address)) continue;
            if (!inArea(address, area)) continue;
            if (nameMatches(name, d.getPlaceName())) matches.add(d);
        }
        if (matches.isEmpty()) return Result.NOT_FOUND;

        // 이름이 똑같은 곳이 있으면 그것, 아니면 후보가 딱 하나일 때만
        String exact = noSpace(name);
        KakaoPlaceResponse.Document pick = matches.stream()
                .filter(d -> noSpace(d.getPlaceName()).equals(exact))
                .findFirst()
                .orElse(matches.size() == 1 ? matches.get(0) : null);
        if (pick == null) return new Result(Status.AMBIGUOUS, null, null, matches.size());

        String address = StringUtils.hasText(pick.getRoadAddressName()) ? pick.getRoadAddressName() : pick.getAddressName();
        return new Result(Status.FOUND, pick.getPlaceName(), address, matches.size());
    }

    /** 찾아볼 만한 장소명인지. 자리값('경조사비')이나 경조사·온라인 구매는 가게 위치가 없다 */
    public static boolean isSearchable(String placeName) {
        if (!StringUtils.hasText(placeName)) return false;
        String n = placeName.trim();
        if (n.equals(CardUseDefaults.ADDR_NAME)) return false;
        return !n.matches(".*(경조사|부의금|축의금|조의금|화환|인터넷|온라인).*");
    }

    /** '워낭명가 외1' → '워낭명가', '이디야 대전시청점, 이디야 대전청사점' → '이디야 대전시청점' */
    static String cleanName(String raw) {
        if (raw == null) return null;
        String n = raw.split("[,，/]")[0];
        n = n.replaceAll("\\s*외\\s*\\d+\\s*(곳|건|개소)?\\s*$", "");
        n = n.replaceAll("[()（）]", " ").replaceAll("\\s+", " ").trim();
        return n.isEmpty() ? null : n;
    }

    /** 한쪽이 다른 쪽을 포함하거나, 장소명의 단어가 모두 카카오 이름에 들어 있으면 같은 곳으로 본다 */
    static boolean nameMatches(String name, String kakaoName) {
        if (kakaoName == null) return false;
        String a = noSpace(name);
        String b = noSpace(kakaoName);
        if (a.length() >= 2 && (b.contains(a) || a.contains(b) && b.length() >= 3)) return true;
        for (String token : name.split("\\s+")) {
            String t = token.length() > 2 ? token.replaceAll("(본점|점)$", "") : token;
            if (t.length() < 2) continue;
            if (!b.contains(t)) return false;
        }
        return name.trim().contains(" ");
    }

    // '대전 서구' 면 주소가 '대전' 으로 시작하고 '서구' 를 품어야 한다
    static boolean inArea(String address, String area) {
        if (!StringUtils.hasText(area)) return true;
        String[] words = area.split("\\s+");
        if (!address.startsWith(words[0])) return false;
        for (int i = 1; i < words.length; i++) {
            if (!address.contains(words[i])) return false;
        }
        return true;
    }

    private static String noSpace(String s) {
        return s == null ? "" : s.replaceAll("\\s+", "");
    }

    private static String firstWord(String region) {
        if (!StringUtils.hasText(region)) return "";
        return region.trim().split("\\s+")[0];
    }
}
