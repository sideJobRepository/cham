package com.cham.security.service.impl.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 없는 필드는 자동으로 무시
public class GoogleProfileResponse {
    
    private String sub;
    private String name;
    private String given_name;
    private String family_name;
    private String picture;
    private String email;
    private Boolean email_verified;
    private String locale;
    
    // People API에서 채워줄 전화번호(없으면 null)
    private String phoneNumber;
}
