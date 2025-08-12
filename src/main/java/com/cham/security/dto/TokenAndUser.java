package com.cham.security.dto;


import com.cham.security.handler.TokenPair;

// 토큰 + 유저 응답용
public record TokenAndUser(TokenPair token, ChamMonimapMemberResponseDto user) {}

