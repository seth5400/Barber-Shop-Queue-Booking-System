package com.example.demo.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Admin;
import com.example.demo.model.Barber;
import com.example.demo.model.User;
import com.example.demo.model.Appointment;
import com.example.demo.model.BarberServiceModel;
import com.example.demo.service.AdminService;
import com.example.demo.service.BarberService;
import com.example.demo.service.UserService;
import com.example.demo.service.ReportService;
import com.example.demo.service.SettingService;
import com.example.demo.service.BarberServiceModelService;

import jakarta.servlet.http.HttpSession;


@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private BarberService barberService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private BarberServiceModelService serviceModelService;

    // ส่วนของ AdminController ดั้งเดิม
    @GetMapping("/admin/home")
    public String showAdminHome(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin != null) {
            model.addAttribute("admin", admin);
            return "admin_home";
        }
        return "redirect:/login";
    }

    @GetMapping("/admin/manage-users")
    public String manageUsers(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin != null) {
            List<User> users = userService.findAllUsers();
            List<Barber> barbers = barberService.findAllBarbers();
            List<Admin> admins = adminService.findAllAdmins();

            // ตรวจสอบบทบาทของแต่ละประเภทและตั้งค่า role
            users.forEach(user -> user.setRole("user"));
            barbers.forEach(barber -> barber.setRole("barber"));
            admins.forEach(adminItem -> adminItem.setRole("admin"));

            // รวมผู้ใช้ทั้งหมดใน model
            model.addAttribute("users", Stream.of(users, barbers, admins)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

            return "manage_users";
        }
        return "redirect:/login";
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam("userId") Long userId,
                             @RequestParam("role") String role,
                             HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin != null) {
            switch(role) {
                case "user":
                    userService.deleteUser(userId);
                    break;
                case "barber":
                    barberService.deleteBarber(userId);
                    break;
                case "admin":
                    adminService.deleteAdmin(userId);
                    break;
                default:
                    // จัดการกรณีที่ role ไม่ถูกต้อง
                    break;
            }
            return "redirect:/admin/manage-users";
        }
        return "redirect:/login";
    }

    @PostMapping("/update-role")
    public String updateUserRole(@RequestParam("userId") Long userId, @RequestParam("role") String role, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin != null) {
            Optional<User> userOpt = userService.findById(userId);
            Optional<Barber> barberOpt = barberService.findById(userId);
            Optional<Admin> adminOpt = adminService.findById(userId);

            // Logic สำหรับการย้ายบทบาทของผู้ใช้
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (role.equals("barber")) {
                    Barber newBarber = new Barber();
                    newBarber.setName(user.getName());
                    newBarber.setEmail(user.getEmail());
                    newBarber.setPassword(user.getPassword());
                    newBarber.setRole("barber");
                    barberService.saveBarber(newBarber);
                    userService.deleteUser(userId);
                } else if (role.equals("admin")) {
                    Admin newAdmin = new Admin();
                    newAdmin.setName(user.getName());
                    newAdmin.setEmail(user.getEmail());
                    newAdmin.setPassword(user.getPassword());
                    newAdmin.setRole("admin");
                    adminService.saveAdmin(newAdmin);
                    userService.deleteUser(userId);
                } else {
                    user.setRole("user");
                    userService.saveUser(user);
                }
            } else if (barberOpt.isPresent()) {
                Barber barber = barberOpt.get();
                if (role.equals("user")) {
                    User newUser = new User();
                    newUser.setName(barber.getName());
                    newUser.setEmail(barber.getEmail());
                    newUser.setPassword(barber.getPassword());
                    newUser.setRole("user");
                    userService.saveUser(newUser);
                    barberService.deleteBarber(userId);
                } else if (role.equals("admin")) {
                    Admin newAdmin = new Admin();
                    newAdmin.setName(barber.getName());
                    newAdmin.setEmail(barber.getEmail());
                    newAdmin.setPassword(barber.getPassword());
                    newAdmin.setRole("admin");
                    adminService.saveAdmin(newAdmin);
                    barberService.deleteBarber(userId);
                } else {
                    barber.setRole("barber");
                    barberService.saveBarber(barber);
                }
            } else if (adminOpt.isPresent()) {
                Admin existingAdmin = adminOpt.get();
                if (role.equals("user")) {
                    User newUser = new User();
                    newUser.setName(existingAdmin.getName());
                    newUser.setEmail(existingAdmin.getEmail());
                    newUser.setPassword(existingAdmin.getPassword());
                    newUser.setRole("user");
                    userService.saveUser(newUser);
                    adminService.deleteAdmin(userId);
                } else if (role.equals("barber")) {
                    Barber newBarber = new Barber();
                    newBarber.setName(existingAdmin.getName());
                    newBarber.setEmail(existingAdmin.getEmail());
                    newBarber.setPassword(existingAdmin.getPassword());
                    newBarber.setRole("barber");
                    barberService.saveBarber(newBarber);
                    adminService.deleteAdmin(userId);
                } else {
                    existingAdmin.setRole("admin");
                    adminService.saveAdmin(existingAdmin);
                }
            }

            return "redirect:/admin/manage-users";
        }
        return "redirect:/login";
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ส่วนของ AdminReportController
    @GetMapping("/admin/reports")
    public String showReports(Model model) {
        double totalEarnings = reportService.calculateTotalEarnings();
        Map<BarberServiceModel, Long> topServices = reportService.getTopServices();
        Map<Barber, Long> topBarbers = reportService.getTopBarbers();
        Map<User, Long> topUsers = reportService.getTopUsers();
        List<Appointment> allAppointments = reportService.getAllAppointments();

        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("topServices", topServices);
        model.addAttribute("topBarbers", topBarbers);
        model.addAttribute("topUsers", topUsers);
        model.addAttribute("allAppointments", allAppointments);

        return "admin_reports";
    }

    // ส่วนของ AdminSettingController
    @GetMapping("/admin/settings")
    public String showSettings(Model model) {
        boolean isBookingEnabled = settingService.isBookingEnabled();
        model.addAttribute("isBookingEnabled", isBookingEnabled);
        return "admin_setting";
    }

    @PostMapping("/admin/settings")
    public String saveSettings(@RequestParam(value = "toggleBooking", required = false) boolean toggleBooking, Model model) {
        settingService.saveSettings(toggleBooking);
        model.addAttribute("successMessage", "Settings saved successfully!");
        model.addAttribute("isBookingEnabled", toggleBooking);
        return "admin_setting";
    }

    // ส่วนของ ServiceController
    @GetMapping("/admin/services")
    public String showServiceManagementPage(Model model) {
        List<BarberServiceModel> services = serviceModelService.findAllServices();
        model.addAttribute("services", services);
        return "manage_services";
    }

    @PostMapping("/admin/services/add")
    public String addService(@RequestParam String serviceName, @RequestParam Double servicePrice) {
        BarberServiceModel service = new BarberServiceModel();
        service.setServiceName(serviceName);
        service.setServicePrice(servicePrice);
        serviceModelService.saveService(service);
        return "redirect:/admin/services";
    }

    @PostMapping("/admin/services/edit")
    public String editService(@RequestParam Long serviceId, @RequestParam String serviceName, @RequestParam Double servicePrice) {
        BarberServiceModel service = serviceModelService.findById(serviceId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid service ID"));
        service.setServiceName(serviceName);
        service.setServicePrice(servicePrice);
        serviceModelService.saveService(service);
        return "redirect:/admin/services";
    }

    @PostMapping("/admin/services/delete")
    public String deleteService(@RequestParam Long serviceId) {
        serviceModelService.deleteService(serviceId);
        return "redirect:/admin/services";
    }
}
