package com.wom.milkmgmt.repository;

import com.wom.milkmgmt.entity.Role;
import com.wom.milkmgmt.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}

