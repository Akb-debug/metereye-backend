// ImageService.java
package com.metereye.backend.service;

import com.metereye.backend.entity.Image;
import com.metereye.backend.entity.Releve;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    
    Image saveImage(MultipartFile file, Releve releve) throws IOException;
    
    String getImageUrl(Long imageId);
    
    Image getImage(Long imageId);
    
    void processImageWithOCR(Long imageId);
    
    void deleteImage(Long imageId);
}
