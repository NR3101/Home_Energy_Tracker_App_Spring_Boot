package com.neeraj.ingestionservice.controller;

import com.neeraj.ingestionservice.dto.EnergyUsageDTO;
import com.neeraj.ingestionservice.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void ingestData(@RequestBody EnergyUsageDTO usageDTO) {
        ingestionService.ingestEnergyUsage(usageDTO);
    }
}
