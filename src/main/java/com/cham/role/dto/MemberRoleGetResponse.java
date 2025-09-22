package com.cham.role.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberRoleGetResponse {
    
    private Long memberId;
    private Long memberRoleId;
    private String memberName;
    private String memberEmail;
    private String memberRoleName;
    
}
