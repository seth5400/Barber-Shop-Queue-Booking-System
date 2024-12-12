package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.Appointment;
import com.example.demo.model.Barber;
import com.example.demo.model.BarberServiceModel;
import com.example.demo.model.User;
import com.example.demo.service.AppointmentService;
import com.example.demo.service.BarberService;
import com.example.demo.service.BarberServiceModelService;
import com.example.demo.service.SettingService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AppointmentController {

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private BarberService barberService;

	@Autowired
	private SettingService settingService;

	@Autowired
	private BarberServiceModelService barberServiceModelService;

	@GetMapping("/appointments")
	public String viewAppointments(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		Barber barber = (Barber) session.getAttribute("barber");

		if (user != null) {
			model.addAttribute("appointments", appointmentService.getAppointmentsByUser(user));
			return "user_appointments";
		} else if (barber != null) {
			model.addAttribute("appointments", appointmentService.getAppointmentsByBarber(barber));
			return "barber_appointments";
		}
		return "redirect:/login";
	}

	@GetMapping("/appointment/book")
	public String bookAppointmentPage(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");

		if (user != null) {
			// เพิ่มการตรวจสอบสถานะการจอง
			boolean isBookingEnabled = settingService.isBookingEnabled();
			if (!isBookingEnabled) {
				model.addAttribute("error", "การจองถูกปิดใช้งานโดยผู้ดูแลระบบ");
				return "user_home"; // หรือคุณสามารถสร้างหน้าแจ้งเตือนเฉพาะก็ได้
			}

			// แสดงเฉพาะช่างที่ว่างเท่านั้น
			List<Barber> barbers = barberService.findAllBarbers().stream().filter(Barber::isAvailable)
					.collect(Collectors.toList());
			List<BarberServiceModel> services = barberServiceModelService.findAllServices();
			model.addAttribute("barbers", barbers);
			model.addAttribute("services", services);
			return "book_appointment";
		}
		return "redirect:/login";
	}

	@PostMapping("/appointment/book")
	public String bookAppointment(@RequestParam("date") LocalDate date, @RequestParam("time") LocalTime time,
			@RequestParam("barberId") Long barberId, @RequestParam("serviceId") Long serviceId, HttpSession session,
			Model model) {
		// เพิ่มการตรวจสอบสถานะการจอง
		boolean isBookingEnabled = settingService.isBookingEnabled();
		if (!isBookingEnabled) {
			model.addAttribute("error", "การจองถูกปิดใช้งานโดยผู้ดูแลระบบ");
			return "user_home"; // หรือคุณสามารถสร้างหน้าแจ้งเตือนเฉพาะก็ได้
		}

		LocalDate today = LocalDate.now();
		LocalTime startTime = LocalTime.of(9, 0); // 9:00 น.
		LocalTime endTime = LocalTime.of(16, 0); // 16:00 น.

		// ตรวจสอบวันที่ย้อนหลัง
		if (date.isBefore(today)) {
			model.addAttribute("error", "ไม่สามารถจองวันที่ย้อนหลังได้");
			return "appointment_form"; // กลับไปที่ฟอร์มจอง
		}

		// ตรวจสอบเวลาย้อนหลังของวันนี้
		if (date.isEqual(today) && time.isBefore(LocalTime.now())) {
			model.addAttribute("error", "ไม่สามารถจองเวลาที่ย้อนหลังในวันนี้ได้");
			return "appointment_form"; // กลับไปที่ฟอร์มจอง
		}

		// ตรวจสอบเวลาที่อยู่ในช่วงเวลาที่กำหนด (9:00 น. - 16:00 น.)
		if (time.isBefore(startTime) || time.isAfter(endTime)) {
			model.addAttribute("error", "You can only make reservations between 9:00 a.m. and 4:00 p.m.");
			return "appointment_form"; // กลับไปที่ฟอร์มจอง
		}

		// ตรวจสอบว่ามี barber ที่ถูกเลือกหรือไม่
		Optional<Barber> barberOpt = barberService.findById(barberId);
		if (barberOpt.isEmpty()) {
			model.addAttribute("error", "ไม่พบช่างตัดผมที่เลือก");
			return "appointment_form"; // กลับไปที่ฟอร์มจอง
		}

		// ตรวจสอบว่า service ที่ถูกเลือกมีอยู่จริงหรือไม่
		Optional<BarberServiceModel> serviceOpt = barberServiceModelService.findById(serviceId);
		if (serviceOpt.isEmpty()) {
			model.addAttribute("error", "ไม่พบบริการที่เลือก");
			return "appointment_form"; // กลับไปที่ฟอร์มจอง
		}

		// Logic การบันทึกการจอง (หากผ่านการตรวจสอบ)
		Appointment appointment = new Appointment();
		appointment.setDate(date);
		appointment.setTime(time);

		// กำหนดช่างตัดผมและบริการที่ถูกเลือก
		Barber barber = barberOpt.get();
		appointment.setBarber(barber);

		BarberServiceModel barberService = serviceOpt.get();
		appointment.setBarberService(barberService); // กำหนด barberService

		User user = (User) session.getAttribute("user");
		appointment.setUser(user);

		// บันทึกการจองในฐานข้อมูล
		appointmentService.saveAppointment(appointment);

		return "redirect:/user/appointments"; // ไปยังหน้าการนัดหมายของผู้ใช้
	}

	@GetMapping("/barber/appointments")
	public String viewBarberAppointments(HttpSession session, Model model) {
		Barber barber = (Barber) session.getAttribute("barber");
		if (barber != null) {
			List<Appointment> appointments = appointmentService.getAppointmentsByBarber(barber);
			model.addAttribute("appointments", appointments);
			model.addAttribute("barber", barber);
			return "barber_appointments"; // ชื่อไฟล์ HTML ที่แก้ไขแล้ว
		}
		return "redirect:/login";
	}

	// แสดงรายการนัดหมายของ User
	@GetMapping("/user/appointments")
	public String viewUserAppointments(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<Appointment> appointments = appointmentService.getAppointmentsByUser(user);
			model.addAttribute("appointments", appointments);
			return "user_appointments"; // ชื่อไฟล์ HTML ที่แก้ไขแล้ว
		}
		return "redirect:/login";
	}

	// ลบนัดหมาย
	@PostMapping("/delete-appointment")
	public String deleteAppointment(@RequestParam Long appointmentId, HttpSession session) {
		Barber barber = (Barber) session.getAttribute("barber");
		if (barber != null) {
			Optional<Appointment> appointmentOpt = appointmentService.findById(appointmentId);
			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();
				// ตรวจสอบว่าช่างตัดผมที่ลบนัดหมายเป็นคนเดียวกับที่ล็อกอินอยู่
				if (appointment.getBarber().getId().equals(barber.getId())) {
					appointmentService.deleteAppointment(appointmentId);
				}
			}
			return "redirect:/barber/appointments";
		}
		return "redirect:/login";
	}

	@PostMapping("/accept-appointment")
	public String acceptAppointment(@RequestParam("appointmentId") Long appointmentId, HttpSession session) {
		Barber barber = (Barber) session.getAttribute("barber");
		if (barber != null) {
			Optional<Appointment> appointmentOpt = appointmentService.findById(appointmentId);
			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();
				// ตรวจสอบว่าช่างตัดผมที่ยอมรับการนัดหมายเป็นคนเดียวกับที่ล็อกอินอยู่
				if (appointment.getBarber().getId().equals(barber.getId())) {
					// คุณสามารถเพิ่มฟิลด์ status ใน Appointment เช่น status = "Accepted"
					appointment.setStatus("Accepted");
					appointmentService.saveAppointment(appointment);
				}
			}
		}
		return "redirect:/barber/appointments";
	}

	@PostMapping("/user/cancel-appointment")
	public String cancelAppointment(@RequestParam("appointmentId") Long appointmentId, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			Optional<Appointment> appointmentOpt = appointmentService.findById(appointmentId);
			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();
				if (appointment.getUser().getId().equals(user.getId())) {
					appointment.setStatus("Cancelled"); // เปลี่ยนสถานะเป็น Cancelled
					appointmentService.saveAppointment(appointment); // บันทึกการเปลี่ยนแปลง
				}
			}
			return "redirect:/user/appointments";
		}
		return "redirect:/login";
	}

	@GetMapping("/barber/total-orders")
	@ResponseBody
	public long getTotalOrders(HttpSession session) {
		Barber barber = (Barber) session.getAttribute("barber");
		if (barber != null) {
			return appointmentService.countAppointmentsByBarber(barber);
		}
		return 0;
	}

	@GetMapping("/barber/pending-orders")
	@ResponseBody
	public long getPendingOrders(HttpSession session) {
		Barber barber = (Barber) session.getAttribute("barber");
		if (barber != null) {
			return appointmentService.countPendingAppointmentsByBarber(barber);
		}
		return 0;
	}

	@GetMapping("/appointments/completed-orders")
	@ResponseBody
	public long getCompletedOrders(HttpSession session) {
		Barber barber = (Barber) session.getAttribute("barber");
		if (barber != null) {
			return appointmentService.countCompletedAppointmentsByBarber(barber);
		}
		return 0;
	}

	@GetMapping("/barber/cancelled-orders")
	@ResponseBody
	public long getCancelledOrders(HttpSession session) {
		Barber barber = (Barber) session.getAttribute("barber");
		if (barber != null) {
			return appointmentService.countCancelledAppointmentsByBarber(barber);
		}
		return 0;
	}

	@GetMapping("/api/appointments-by-date")
	@ResponseBody // เพิ่มคำสั่งนี้เพื่อระบุให้ส่งข้อมูล JSON กลับไป
	public List<AppointmentResponse> getAppointmentsByDate(@RequestParam("date") String date) {
		LocalDate selectedDate = LocalDate.parse(date);

		List<Appointment> appointments = appointmentService.getAppointmentsByDate(selectedDate);

		return appointments.stream()
				.map(appointment -> new AppointmentResponse(appointment.getUser().getName(),
						appointment.getTime().toString(), appointment.getBarberService().getServiceName(),
						appointment.getStatus(), appointment.getBarber().getName()))
				.collect(Collectors.toList());
	}

	// DTO สำหรับการส่งข้อมูลกลับในรูปแบบ JSON
	public static class AppointmentResponse {
		private String customerName;
		private String time;
		private String serviceName;
		private String status;
		private String barberName;

		public AppointmentResponse(String customerName, String time, String serviceName, String status,
				String barberName) {
			this.customerName = customerName;
			this.time = time;
			this.serviceName = serviceName;
			this.status = status;
			this.barberName = barberName;
		}

		// Getters
		public String getCustomerName() {
			return customerName;
		}

		public String getTime() {
			return time;
		}

		public String getServiceName() {
			return serviceName;
		}

		public String getStatus() {
			return status;
		}

		public String getBarberName() {
			return barberName;
		}
	}

	@PostMapping("/cancel-appointment")
	public String barberCancelAppointment(@RequestParam Long appointmentId, HttpSession session) {
		Barber barber = (Barber) session.getAttribute("barber");
		if (barber != null) {
			Optional<Appointment> appointmentOpt = appointmentService.findById(appointmentId);
			if (appointmentOpt.isPresent()) {
				Appointment appointment = appointmentOpt.get();
				if (appointment.getBarber().getId().equals(barber.getId())) {
					appointment.setStatus("Cancelled");
					appointmentService.saveAppointment(appointment);
				}
			}
			return "redirect:/barber/appointments";
		}
		return "redirect:/login";
	}
}