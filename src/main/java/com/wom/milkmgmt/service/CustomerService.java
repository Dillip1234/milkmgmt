package com.wom.milkmgmt.service;

import com.wom.milkmgmt.dto.CustomerRequestDTO;
import com.wom.milkmgmt.dto.CustomerResponseDTO;

import com.wom.milkmgmt.entity.Customer;
import com.wom.milkmgmt.entity.District;
import com.wom.milkmgmt.entity.MilkType;
import com.wom.milkmgmt.entity.User;
import com.wom.milkmgmt.repository.CustomerRepository;
import com.wom.milkmgmt.repository.DistrictRepository;
import com.wom.milkmgmt.repository.MilkTypeRepository;
import com.wom.milkmgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    // Repositories for lookups
    private final DistrictRepository districtRepository;
    private final MilkTypeRepository milkTypeRepository;
    private final UserRepository userRepository;

    // ── CREATE ──────────────────────────────────────────────────────────────
    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {
        if (customerRepository.existsByMobileNumber(dto.getMobileNumber())) {
            throw new IllegalArgumentException(
                    "Mobile number already registered: " + dto.getMobileNumber());
        }

        District district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() -> new EntityNotFoundException("District not found: " + dto.getDistrictId()));

        MilkType milkType = milkTypeRepository.findById(dto.getMilkTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Milk type not found: " + dto.getMilkTypeId()));

        User deliveryPerson = userRepository.findById(dto.getDeliveryPersonId())
                .orElseThrow(() -> new EntityNotFoundException("Delivery person not found: " + dto.getDeliveryPersonId()));

        Customer customer = Customer.builder()
                .customerName(dto.getCustomerName())
                .mobileNumber(dto.getMobileNumber())
                .address(dto.getAddress())
                .district(district)
                .milkType(milkType)
                .regularQuantity(dto.getRegularQuantity())
                .deliveryPerson(deliveryPerson)
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .active(true)
                .build();

        return toDTO(customerRepository.save(customer));
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────
    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));

        if (customerRepository.existsByMobileNumberAndIdNot(dto.getMobileNumber(), id)) {
            throw new IllegalArgumentException(
                    "Mobile number already in use: " + dto.getMobileNumber());
        }

        District district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() -> new EntityNotFoundException("District not found: " + dto.getDistrictId()));

        MilkType milkType = milkTypeRepository.findById(dto.getMilkTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Milk type not found: " + dto.getMilkTypeId()));

        User deliveryPerson = userRepository.findById(dto.getDeliveryPersonId())
                .orElseThrow(() -> new EntityNotFoundException("Delivery person not found: " + dto.getDeliveryPersonId()));

        customer.setCustomerName(dto.getCustomerName());
        customer.setMobileNumber(dto.getMobileNumber());
        customer.setAddress(dto.getAddress());
        customer.setDistrict(district);
        customer.setMilkType(milkType);
        customer.setRegularQuantity(dto.getRegularQuantity());
        customer.setDeliveryPerson(deliveryPerson);

        return toDTO(customerRepository.save(customer));
    }

    // ── SOFT DELETE ─────────────────────────────────────────────────────────
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
        customer.setActive(false);          // soft delete
        customerRepository.save(customer);
    }

    // ── GET BY DELIVERY PERSON ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getCustomersByDeliveryPerson(Long deliveryPersonId) {
        // Verify delivery person exists
        userRepository.findById(deliveryPersonId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery person not found: " + deliveryPersonId));

        return customerRepository.findByDeliveryPersonId(deliveryPersonId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(Long id) {
        return customerRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    }

    // ── MAPPER ───────────────────────────────────────────────────────────────
    private CustomerResponseDTO toDTO(Customer c) {
        return CustomerResponseDTO.builder()
                .id(c.getId())
                .customerName(c.getCustomerName())
                .mobileNumber(c.getMobileNumber())
                .address(c.getAddress())
                .districtId(c.getDistrict().getId())
                .districtName(c.getDistrict().getName())
                .milkTypeId(c.getMilkType().getId())
                .milkTypeName(c.getMilkType().getName())
                .regularQuantity(c.getRegularQuantity())
                .deliveryPersonId(c.getDeliveryPerson().getId())
                .deliveryPersonName(c.getDeliveryPerson().getUsername())
                .createdAt(c.getCreatedAt())
                .active(c.getActive())
                .build();
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    private CustomerResponseDTO mapToResponseDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .customerName(customer.getCustomerName())
                .mobileNumber(customer.getMobileNumber())
                .address(customer.getAddress())
                .districtId(customer.getDistrict().getId())
                .districtName(customer.getDistrict().getName())
                .milkTypeId(customer.getMilkType().getId())
                .milkTypeName(customer.getMilkType().getName())
                .regularQuantity(customer.getRegularQuantity())
                .deliveryPersonId(customer.getDeliveryPerson().getId())
                .deliveryPersonName(customer.getDeliveryPerson().getUsername())
                .createdAt(customer.getCreatedAt())
                .active(customer.getActive())
                .build();
    }
}
