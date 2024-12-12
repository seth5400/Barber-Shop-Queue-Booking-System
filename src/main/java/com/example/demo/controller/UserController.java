package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.User;
import com.example.demo.model.Barber;
import com.example.demo.model.Admin;
import com.example.demo.model.Appointment;
import com.example.demo.service.UserService;
import com.example.demo.service.BarberService;
import com.example.demo.service.AdminService;
import com.example.demo.service.AppointmentService;
import com.example.demo.service.SettingService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BarberService barberService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private SettingService settingService; // เพิ่ม SettingService

    // แสดงหน้า login
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // แสดงหน้า register
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // ลงทะเบียนผู้ใช้ใหม่ พร้อมตรวจสอบบทบาท
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                               @RequestParam("role") String role,
                               Model model) {
        if (role.equals("user")) {
            userService.saveUser(user);
        } else if (role.equals("barber")) {
            Barber barber = new Barber();
            barber.setName(user.getName());
            barber.setEmail(user.getEmail());
            barber.setPassword(user.getPassword());
            barberService.saveBarber(barber);
        } else if (role.equals("admin")) {
            Admin admin = new Admin();
            admin.setName(user.getName());
            admin.setEmail(user.getEmail());
            admin.setPassword(user.getPassword());
            adminService.saveAdmin(admin);
        } else {
            model.addAttribute("error", "Invalid role selected");
            return "register";
        }
        return "redirect:/login";
    }

    // ล็อกอินผู้ใช้ตามบทบาท
    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        Optional<User> userOpt = userService.findByEmail(email);
        Optional<Barber> barberOpt = barberService.findByEmail(email);
        Optional<Admin> adminOpt = adminService.findByEmail(email);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("user", userOpt.get());
            return "redirect:/home";
        }

        if (barberOpt.isPresent() && barberOpt.get().getPassword().equals(password)) {
            session.setAttribute("barber", barberOpt.get());
            return "redirect:/home";
        }

        if (adminOpt.isPresent() && adminOpt.get().getPassword().equals(password)) {
            session.setAttribute("admin", adminOpt.get());
            return "redirect:/home";
        }

        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    // หน้า home ที่ตรวจสอบบทบาทและนำไปสู่หน้า home ของแต่ละ role
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        Barber barber = (Barber) session.getAttribute("barber");
        Admin admin = (Admin) session.getAttribute("admin");

        boolean isBookingEnabled = settingService.isBookingEnabled();  // ดึงค่าการตั้งค่า

        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("isBookingEnabled", isBookingEnabled);  // ส่งค่าการตั้งค่าไปยัง view
            return "user_home";
        } else if (barber != null) {
            model.addAttribute("barber", barber);
            return "barber_home";
        } else if (admin != null) {
            model.addAttribute("admin", admin);
            return "admin_home";
        }

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/user/history")
    public String viewUserHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("historyAppointments", appointmentService.getAppointmentsByUser(user));
            return "user_history";
        }
        return "redirect:/login";
    }

    @GetMapping("/user/edit")
    public String showEditProfilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            return "edit_profile";
        }
        return "redirect:/login";
    }

    @PostMapping("/user/update")
    public String updateUserProfile(@RequestParam("id") Long userId,
                                    @ModelAttribute User user,
                                    @RequestParam("password") String newPassword,
                                    @RequestParam("confirmPassword") String confirmPassword,
                                    HttpSession session,
                                    Model model) {

        if (userId == null) {
            model.addAttribute("error", "User ID is missing.");
            return "edit_profile";
        }

        if (user.getName() == null || user.getEmail() == null || newPassword == null || confirmPassword == null) {
            model.addAttribute("error", "กรุณากรอกข้อมูลให้ครบทุกช่อง");
            return "edit_profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "รหัสผ่านและยืนยันรหัสผ่านไม่ตรงกัน");
            return "edit_profile";
        }

        Optional<User> existingUserOpt = userService.findById(userId);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());

            // ตรวจสอบว่าผู้ใช้ต้องการเปลี่ยนรหัสผ่านหรือไม่
            if (newPassword != null && !newPassword.isEmpty()) {
                existingUser.setPassword(newPassword);
            }

            userService.updateUser(existingUser);
            session.setAttribute("user", existingUser); // อัปเดตข้อมูลใน session
        }
        return "redirect:/user/profile"; // นำไปที่หน้าโปรไฟล์หลังจากอัปเดต
    }



    
    @GetMapping("/user/profile")
    public String showUserProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            return "user_profile";
        }
        return "redirect:/login";
    }

}
