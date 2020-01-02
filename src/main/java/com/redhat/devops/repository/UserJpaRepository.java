package com.redhat.devops.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.redhat.devops.entity.User;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Integer> {

	List<User> findByName(String name);

}
