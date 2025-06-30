
package com.fitness.fitnessapi.scheduler;

import com.fitness.fitnessapi.entity.TimeSlot;
import com.fitness.fitnessapi.repository.TimeSlotRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SlotResetScheduler {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    // ✅ This runs every day at 12:00 AM → Resets availability
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyAvailability() {
        List<TimeSlot> allSlots = timeSlotRepository.findAll();
        for (TimeSlot slot : allSlots) {
            slot.setAvailableToday(false); // Reset daily
        }
        timeSlotRepository.saveAll(allSlots);
        System.out.println("✅ Running resetDailyAvailability at " + LocalDateTime.now());
    }

    // ✅ This runs every 1 minutes → Marks expired slots
    @Scheduled(cron = "0 */1 * * * *")
    public void updateSlotExpiry() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<TimeSlot> allSlots = timeSlotRepository.findAll();
        for (TimeSlot slot : allSlots) {
            boolean isExpired = false;

            if (slot.getDate().isBefore(today)) {
                isExpired = true;
            } else if (slot.getDate().isEqual(today) && slot.getEndTime().isBefore(now)) {
                isExpired = true;
            }

            if (slot.isExpired() != isExpired) {
                slot.setExpired(isExpired);
            }
        }

        timeSlotRepository.saveAll(allSlots);
    }

//    @PostConstruct
//    public void testScheduler() {
//        System.out.println("🔁 Manually calling resetDailyAvailability at startup...");
//        resetDailyAvailability();
//    }
}
