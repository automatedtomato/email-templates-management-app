package com.automatedtomato.bmtm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.automatedtomato.bmtm.entity.Template;
import com.automatedtomato.bmtm.enums.TemplateStatus;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    Optional<Template> findById(Long id);
    List<Template> findAllByTitleContainingIgnoreCase(String title);
    List<Template> findAllByStatus(TemplateStatus status);
    // TemplateRepository内
    @Query("SELECT t FROM Template t WHERE t.status = 'PUBLISHED'")
    List<Template> findAllPublishedTemplates();

    @Query("SELECT t FROM Template t WHERE t.status = :status AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Template> findAllPublishedTemplatesByKeyword(
        @Param("status") TemplateStatus status,
        @Param("keyword") String keyword
    );

    @Query("SELECT t FROM Template t WHERE t.status = 'DRAFT'")
    List<Template> findAllDraftTemplates();

    @Query("SELECT t FROM Template t WHERE t.status = :status AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Template> findAllDraftTemplatesByKeyword(
        @Param("status") TemplateStatus status,
        @Param("keyword") String keyword
    );

    @Query("SELECT t FROM Template t WHERE t.status = 'ARCHIVED'")
    List<Template> findAllArchivedTemplates();

    @Query("SELECT t FROM Template t WHERE t.status = :status AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Template> findAllArchivedTemplatesByKeyword(
        @Param("status") TemplateStatus status,
        @Param("keyword") String keyword
    );
}
