package com.cham.repository;

import com.cham.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository  extends JpaRepository<Reply, Long> {
    

}
