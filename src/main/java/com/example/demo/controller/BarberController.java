package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.Barber;
import com.example.demo.service.AppointmentService;
import com.example.demo.service.BarberService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BarberController {

    @Autowired
    private BarberService barberService;
    
    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/barber/home")
    public String showBarberHome(HttpSession session, Model model) {
        Barber barber = (Barber) session.getAttribute("barber");
        if (barber != null) {
            model.addAttribute("barber", barber);
            
            // ดึงข้อมูลราคารวมของนัดหมายที่ถูก Accepted
            Double totalEarnings = appointmentService.getTotalEarningsByBarber(barber);
            model.addAttribute("totalEarnings", totalEarnings);
            
            return "barber_home";
        }
        return "redirect:/login";
    }

    

    @GetMapping("/barber/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    //สำหรับ baber กดว่างไม่ว่างในหน้า home baber
    @PostMapping("/toggle-availability")
    public String toggleAvailability(HttpSession session, Model model) {
        Barber barber = (Barber) session.getAttribute("barber");
        if (barber != null) {
            barberService.toggleAvailability(barber); // สลับสถานะว่าง/ไม่ว่าง
            session.setAttribute("barber", barber); // อัปเดตข้อมูลใน session
        }
        return "redirect:/home";
    }
    
    // แสดงหน้าประวัติการนัดหมาย
    @GetMapping("/barber/history")
    public String viewHistory(HttpSession session, Model model) {
        Barber barber = (Barber) session.getAttribute("barber");
        if (barber != null) {
            // ดึงข้อมูลการนัดหมายทั้งหมดที่ช่างคนนี้เคยทำ
            model.addAttribute("appointments", appointmentService.getAppointmentsByBarber(barber));
            return "barber_history"; // ชื่อไฟล์ HTML ที่จะแสดง
        }
        return "redirect:/login";
    }
    
    
    // ฟังก์ชันสำหรับดึงข้อมูลการนัดหมายที่ยอมรับแล้ว
    @GetMapping("/barber/completed-orders")
    @ResponseBody
    public long getCompletedOrders(HttpSession session) {
        Barber barber = (Barber) session.getAttribute("barber");
        if (barber != null) {
            return appointmentService.countCompletedAppointmentsByBarber(barber);
        }
        return 0;
    }
    
    @GetMapping("/barber/accepted-orders")
    @ResponseBody
    public long getAcceptedOrders(HttpSession session) {
        Barber barber = (Barber) session.getAttribute("barber");
        if (barber != null) {
            return appointmentService.countAcceptedAppointmentsByBarber(barber);
        }
        return 0;
    }
    
    @GetMapping("/barber/total-earnings")
    @ResponseBody
    public double getTotalEarnings(HttpSession session) {
        Barber barber = (Barber) session.getAttribute("barber");
        if (barber != null) {
            // เรียกใช้ appointmentService เพื่อหาผลรวมราคาเฉพาะที่ Accepted
            return appointmentService.calculateTotalEarningsByBarber(barber);
        }
        return 0;
    }
    
    @GetMapping("/barber/my-pending-orders")
    @ResponseBody
    public long getPendingOrders(HttpSession session) {
        Barber barber = (Barber) session.getAttribute("barber");
        if (barber != null) {
            return appointmentService.countPendingAppointmentsByBarber(barber);
        }
        return 0;
    }
    
    // Method to show barber's schedule
    @GetMapping("/barber/schedule")
    public String showBarberSchedule() {
        return "baber_schedule"; // ชื่อไฟล์ HTML ที่อยู่ใน templates เช่น baber_schedule.html
    }
    
    @GetMapping("/admin/barberstatus")
    public String showBarberStatus(Model model) {
        List<Barber> barbers = barberService.findAllBarbers();  // ดึงข้อมูลช่างตัดผมทั้งหมด
        model.addAttribute("barbers", barbers);  // ส่งข้อมูลไปยัง view
        return "barber_status";  // ชื่อของไฟล์ HTML ที่จะใช้
    }
    
    
    
}