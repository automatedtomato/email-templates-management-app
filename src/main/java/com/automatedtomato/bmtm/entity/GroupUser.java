package com.automatedtomato.bmtm.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.automatedtomato.bmtm.enums.Role;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "group_users",
    indexes = {
        @Index(name = "idx_group_users_group", columnList = "group_id"),
        @Index(name = "idx_group_users_user", columnList = "user_id"),
        @Index(name = "idx_group_users_role", columnList = "role"),
        @Index(name = "idx_group_users_user_group", columnList = "group_id, user_id", unique = true)
    }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GroupUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.VIEWER;

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
        GroupUser groupUser = (GroupUser) o;
        return (Objects.equals(id, groupUser.id)
             && Objects.equals(group, groupUser.group)
             && Objects.equals(user, groupUser.user)
             && Objects.equals(role, groupUser.role)
             && Objects.equals(createdAt, groupUser.createdAt)
             && Objects.equals(updatedAt, groupUser.updatedAt)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, group, user, role, createdAt, updatedAt);
    }
}
