package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.Appointment;
import com.example.demo.model.User;
import com.example.demo.model.Barber;
import com.example.demo.model.BarberServiceModel;
import com.example.demo.repository.AppointmentRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment saveAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByUser(User user) {
        return appointmentRepository.findByUser(user);
    }

    public List<Appointment> getAppointmentsByBarber(Barber barber) {
        return appointmentRepository.findByBarber(barber);
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }
    public long countAppointmentsByBarber(Barber barber) {
        return appointmentRepository.countByBarber(barber);
    }

    public long countPendingAppointmentsByBarber(Barber barber) {
        return appointmentRepository.countByBarberAndStatus(barber, "Pending");
    }

    public long countCancelledAppointmentsByBarber(Barber barber) {
        return appointmentRepository.countByBarberAndStatus(barber, "Cancelled");
    }
    // เพิ่มเมธอดเพื่อดึงข้อมูลการจองตามวันที่
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByDate(date);
    }
    
    // เมธอดสำหรับสร้างและบันทึกนัดหมายใหม่
    public Appointment createAppointment(User user, Barber barber, String date, String time, BarberServiceModel service) {
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setBarber(barber);
        appointment.setDate(LocalDate.parse(date)); // แปลงจาก String เป็น LocalDate
        appointment.setTime(LocalTime.parse(time)); // แปลงจาก String เป็น LocalTime
        appointment.setBarberService(service);
        appointment.setStatus("Pending"); // ตั้งค่าสถานะเริ่มต้น
        return appointmentRepository.save(appointment); // บันทึกนัดหมาย
    }
    
    public long countCompletedAppointmentsByBarber(Barber barber) {
        return appointmentRepository.countByBarberAndStatus(barber, "Accepted");
    }
    
 // นับจำนวนการนัดหมายที่ถูก Accepted โดย Barber
    public long countAcceptedAppointmentsByBarber(Barber barber) {
        return appointmentRepository.countByBarberAndStatus(barber, "Accepted");
    }
    
 // เมธอดเพื่อคำนวณราคารวมของนัดหมายที่ Accepted
    public Double getTotalEarningsByBarber(Barber barber) {
        Double totalEarnings = appointmentRepository.findTotalEarningsByBarberAndStatusAccepted(barber);
        return totalEarnings != null ? totalEarnings : 0.0; // ถ้าไม่มีรายการให้คืนค่า 0
    }
    
    //total price baber home
    public double calculateTotalEarningsByBarber(Barber barber) {
        List<Appointment> acceptedAppointments = appointmentRepository.findByBarberAndStatus(barber, "Accepted");
        // ใช้ Stream เพื่อรวมราคาของการนัดหมายที่ Accepted
        return acceptedAppointments.stream()
            .mapToDouble(appointment -> appointment.getBarberService().getServicePrice())
            .sum();
    }
    
 
    
    
    
    
}
