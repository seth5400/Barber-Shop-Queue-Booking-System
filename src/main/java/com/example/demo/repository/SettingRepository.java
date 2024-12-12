package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Setting;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    @Query("SELECT s FROM Setting s WHERE s.id = 1")
    Setting findSetting();  // Method สำหรับดึง setting
}
