package com.cham.feedbacck.legislation.entity;


import com.cham.base.BaseData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "LEGISLATION")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Legislation extends BaseData {


    // 법률제정 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEGISLATION_ID")
    private Long id;

    // 제목
    @Column(name = "TITLE")
    private String title;

    // 빌 버전
    @Column(name = "BILL_VERSION")
    private String billVersion;
    
}
