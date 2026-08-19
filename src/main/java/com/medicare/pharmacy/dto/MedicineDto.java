package com.medicare.pharmacy.dto;

import com.medicare.pharmacy.entity.Medicine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineDto {
    private Long id;
    private String name;
    private String genericName;
    private String brandName;
    private String description;
    private String manufacturer;
    private BigDecimal price;
    private boolean prescriptionRequired;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private boolean active;
    private int stock;
    private boolean lowStock;

    public static MedicineDto from(Medicine medicine) {
        return from(medicine, -1);
    }

    public static MedicineDto from(Medicine medicine, int available) {
        if (medicine == null) {
            return null;
        }
        return MedicineDto.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .genericName(medicine.getGenericName())
                .brandName(medicine.getBrandName())
                .description(medicine.getDescription())
                .manufacturer(medicine.getManufacturer())
                .price(medicine.getPrice())
                .prescriptionRequired(medicine.isPrescriptionRequired())
                .categoryId(medicine.getCategory() != null ? medicine.getCategory().getId() : null)
                .categoryName(medicine.getCategory() != null ? medicine.getCategory().getName() : null)
                .imageUrl(medicine.getImageUrl())
                .active(medicine.isActive())
                .stock(available)
                .lowStock(available >= 0 && available <= 10)
                .build();
    }
}
