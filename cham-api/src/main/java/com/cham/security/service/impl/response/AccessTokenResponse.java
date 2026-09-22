package com.cham.security.service.impl.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) //없는 필드는 자동무시
public class AccessTokenResponse {
    private String access_token;
    private String expires_in;
    private String scope;
    private String id_token;
    private String error;
    private String error_description;
}
