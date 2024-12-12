package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.BarberServiceModel;

@Repository
public interface ServiceRepository extends JpaRepository<BarberServiceModel, Long> {
}
