package com.barunsigong.interior.material.model;

import com.barunsigong.interior.common.model.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** 후속 기능의 데이터 모델. 변경 규칙은 해당 서비스 구현 시 적용합니다. */
@Entity
@Table(name = "materials")
public class Material extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 50)
    private String subcategory;

    @Column(name = "material_size", length = 100)
    private String size;

    @Column(length = 512)
    private String imageKey;

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getSize() {
        return size;
    }

    public String getCategoryLabel() {
        return MaterialCategories.categoryLabel(category);
    }

    public String getSubcategoryLabel() {
        return MaterialCategories.itemLabel(subcategory);
    }

    public void updateDetails(
        String category,
        String subcategory,
        String name,
        String brand,
        String size,
        String color
    ) {
        this.category = category;
        this.subcategory = subcategory;
        this.name = name.trim();
        this.brand = brand == null ? null : brand.trim();
        this.size = size == null ? null : size.trim();
        this.color = color == null ? null : color.trim();
    }

    @Column(length = 100)
    private String brand;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String productCode;

    @Column(length = 100)
    private String color;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private Long sellingPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal coveragePerRoll;

    @Column(precision = 5, scale = 4)
    private BigDecimal wasteRate;

    @Column(nullable = false)
    private boolean active;

    protected Material() {}

    public Material(
        Long companyId,
        String category,
        String brand,
        String name,
        String productCode,
        String color,
        String unit,
        Long sellingPrice,
        BigDecimal coveragePerRoll,
        BigDecimal wasteRate,
        boolean active
    ) {
        super(companyId);
        this.category = category;
        this.brand = brand;
        this.name = name;
        this.productCode = productCode;
        this.color = color;
        this.unit = unit;
        this.sellingPrice = sellingPrice;
        this.coveragePerRoll = coveragePerRoll;
        this.wasteRate = wasteRate;
        this.active = active;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getColor() {
        return color;
    }

    public String getUnit() {
        return unit;
    }

    public Long getSellingPrice() {
        return sellingPrice;
    }

    public BigDecimal getCoveragePerRoll() {
        return coveragePerRoll;
    }

    public BigDecimal getWasteRate() {
        return wasteRate;
    }

    public boolean isActive() {
        return active;
    }
}
