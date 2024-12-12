package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.example.demo.model.Admin;
import com.example.demo.model.BarberServiceModel;
import com.example.demo.service.AdminService;
import com.example.demo.service.BarberServiceModelService;

@SpringBootApplication
public class ProjectHairKVersion3Application implements CommandLineRunner {

    @Autowired
    private AdminService adminService;

    @Autowired
    private BarberServiceModelService serviceModelService;

    public static void main(String[] args) {
        SpringApplication.run(ProjectHairKVersion3Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // ตรวจสอบว่ามีข้อมูลแอดมินอยู่แล้วหรือไม่
        if (adminService.findByEmail("admin@example.com").isEmpty()) {
            // หากไม่มี ให้สร้างแอดมินใหม่ขึ้นมา
            Admin admin = new Admin();
            admin.setEmail("admin@example.com");
            admin.setName("Admin");
            admin.setPassword("admin123"); // คุณสามารถกำหนดรหัสผ่านเป็น hashed ได้
            adminService.saveAdmin(admin);
            
            System.out.println("Admin account created with email: admin@example.com and password: admin123");
        } else {
            System.out.println("Admin account already exists.");
        }

        // ตรวจสอบว่ามีข้อมูลใน services อยู่แล้วหรือไม่
        if (serviceModelService.findAllServices().isEmpty()) {
            // หากไม่มี ให้ insert ข้อมูลเข้าไปในตาราง services
            insertDefaultServices();
        }
    }

    private void insertDefaultServices() {
        BarberServiceModel service1 = new BarberServiceModel();
        service1.setServiceName("Haircut");
        service1.setServicePrice(15.00);
        serviceModelService.saveService(service1);

        BarberServiceModel service2 = new BarberServiceModel();
        service2.setServiceName("Beard Trim");
        service2.setServicePrice(10.00);
        serviceModelService.saveService(service2);

        BarberServiceModel service3 = new BarberServiceModel();
        service3.setServiceName("Shave");
        service3.setServicePrice(12.00);
        serviceModelService.saveService(service3);

        BarberServiceModel service4 = new BarberServiceModel();
        service4.setServiceName("Hair Wash");
        service4.setServicePrice(8.00);
        serviceModelService.saveService(service4);

        BarberServiceModel service5 = new BarberServiceModel();
        service5.setServiceName("Hair Color");
        service5.setServicePrice(25.00);
        serviceModelService.saveService(service5);

        System.out.println("Default services inserted into database.");
    }
}
