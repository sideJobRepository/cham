package com.cham.security.entrypoint;


import com.cham.advice.response.ErrorMessageResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component(value = "chamAuthenticationEntryPoint")
@RequiredArgsConstructor
public class ChamAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper mapper;
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(mapper.writeValueAsString(
                new ErrorMessageResponse("401", "로그인 정보가 존재하지 않습니다.")));
    }
}
