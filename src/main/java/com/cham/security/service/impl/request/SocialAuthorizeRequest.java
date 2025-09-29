package com.cham.security.service.impl.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialAuthorizeRequest {
    
    private String code;
}
