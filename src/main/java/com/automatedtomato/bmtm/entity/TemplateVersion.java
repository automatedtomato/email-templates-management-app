package com.automatedtomato.bmtm.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "template_versions",
    indexes = {
        @Index(name = "idx_template_versions_template", columnList = "template_id"),
        @Index(name = "idx_template_versions_created_by", columnList = "created_by"),
        @Index(name = "idx_template_versions_branch_version", columnList = "template_id, branch_name, version_number"),
        @Index(name = "idx_template_versions_branch", columnList = "branch_name")
    })
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TemplateVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "version_number", nullable = false)
    private Long versionNumber;

    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName = "main";

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "commit_message", columnDefinition = "TEXT")
    private String commitMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateVersion that = (TemplateVersion) o;
        return Objects.equals(id, that.id) 
            && Objects.equals(template, that.template) 
            && Objects.equals(content, that.content)
            && Objects.equals(versionNumber, that.versionNumber)
            && Objects.equals(createdBy, that.createdBy)
            && Objects.equals(commitMessage, that.commitMessage)
            && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, template, content, versionNumber, createdBy, commitMessage, createdAt);
    }

}
