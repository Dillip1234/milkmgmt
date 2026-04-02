package com.wom.milkmgmt.service;

import com.wom.milkmgmt.model.MasterDataResponse;
import com.wom.milkmgmt.repository.MilkTypeRepository;
import com.wom.milkmgmt.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MilkTypeRepository milkTypeRepository;

    public MasterDataResponse getMasterData(){

        MasterDataResponse response = new MasterDataResponse();

        response.setRoles(roleRepository.findAll());
        response.setMilkTypes(milkTypeRepository.findAll());

        return response;
    }
}
