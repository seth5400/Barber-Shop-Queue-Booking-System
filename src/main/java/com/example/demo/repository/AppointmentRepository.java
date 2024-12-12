package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Appointment;
import com.example.demo.model.User;
import com.example.demo.model.Barber;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUser(User user);

    List<Appointment> findByBarber(Barber barber);
    
    List<Appointment> findByStatusIn(List<String> statuses);

    List<Appointment> findByBarberAndDate(Barber barber, LocalDate date);
 // นับการนัดหมายทั้งหมดของช่าง
    
 // เพิ่มเมธอดเพื่อดึงข้อมูลการจองตามวันที่
    List<Appointment> findByDate(LocalDate date);
    
    long countByBarber(Barber barber);
    
    // นับการนัดหมายตามสถานะของช่าง
    long countByBarberAndStatus(Barber barber, String status);
    
    
    // เมธอดสำหรับดึงราคารวมของนัดหมายที่มีสถานะ Accepted
    @Query("SELECT SUM(a.barberService.servicePrice) FROM Appointment a WHERE a.barber = :barber AND a.status = 'Accepted'")
    Double findTotalEarningsByBarberAndStatusAccepted(@Param("barber") Barber barber);
    
    List<Appointment> findByBarberAndStatus(Barber barber, String status);
    
 
    

}