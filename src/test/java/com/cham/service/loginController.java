package com.cham.service;

import com.cham.ControllerTestSupport;
import com.cham.security.service.impl.request.KaKaoAuthorizeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class loginController extends ControllerTestSupport {


    @DisplayName("")
    @Test
    void test() throws Exception {
        KaKaoAuthorizeRequest dto = new KaKaoAuthorizeRequest("v8n_kwcU45NFh6KigNfy_B1PtkIeqihT8pTN7-NJFH8ZCcM62SKYHgAAAAQKFwFQAAABl4ZrIXotjdRiIM79qQ");
        
        mockMvc.perform(post("/cham/kakao-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists()) // JWT 토큰이 포함되어야 함
                .andDo(print());
    }
}
