package com.automatedtomato.bmtm.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.automatedtomato.bmtm.enums.TemplateStatus;

import jakarta.persistence.*;
import lombok.*;

// テンプレートのメタ情報を管理
// 実際のテンプレート内容はTemplateVersionエンティティに格納
@Entity
@Table(
    name = "templates",
    indexes = {
        @Index(name = "idx_templates_created_by", columnList = "created_by"),
        @Index(name = "idx_templates_status", columnList = "status"),
        @Index(name = "dx_templates_title", columnList = "title"),
        @Index(name = "idx_templates_is_archived", columnList = "is_archived")
    }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name  = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TemplateStatus status = TemplateStatus.DRAFT;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_version_id")
    private TemplateVersion lastVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Template template = (Template) o;
        return Objects.equals(id, template.id) 
            && Objects.equals(title, template.title) 
            && Objects.equals(description, template.description)
            && Objects.equals(createdBy, template.createdBy)
            && Objects.equals(isArchived, template.isArchived)
            && Objects.equals(status, template.status)
            && Objects.equals(createdAt, template.createdAt)
            && Objects.equals(updatedAt, template.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, createdBy, isArchived, status, createdAt, updatedAt);
    }
}
