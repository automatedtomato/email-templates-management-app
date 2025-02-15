package com.automatedtomato.bmtm.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.automatedtomato.bmtm.enums.GroupStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "groups",
    indexes = {
        @Index(name = "idx_groups_status", columnList = "status")
    }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupStatus status = GroupStatus.ACTIVE;

    @OneToMany(mappedBy = "group")
    private List<GroupUser> groupUsers = new ArrayList<>();

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
        Group group = (Group) o;
        return Objects.equals(id, group.id) 
            && Objects.equals(name, group.name) 
            && Objects.equals(description, group.description)
            && Objects.equals(status, group.status)
            && Objects.equals(createdAt, group.createdAt)
            && Objects.equals(updatedAt, group.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status, createdAt, updatedAt);
    }
}
