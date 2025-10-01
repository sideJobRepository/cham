package com.cham.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberRolePutRequest {
    @NotNull(message = "권한 아이디는 필수입니다.")
    private Long roleId;
    @NotNull(message = "회원 권한 아이디는 필수입니다.")
    private Long memberRoleId;
}
