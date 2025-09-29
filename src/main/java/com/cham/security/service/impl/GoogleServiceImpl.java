package com.cham.security.service.impl;

import com.cham.security.service.SocialService;
import com.cham.security.service.impl.response.*;
import com.enumtype.SocialType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Stream;

@Service("googleService")
@Transactional
@RequiredArgsConstructor
public class GoogleServiceImpl implements SocialService {
    
    @Value("${google.clientId}")
    private String googleClientId;
    @Value("${google.redirecturi}")
    private String googleRedirectUri;
    @Value("${google.client-secret}")
    private String googleClientSecret;
    
    @Override
    public SocialType type() {
        return SocialType.GOOGLE;
    }
    
    @Override
    public AccessTokenResponse getAccessToken(String code) {
        RestClient restClient = RestClient.create();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");
        params.add("client_secret",googleClientSecret);
        
        ResponseEntity<AccessTokenResponse> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(AccessTokenResponse.class);
        
        return response.getBody();
    }
    
    @Override
    public SocialProfile getProfile(String accessToken) {
        GoogleProfileResponse resp = RestClient.create().get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(GoogleProfileResponse.class);
        
        if (resp == null) resp = new GoogleProfileResponse();
        
        String peopleUrl = UriComponentsBuilder
                .fromHttpUrl("https://people.googleapis.com/v1/people/me")
                .queryParam("personFields", "phoneNumbers")
                .queryParam("sources", "READ_SOURCE_TYPE_PROFILE")
                .build(true)
                .toUriString();
        
        GooglePeopleResponse people = RestClient.create().get()
                .uri(peopleUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(GooglePeopleResponse.class);

        // primary 우선 → 없으면 첫 번째
        String phone = null;
        if (people != null && people.getPhoneNumbers() != null && !people.getPhoneNumbers().isEmpty()) {
            Optional<String> primary = people.getPhoneNumbers().stream()
                    .filter(p -> p != null && p.getMetadata() != null && Boolean.TRUE.equals(p.getMetadata().getPrimary()))
                    .map(GooglePeopleResponse.PhoneNumber::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst();
            
            phone = primary.orElseGet(() ->
                    people.getPhoneNumbers().stream()
                            .filter(Objects::nonNull)
                            .map(GooglePeopleResponse.PhoneNumber::getValue)
                            .filter(v -> v != null && !v.isBlank())
                            .findFirst()
                            .orElse(null)
            );
        }
        
        resp.setPhoneNumber(phone);
        
        return new SocialProfile(
                SocialType.GOOGLE,
                resp.getSub(),
                resp.getEmail(),
                resp.getName(),
                resp.getName(),
                resp.getPhoneNumber(),
                resp.getPicture(),
                resp.getPicture()
        );
    }
}
