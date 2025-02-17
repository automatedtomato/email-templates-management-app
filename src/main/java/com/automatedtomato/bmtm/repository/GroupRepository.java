package com.automatedtomato.bmtm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.automatedtomato.bmtm.entity.Group;
import com.automatedtomato.bmtm.enums.GroupStatus;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    Optional<Group> findByName(String name);
    Optional<Group> findById(Long id);
    List<Group> findAllByStatus(GroupStatus status);
    @Query("SELECT DISTINCT g FROM Group g " +
           "JOIN g.groupUsers gu " +
           "WHERE gu.user.id = :userId")
    List<Group> findAllByUserId(@Param("userId")Long userId);

    @Query("SELECT DISTINCT g FROM Group g " +
           "JOIN g.groupUsers gu " +
           "WHERE gu.user.id = :userId AND g.status = :status")
    List<Group> findAllByUserIdAndStatus(@Param("userId")Long userId, @Param("status")GroupStatus status);
}
