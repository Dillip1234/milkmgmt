package com.wom.milkmgmt.controller;

import com.wom.milkmgmt.model.MasterDataResponse;
import com.wom.milkmgmt.service.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/master")
public class MasterController {

    @Autowired
    private MasterService masterService;

    @GetMapping("/master-data")
    public MasterDataResponse getMasterData(){
        return masterService.getMasterData();
    }
}
