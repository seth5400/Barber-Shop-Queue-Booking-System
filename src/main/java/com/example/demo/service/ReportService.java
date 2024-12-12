package com.example.demo.service;

import com.example.demo.model.Appointment;
import com.example.demo.model.Barber;
import com.example.demo.model.User;
import com.example.demo.model.BarberServiceModel;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.BarberRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    public double calculateTotalEarnings() {
        // กำหนดสถานะที่ต้องการนับรวมในรายได้
        List<String> validStatuses = Arrays.asList("Accepted", "Completed");
        
        // ดึงการนัดหมายที่มีสถานะที่ต้องการจากฐานข้อมูล
        List<Appointment> appointments = appointmentRepository.findByStatusIn(validStatuses);
        
        // คำนวณรายได้รวม
        return appointments.stream()
                .mapToDouble(app -> app.getBarberService().getServicePrice())
                .sum();
    }

    public Map<BarberServiceModel, Long> getTopServices() {
        List<String> validStatuses = Arrays.asList("Accepted", "Completed");
        List<Appointment> appointments = appointmentRepository.findByStatusIn(validStatuses);

        return appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getBarberService, Collectors.counting()));
    }

    public Map<Barber, Long> getTopBarbers() {
        List<String> validStatuses = Arrays.asList("Accepted", "Completed");
        List<Appointment> appointments = appointmentRepository.findByStatusIn(validStatuses);

        return appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getBarber, Collectors.counting()));
    }

    public Map<User, Long> getTopUsers() {
        List<String> validStatuses = Arrays.asList("Accepted", "Completed");
        List<Appointment> appointments = appointmentRepository.findByStatusIn(validStatuses);

        return appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getUser, Collectors.counting()));
    }

    // ดึงรายการการจองทั้งหมด
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}