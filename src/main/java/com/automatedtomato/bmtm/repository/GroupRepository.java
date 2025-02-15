package com.automatedtomato.bmtm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.automatedtomato.bmtm.entity.Group;
import com.automatedtomato.bmtm.entity.User;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    Optional<Group> findByName(String name);

    // List<Group> findAllByUser(User user);
}
