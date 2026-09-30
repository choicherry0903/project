package com.barunsigong.interior.material.controller;

import com.barunsigong.interior.material.dto.MaterialForm;
import com.barunsigong.interior.material.model.MaterialCategories;
import com.barunsigong.interior.material.service.MaterialService;
import com.barunsigong.interior.storage.ImageStorageException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/materials")
public class MaterialController {

    private final MaterialService materials;

    public MaterialController(MaterialService m) {
        materials = m;
    }

    @ModelAttribute("categories")
    public Object categories() {
        return MaterialCategories.ALL;
    }

    @ModelAttribute("subcategories")
    public Object subcategories() {
        return MaterialCategories.ITEMS;
    }

    @GetMapping
    public String index(
        @RequestParam(defaultValue = "") String category,
        @RequestParam(defaultValue = "") String subcategory,
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        model.addAttribute("materials", materials.search(category, subcategory, keyword, page));
        model.addAttribute("subcategory", subcategory);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        return "material/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("form", new MaterialForm());
        return form(model, null);
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var m = materials.get(id);
        var f = new MaterialForm();
        f.setCategory(m.getCategory());
        f.setSubcategory(m.getSubcategory());
        f.setName(m.getName());
        f.setBrand(m.getBrand());
        f.setSize(m.getSize());
        f.setColor(m.getColor());
        f.setVersion(m.getVersion());
        model.addAttribute("form", f);
        return form(model, id);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("material", materials.get(id));
        return "material/detail";
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Void> image(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(materials.imageUrl(id))
                .cacheControl(CacheControl.noStore())
                .build();
        } catch (ImageStorageException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).cacheControl(CacheControl.noStore()).build();
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String oversizedImage(RedirectAttributes redirect) {
        redirect.addFlashAttribute(
            "materialError",
            "이미지는 10MB 이하로 선택해 주세요. 입력 내용을 확인하고 다시 등록해 주세요."
        );
        return "redirect:/materials";
    }

    @PostMapping({ "", "/{id}" })
    public String save(
        @PathVariable(required = false) Long id,
        @Valid @ModelAttribute("form") MaterialForm f,
        BindingResult errors,
        @RequestParam(name = "image", required = false) List<MultipartFile> images,
        Model model,
        RedirectAttributes redirect
    ) {
        if (id != null) materials.get(id);
        if (images != null && images.size() > 1) errors.reject(
            "imageCount",
            "이미지는 품목당 최대 1개만 등록할 수 있습니다."
        );
        if (errors.hasErrors()) return form(model, id);
        try {
            var material = materials.save(id, f);
            redirect.addFlashAttribute("success", "자재 품목을 저장했습니다.");
            return "redirect:/materials/" + material.getId();
        } catch (IllegalArgumentException e) {
            errors.reject("save", e.getMessage());
        } catch (OptimisticLockingFailureException e) {
            errors.reject("conflict", "다른 사용자가 변경했습니다. 새로고침 후 다시 확인해 주세요.");
        } catch (ImageStorageException e) {
            errors.reject("image", e.getMessage());
        }
        if (f.getImage() != null && !f.getImage().isEmpty()) errors.reject(
            "reselect",
            "저장하지 못했습니다. 이미지를 다시 선택한 후 저장해 주세요."
        );
        return form(model, id);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam long version, RedirectAttributes redirect) {
        try {
            materials.delete(id, version);
            redirect.addFlashAttribute("success", "자재 품목을 삭제했습니다.");
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("materialError", "발주서에서 사용 중인 자재는 삭제할 수 없습니다.");
            return "redirect:/materials/" + id;
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            redirect.addFlashAttribute("materialError", "다른 사용자가 변경했습니다. 새로고침 후 다시 확인해 주세요.");
            return "redirect:/materials/" + id;
        }
        return "redirect:/materials";
    }

    private String form(Model model, Long id) {
        model.addAttribute("materialId", id);
        if (id != null) model.addAttribute("material", materials.get(id));
        return "material/form";
    }
}
