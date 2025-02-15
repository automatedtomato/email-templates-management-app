package com.automatedtomato.bmtm.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.automatedtomato.bmtm.entity.GroupUser;
import com.automatedtomato.bmtm.entity.User;
import com.automatedtomato.bmtm.enums.Role;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, Long>{

    List<GroupUser> findAllByGroupId(Long groupId);
    List<GroupUser> findByGroupIdAndRole(Long groupId, Role role);
    List<GroupUser> findAllByUser(User user);

}
