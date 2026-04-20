// ReadingService.java
package com.metereye.backend.service;

import com.metereye.backend.dto.ManualReadingRequest;
import com.metereye.backend.dto.ReadingResponse;
import com.metereye.backend.dto.SensorReadingRequest;
import com.metereye.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ReadingService {
    
    ReadingResponse createManualReading(ManualReadingRequest request, User user);
    
    ReadingResponse createImageReading(Long meterId, MultipartFile file, User user) throws IOException;
    
    ReadingResponse createSensorReading(SensorReadingRequest request, User user);
    
    Page<ReadingResponse> getMeterReadings(Long meterId, Pageable pageable, User user);
    
    ReadingResponse getLatestReading(Long meterId, User user);
}
