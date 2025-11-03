package com.cham.theme.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ThemePostRequestWrapper {
    private List<ThemePostRequest> themes;
}
