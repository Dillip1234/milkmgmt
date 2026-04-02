package com.wom.milkmgmt.controller;

import com.wom.milkmgmt.dto.MilkTypeRequestDTO;
import com.wom.milkmgmt.dto.MilkTypeResponseDTO;
import com.wom.milkmgmt.service.MilkTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milk-types")
@RequiredArgsConstructor
public class MilkTypeController {

    private final MilkTypeService milkTypeService;

    // POST /api/milk-types
    @PostMapping("/register")
    public ResponseEntity<MilkTypeResponseDTO> createMilkType(
            @RequestBody MilkTypeRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(milkTypeService.createMilkType(dto));
    }

    // PUT /api/milk-types/{id}
    @PutMapping("/{id}")
    public ResponseEntity<MilkTypeResponseDTO> updateMilkType(
            @PathVariable Long id,
            @RequestBody MilkTypeRequestDTO dto) {
        return ResponseEntity.ok(milkTypeService.updateMilkType(id, dto));
    }

    // DELETE /api/milk-types/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMilkType(@PathVariable Long id) {
        milkTypeService.deleteMilkType(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/milk-types
    @GetMapping
    public ResponseEntity<List<MilkTypeResponseDTO>> getAllMilkTypes() {
        return ResponseEntity.ok(milkTypeService.getAllActiveMilkTypes());
    }

    // GET /api/milk-types/all  (includes inactive)
    @GetMapping("/all")
    public ResponseEntity<List<MilkTypeResponseDTO>> getAllIncludingInactive() {
        return ResponseEntity.ok(milkTypeService.getAllMilkTypes());
    }

    // GET /api/milk-types/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MilkTypeResponseDTO> getMilkTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(milkTypeService.getMilkTypeById(id));
    }
}