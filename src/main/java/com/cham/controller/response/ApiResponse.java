package com.cham.controller.response;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ApiResponse {
    private final int code;
    private final boolean success;
    private final String message;
}
