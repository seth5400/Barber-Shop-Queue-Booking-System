package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.Setting;
import com.example.demo.repository.SettingRepository;

@Service
public class SettingService {

    @Autowired
    private SettingRepository settingRepository;

    private Setting getOrCreateSetting() {
        List<Setting> settings = settingRepository.findAll();
        Setting setting = settings.isEmpty() ? null : settings.get(0);
        if (setting == null) {
            setting = new Setting();
            setting.setBookingEnabled(false);  // ค่าเริ่มต้น
            settingRepository.save(setting);
        }
        return setting;
    }

    public boolean isBookingEnabled() {
        return getOrCreateSetting().isBookingEnabled();
    }

    public void saveSettings(boolean isBookingEnabled) {
        Setting setting = getOrCreateSetting();
        setting.setBookingEnabled(isBookingEnabled);
        settingRepository.save(setting);
    }
}