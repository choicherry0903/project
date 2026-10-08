package com.roofandwall.interior.material.dto;

import jakarta.validation.constraints.*;

public class MaterialForm {

    @NotBlank(message = "장소를 선택해 주세요.")
    @Size(max = 50)
    private String category;

    @NotBlank(message = "품목을 선택해 주세요.")
    @Size(max = 50)
    private String subcategory;

    @NotBlank(message = "품명을 입력해 주세요.")
    @Size(max = 200)
    private String name;

    @Size(max = 100)
    private String brand;

    @Size(max = 100)
    private String size;

    @Size(max = 100)
    private String color;

    private long version;
    private org.springframework.web.multipart.MultipartFile image;
    private boolean removeImage;

    public org.springframework.web.multipart.MultipartFile getImage() {
        return image;
    }

    public void setImage(org.springframework.web.multipart.MultipartFile image) {
        this.image = image;
    }

    public boolean isRemoveImage() {
        return removeImage;
    }

    public void setRemoveImage(boolean removeImage) {
        this.removeImage = removeImage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String v) {
        category = v;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String v) {
        subcategory = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String v) {
        brand = v;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String v) {
        size = v;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String v) {
        color = v;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long v) {
        version = v;
    }
}
