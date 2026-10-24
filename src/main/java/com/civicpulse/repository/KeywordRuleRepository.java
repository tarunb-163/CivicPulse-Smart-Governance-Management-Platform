package com.civicpulse.repository;

import com.civicpulse.entity.KeywordRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeywordRuleRepository extends JpaRepository<KeywordRule, Long> {
    List<KeywordRule> findByCategoryId(Long categoryId);

    @Query("SELECT kr FROM KeywordRule kr JOIN FETCH kr.category c JOIN FETCH c.department d")
    List<KeywordRule> findAllWithCategoryAndDepartment();
}
