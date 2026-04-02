package com.wom.milkmgmt.repository;


import com.wom.milkmgmt.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    List<District> findByActiveTrue();

    boolean existsByName(String name);
}
