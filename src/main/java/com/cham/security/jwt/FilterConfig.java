package com.cham.security.jwt;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    
    
    @Bean
    public FilterRegistrationBean<JwtTokenFilter> disableJwtFilterRegistration(JwtTokenFilter jwtTokenFilter) {
        FilterRegistrationBean<JwtTokenFilter> registrationBean = new FilterRegistrationBean<>(jwtTokenFilter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
