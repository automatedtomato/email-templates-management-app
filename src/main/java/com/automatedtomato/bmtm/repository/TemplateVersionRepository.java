package com.automatedtomato.bmtm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.automatedtomato.bmtm.entity.TemplateVersion;

@Repository
public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long>{

    @Query("SELECT COALESCE(MAX(tv.versionNumber), 0) + 1 FROM TemplateVersion tv WHERE tv.template.id = :templateId AND tv.branchName = :branchName")
    Long getNextVersionNumber(@Param("templateId") Long templateId,
                              @Param("branchName") String branchName);

    List<TemplateVersion> findByTemplateIdOrderByVersionNumberDesc(Long templateId);

    List<TemplateVersion> findByTemplateIdAndBranchNameOrderByVersionNumberDesc(Long templateId, String branchName);

    Optional<TemplateVersion> findFirstByTemplateIdAndBranchNameOrderByVersionNumberDesc(Long templateId, String branchName);
}
