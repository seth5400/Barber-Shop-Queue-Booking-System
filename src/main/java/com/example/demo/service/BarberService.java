package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.Barber;
import com.example.demo.repository.BarberRepository;

@Service
public class BarberService {

    @Autowired
    private BarberRepository barberRepository;

    public Barber saveBarber(Barber barber) {
        return barberRepository.save(barber);
    }

    public Optional<Barber> findById(Long id) {
        return barberRepository.findById(id);
    }

    public Optional<Barber> findByEmail(String email) {
        return barberRepository.findByEmail(email);
    }

    public List<Barber> findAllBarbers() {
        return barberRepository.findAll();
    }

    public void deleteBarber(Long id) {
        barberRepository.deleteById(id);
    }
 // เมธอดสำหรับเปลี่ยนสถานะ ว่าง/ไม่ว่าง
    public void toggleAvailability(Barber barber) {
        barber.setAvailable(!barber.isAvailable()); // สลับสถานะ ว่าง/ไม่ว่าง
        barberRepository.save(barber);
    }
}
