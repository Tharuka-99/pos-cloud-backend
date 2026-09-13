package com.blackholesoftware.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditSummaryDTO {
    private Float totalCreditGivenInPeriod;
    private Float totalCreditCollectedInPeriod;
    private Float totalOutstandingCreditAllTime;
}