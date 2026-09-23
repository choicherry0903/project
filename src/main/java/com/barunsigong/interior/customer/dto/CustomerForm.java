package com.barunsigong.interior.customer.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public class CustomerForm {

    @NotBlank
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank
    @Pattern(regexp = "[0-9]{8,15}", message = "연락처는 숫자 8~15자리로 입력해 주세요.")
    private String phone;

    @Size(max = 100)
    private String source;

    @Size(max = 2000)
    private String memo;

    @NotBlank
    @Pattern(regexp = "[0-9a-fA-F-]{36}")
    private String requestKey = UUID.randomUUID().toString();

    @Min(0)
    private long version;

    private boolean allowDuplicate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.replaceAll("[-\\s]", "");
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isAllowDuplicate() {
        return allowDuplicate;
    }

    public void setAllowDuplicate(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
    }
}
