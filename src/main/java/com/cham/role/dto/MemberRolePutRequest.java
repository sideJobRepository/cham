package com.cham.role.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberRolePutRequest {
    private Long roleId;
    private Long memberRoleId;
}
