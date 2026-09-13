package com.blackholesoftware.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyTrendDto {
    private String date;
    private Double sales;
    private Double profit;
}