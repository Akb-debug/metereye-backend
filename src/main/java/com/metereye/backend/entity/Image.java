// Image.java
package com.metereye.backend.entity;

import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "images")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Image extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "releve_id", nullable = false)
    private Releve releve;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Builder.Default
    @Column(name = "processed")
    private Boolean processed = false;

    @Column(name = "ocr_value")
    private Double ocrValue;

    @Column(name = "ocr_confidence")
    private Double ocrConfidence;

    @Column(name = "processing_error")
    private String processingError;
}
