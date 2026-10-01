package com.barunsigong.interior.material.service;

import com.barunsigong.interior.auth.service.CurrentStaff;
import com.barunsigong.interior.common.exception.ResourceNotFoundException;
import com.barunsigong.interior.material.dto.MaterialForm;
import com.barunsigong.interior.material.model.*;
import com.barunsigong.interior.material.repository.MaterialRepository;
import com.barunsigong.interior.storage.*;
import java.net.URI;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
@PreAuthorize("hasAnyRole('ADMIN','OFFICE')")
public class MaterialService {

    private final MaterialRepository materials;
    private final CurrentStaff staff;
    private final ImageStorage storage;
    private final StorageCleanup cleanup;
    private final MaterialWriter writer;

    public MaterialService(
        MaterialRepository materials,
        CurrentStaff staff,
        ImageStorage storage,
        StorageCleanup cleanup,
        MaterialWriter writer
    ) {
        this.materials = materials;
        this.staff = staff;
        this.storage = storage;
        this.cleanup = cleanup;
        this.writer = writer;
    }

    @Transactional(readOnly = true)
    public Page<Material> search(String category, String subcategory, String keyword, int page) {
        return materials.search(
            staff.companyId(),
            category,
            subcategory,
            keyword.trim(),
            PageRequest.of(Math.max(0, page), 20, Sort.by("updatedAt").descending().and(Sort.by("id").descending()))
        );
    }

    @Transactional(readOnly = true)
    public Material get(Long id) {
        return materials.findByIdAndCompanyId(id, staff.companyId()).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public URI imageUrl(Long id) {
        var material = get(id);
        if (material.getImageKey() == null) throw new ResourceNotFoundException();
        return storage.readUrl(material.getImageKey());
    }

    /** External upload holds no DB connection; the writer rechecks version on commit. */
    public Material save(Long id, MaterialForm form) {
        if (!MaterialCategories.valid(form.getCategory(), form.getSubcategory())) throw new IllegalArgumentException(
            "등록된 장소와 품목을 선택해 주세요."
        );
        if (id != null && get(id).getVersion() != form.getVersion()) throw new IllegalArgumentException(
            "다른 사용자가 수정했습니다. 새로고침 후 다시 확인해 주세요."
        );
        var image = ImageUpload.validate(form.getImage());
        if (image != null && form.isRemoveImage()) throw new IllegalArgumentException(
            "이미지 교체와 삭제는 하나만 선택해 주세요."
        );
        if (image != null && !storage.configured()) throw new ImageStorageException(
            "이미지 저장소 설정이 필요합니다. 관리자에게 문의해 주세요."
        );
        String key = null;
        if (image != null) {
            key = ImageUpload.key(ImageUpload.Folder.MATERIALS, staff.companyId(), image);
            cleanup.prepareUpload(key);
            storage.upload(key, image);
        }
        return writer.save(id, form, key);
    }

    @Transactional
    public void delete(Long id, long version) {
        var material = get(id);
        if (material.getVersion() != version) throw new IllegalArgumentException(
            "다른 사용자가 수정했습니다. 새로고침 후 다시 확인해 주세요."
        );
        materials.delete(material);
        materials.flush();
        cleanup.deleteAfterCommit(material.getImageKey());
    }
}
