package com.neeraj.usageservice.controller;

import com.neeraj.usageservice.dto.UsageDTO;
import com.neeraj.usageservice.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
public class UsageController {
    private final UsageService usageService;

    @GetMapping("/{userId}")
    public ResponseEntity<UsageDTO> getUserDeviceUsage(@PathVariable Long userId, @RequestParam(defaultValue = "3") int days) {
        final UsageDTO usage = usageService.getXDaysUsageForUser(userId, days);
        return ResponseEntity.ok(usage);
    }
}
