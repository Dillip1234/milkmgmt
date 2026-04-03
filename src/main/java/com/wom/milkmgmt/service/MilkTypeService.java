package com.wom.milkmgmt.service;

import com.wom.milkmgmt.dto.MilkTypeRequestDTO;
import com.wom.milkmgmt.dto.MilkTypeResponseDTO;
import com.wom.milkmgmt.entity.MilkType;
import com.wom.milkmgmt.repository.MilkTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilkTypeService {

    private final MilkTypeRepository milkTypeRepository;

    // ── CREATE ──────────────────────────────────────────────────────────────
    @Transactional
    public MilkTypeResponseDTO createMilkType(MilkTypeRequestDTO dto) {
        if (milkTypeRepository.existsByNameAndVolumeMl(dto.getName(), dto.getVolumeMl())) {
            throw new IllegalArgumentException(
                    "Milk type already exists: " + dto.getName() + " " + dto.getVolumeMl() + "ml");
        }

        MilkType milkType = MilkType.builder()
                .name(dto.getName())
               // .animal(dto.getAnimal())
                .volumeMl(dto.getVolumeMl())
                .pricePerUnit(dto.getPricePerUnit())
                .active(true)
                .build();

        return toDTO(milkTypeRepository.save(milkType));
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────
    @Transactional
    public MilkTypeResponseDTO updateMilkType(Long id, MilkTypeRequestDTO dto) {
        MilkType milkType = milkTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Milk type not found: " + id));

        if (milkTypeRepository.existsByNameAndVolumeMlAndIdNot(dto.getName(), dto.getVolumeMl(), id)) {
            throw new IllegalArgumentException(
                    "Milk type already exists: " + dto.getName() + " " + dto.getVolumeMl() + "ml");
        }

        milkType.setName(dto.getName());
       // milkType.setAnimal(dto.getAnimal());
        milkType.setVolumeMl(dto.getVolumeMl());
        milkType.setPricePerUnit(dto.getPricePerUnit());

        return toDTO(milkTypeRepository.save(milkType));
    }

    // ── SOFT DELETE ─────────────────────────────────────────────────────────
    @Transactional
    public void deleteMilkType(Long id) {
        MilkType milkType = milkTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Milk type not found: " + id));
        milkType.setActive(false);
        milkTypeRepository.save(milkType);
    }

    // ── GET ALL ACTIVE ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MilkTypeResponseDTO> getAllActiveMilkTypes() {
        return milkTypeRepository.findByActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MilkTypeResponseDTO> getAllMilkTypes() {
        return milkTypeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public MilkTypeResponseDTO getMilkTypeById(Long id) {
        return milkTypeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Milk type not found: " + id));
    }

    // ── MAPPER ───────────────────────────────────────────────────────────────
    private MilkTypeResponseDTO toDTO(MilkType m) {
        return MilkTypeResponseDTO.builder()
                .id(m.getId())
                .name(m.getName())
                //.animal(m.getAnimal())
                .volumeMl(m.getVolumeMl())
                .pricePerUnit(m.getPricePerUnit())
                .active(m.getActive())
                .build();
    }
}