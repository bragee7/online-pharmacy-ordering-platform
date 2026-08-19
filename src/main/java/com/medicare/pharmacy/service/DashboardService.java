package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.DashboardStats;
import com.medicare.pharmacy.dto.MedicineDto;
import com.medicare.pharmacy.dto.OrderDto;
import com.medicare.pharmacy.entity.AuditLog;
import com.medicare.pharmacy.entity.Inventory;
import com.medicare.pharmacy.enums.OrderStatus;
import com.medicare.pharmacy.enums.PrescriptionStatus;
import com.medicare.pharmacy.repository.AuditLogRepository;
import com.medicare.pharmacy.repository.InventoryRepository;
import com.medicare.pharmacy.repository.MedicineRepository;
import com.medicare.pharmacy.repository.OrderRepository;
import com.medicare.pharmacy.repository.PrescriptionRepository;
import com.medicare.pharmacy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final OrderRepository orderRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final InventoryRepository inventoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final OrderService orderService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public DashboardStats stats() {
        long users = userRepository.count();
        long medicines = medicineRepository.countByActiveTrue();
        long orders = orderRepository.count();
        BigDecimal revenue = orderRepository.totalRevenue();
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PLACED);
        long pendingPrescriptions = prescriptionRepository.countByStatus(PrescriptionStatus.PENDING);
        long lowStock = inventoryRepository.findLowStock().size();
        long delivered = orderRepository.countByStatus(OrderStatus.DELIVERED);
        return DashboardStats.builder()
                .totalUsers(users)
                .totalMedicines(medicines)
                .totalOrders(orders)
                .totalRevenue(revenue)
                .pendingOrders(pendingOrders)
                .pendingPrescriptions(pendingPrescriptions)
                .lowStockCount(lowStock)
                .deliveredOrders(delivered)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> recentOrders(int limit) {
        int size = limit <= 0 ? 10 : limit;
        return orderService.listAll(null,
                PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> lowStockMedicines() {
        List<Inventory> low = inventoryRepository.findLowStock();
        return low.stream()
                .map(inv -> MedicineDto.from(inv.getMedicine(), inv.getAvailable()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> recentAudits(int limit) {
        int size = limit <= 0 ? 20 : limit;
        return auditService.recent(size);
    }
}
