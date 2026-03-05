package com.malgn.repository;

import com.malgn.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content,Long> {
    List<Content> findAllByUserId(Long userId);
}
