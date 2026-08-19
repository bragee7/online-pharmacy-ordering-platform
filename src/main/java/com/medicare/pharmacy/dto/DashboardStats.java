package com.medicare.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    private long totalUsers;
    private long totalMedicines;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long pendingPrescriptions;
    private long lowStockCount;
    private long deliveredOrders;
}
