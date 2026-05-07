package com.streetworkout.dto;

import com.streetworkout.entity.Progress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {

    private Long id;
    private LocalDate progressDate;
    private BigDecimal weight;
    private String notes;
    private LocalDateTime createdAt;

    public static ProgressResponse fromEntity(Progress progress) {
        return new ProgressResponse(
            progress.getId(),
            progress.getProgressDate(),
            progress.getWeight(),
            progress.getNotes(),
            progress.getCreatedAt()
        );
    }
}
