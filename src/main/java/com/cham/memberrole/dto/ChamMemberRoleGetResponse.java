package com.cham.memberrole.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChamMemberRoleGetResponse {
    private Long memberId;
    private Long roleId;
    private Long memberRoleId;
    private String memberName;
    private String roleName;
    private String memberEmail;
    private String memberPhoneNo;
}
