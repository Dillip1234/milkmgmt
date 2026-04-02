package com.wom.milkmgmt.model;

import com.wom.milkmgmt.entity.MilkType;
import com.wom.milkmgmt.entity.Role;
import lombok.Data;

import java.util.List;

@Data
public class MasterDataResponse {

    private List<Role> roles;
    private List<MilkType> milkTypes;

    // getters and setters
}
