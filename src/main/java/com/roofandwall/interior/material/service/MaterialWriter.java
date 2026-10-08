package com.roofandwall.interior.material.service;

import com.roofandwall.interior.auth.service.CurrentStaff;
import com.roofandwall.interior.common.exception.ResourceNotFoundException;
import com.roofandwall.interior.material.dto.MaterialForm;
import com.roofandwall.interior.material.model.Material;
import com.roofandwall.interior.material.repository.MaterialRepository;
import com.roofandwall.interior.storage.StorageCleanup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Commits material changes and cleanup intents together, after the upload succeeds. */
@Service
@PreAuthorize("hasAnyRole('ADMIN','OFFICE')")
public class MaterialWriter {

    private final MaterialRepository materials;
    private final CurrentStaff staff;
    private final StorageCleanup cleanup;

    public MaterialWriter(MaterialRepository materials, CurrentStaff staff, StorageCleanup cleanup) {
        this.materials = materials;
        this.staff = staff;
        this.cleanup = cleanup;
    }

    @Transactional
    public Material save(Long id, MaterialForm form, String uploadedKey) {
        var m =
            id == null
                ? new Material(
                      staff.companyId(),
                      form.getCategory(),
                      form.getBrand(),
                      form.getName(),
                      null,
                      form.getColor(),
                      "개",
                      0L,
                      null,
                      null,
                      true
                  )
                : materials.findByIdAndCompanyId(id, staff.companyId()).orElseThrow(ResourceNotFoundException::new);
        if (id != null && m.getVersion() != form.getVersion()) throw new IllegalArgumentException(
            "다른 사용자가 수정했습니다. 새로고침 후 다시 확인해 주세요."
        );
        String oldKey = m.getImageKey();
        m.updateDetails(
            form.getCategory(),
            form.getSubcategory(),
            form.getName(),
            form.getBrand(),
            form.getSize(),
            form.getColor()
        );
        if (uploadedKey != null) m.setImageKey(uploadedKey);
        else if (form.isRemoveImage()) m.setImageKey(null);
        materials.saveAndFlush(m);
        if (uploadedKey != null) cleanup.retainUpload(uploadedKey);
        if (uploadedKey != null || form.isRemoveImage()) cleanup.deleteAfterCommit(oldKey);
        return m;
    }
}
