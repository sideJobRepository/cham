package com.cham.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseData {
    
    @CreatedDate
    @Column(name = "REGIST_DATE")
    protected LocalDateTime registDate;
    
    
    @LastModifiedDate
    @Column(name = "MODIFY_DATE")
    protected LocalDateTime modifyDate;
}