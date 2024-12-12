package com.example.demo.service;

import com.example.demo.model.BarberServiceModel;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BarberServiceModelService {

    @Autowired
    private ServiceRepository serviceRepository;

    public List<BarberServiceModel> findAllServices() {
        return serviceRepository.findAll();
    }

    public Optional<BarberServiceModel> findById(Long id) {
        return serviceRepository.findById(id);
    }

    public void saveService(BarberServiceModel barberServiceModel) {
        serviceRepository.save(barberServiceModel);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }
}
