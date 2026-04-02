package com.wom.milkmgmt.repository;

import com.wom.milkmgmt.entity.MilkType;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilkTypeRepository extends JpaRepository<MilkType, Long> {

    List<MilkType> findByActiveTrue();

    boolean existsByNameAndVolumeMl(String name, Integer volumeMl);
    // Check duplicate name + volume (excluding self on update)
    boolean existsByNameAndVolumeMlAndIdNot(String name, Integer volumeMl, Long id);

}