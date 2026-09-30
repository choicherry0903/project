package com.barunsigong.interior;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.barunsigong.interior.auth.repository.StaffAccountRepository;
import com.barunsigong.interior.auth.service.*;
import com.barunsigong.interior.material.model.Material;
import com.barunsigong.interior.material.repository.MaterialRepository;
import com.barunsigong.interior.storage.*;
import java.net.URI;
import java.util.Base64;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MaterialImageIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    StaffAccountRepository accounts;

    @Autowired
    MaterialRepository materials;

    @Autowired
    StorageDeletionRepository deletions;

    @MockitoBean
    ImageStorage storage;

    @MockitoBean
    VerificationEmailSender verificationEmailSender;

    StaffPrincipal admin;
    static final byte[] PNG = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aX1kAAAAASUVORK5CYII="
    );

    @BeforeEach
    void setup() {
        admin = new StaffPrincipal(accounts.findByUsernameIgnoreCase("test-admin").orElseThrow());
        when(storage.configured()).thenReturn(true);
    }

    Material material(Long company) {
        var m = new Material(company, "ENTRANCE", null, "이미지 검증 자재", null, null, "개", 0L, null, null, true);
        m.updateDetails("ENTRANCE", "FLOOR_TILE", m.getName(), null, null, null);
        return materials.saveAndFlush(m);
    }

    MockMultipartHttpServletRequestBuilder update(Material m) {
        var request = multipart("/materials/" + m.getId());
        request
            .param("category", "ENTRANCE")
            .param("subcategory", "FLOOR_TILE")
            .param("name", m.getName())
            .param("version", String.valueOf(m.getVersion()));
        return request;
    }

    MockMultipartFile image() {
        return new MockMultipartFile("image", "test.png", "image/png", PNG);
    }

    @Test
    void multipleImagesAreRejectedWithoutCreatingOrReplacingMaterial() throws Exception {
        var m = material(1L);
        String oldKey = "materials/1/existing.png";
        m.setImageKey(oldKey);
        materials.saveAndFlush(m);
        long count = materials.count();
        var create = multipart("/materials");
        create.param("category", "ENTRANCE").param("subcategory", "FLOOR_TILE").param("name", "여러 이미지 등록 시도");
        for (var request : java.util.List.of(create, update(m))) {
            mvc.perform(request.file(image()).file(image()).with(user(admin)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("material/form"))
                .andExpect(model().attributeHasErrors("form"))
                .andExpect(
                    content().string(
                        org.hamcrest.Matchers.containsString("이미지는 품목당 최대 1개만 등록할 수 있습니다.")
                    )
                );
        }
        assertThat(materials.count()).isEqualTo(count);
        assertThat(materials.findById(m.getId()).orElseThrow().getImageKey()).isEqualTo(oldKey);
        verify(storage, never()).upload(anyString(), any());
        assertThat(deletions.findById(oldKey)).isEmpty();
    }

    @Test
    void uploadReplaceKeepAndRemoveImage() throws Exception {
        mvc.perform(
            multipart("/materials")
                .file(image())
                .with(user(admin))
                .with(csrf())
                .param("category", "ENTRANCE")
                .param("subcategory", "FLOOR_TILE")
                .param("name", "이미지 신규 자재")
        ).andExpect(redirectedUrlPattern("/materials/*"));
        var m = materials
            .findAll()
            .stream()
            .filter(x -> "이미지 신규 자재".equals(x.getName()))
            .findFirst()
            .orElseThrow();
        String oldKey = m.getImageKey();
        assertThat(oldKey)
            .startsWith("materials/" + m.getCompanyId() + "/")
            .endsWith(".png");
        assertThat(deletions.findById(oldKey)).isEmpty();
        when(storage.readUrl(oldKey)).thenReturn(URI.create("https://example.test/signed-image"));
        mvc.perform(get("/materials/" + m.getId() + "/image").with(user(admin)))
            .andExpect(redirectedUrl("https://example.test/signed-image"))
            .andExpect(header().string("Cache-Control", "no-store"));
        mvc.perform(get("/materials/" + m.getId() + "/edit").with(user(admin)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("removeImage")));
        mvc.perform(update(m).with(user(admin)).with(csrf())).andExpect(redirectedUrl("/materials/" + m.getId()));
        assertThat(m.getImageKey()).isEqualTo(oldKey);
        mvc.perform(update(m).file(image()).with(user(admin)).with(csrf())).andExpect(
            redirectedUrl("/materials/" + m.getId())
        );
        String newKey = m.getImageKey();
        assertThat(newKey).isNotEqualTo(oldKey);
        assertThat(deletions.findById(oldKey)).isPresent();
        verify(storage, never()).delete(oldKey);
        mvc.perform(update(m).param("removeImage", "true").with(user(admin)).with(csrf())).andExpect(
            redirectedUrl("/materials/" + m.getId())
        );
        assertThat(m.getImageKey()).isNull();
        assertThat(deletions.findById(newKey)).isPresent();
        mvc.perform(get("/materials/" + m.getId() + "/image").with(user(admin))).andExpect(status().isNotFound());
    }

    @Test
    void deletingMaterialQueuesImageAfterDatabaseDelete() throws Exception {
        var m = material(1L);
        m.setImageKey("materials/1/" + m.getId() + "/old.png");
        materials.saveAndFlush(m);
        mvc.perform(
            post("/materials/" + m.getId() + "/delete")
                .with(user(admin))
                .with(csrf())
                .param("version", String.valueOf(m.getVersion()))
        ).andExpect(redirectedUrl("/materials"));
        assertThat(materials.findById(m.getId())).isEmpty();
        assertThat(deletions.findById(m.getImageKey())).isPresent();
        verify(storage, never()).delete(anyString());
    }

    @Test
    void cleanupDeletesOnlyDueFilesAndRetriesFailures() {
        String key = "materials/test/" + java.util.UUID.randomUUID() + ".png";
        String delayed = "materials/test/" + java.util.UUID.randomUUID() + ".png";
        deletions.saveAndFlush(new StorageDeletion(key, java.time.Instant.now().minusSeconds(1)));
        deletions.saveAndFlush(new StorageDeletion(delayed, java.time.Instant.now().plusSeconds(3600)));
        doThrow(new ImageStorageException("temporary failure")).when(storage).delete(key);
        var worker = new StorageCleanupWorker(deletions, storage);
        worker.clean();
        assertThat(deletions.findById(key).orElseThrow().getAttempts()).isEqualTo(1);
        assertThat(deletions.findById(key).orElseThrow().getAvailableAt()).isAfter(java.time.Instant.now());
        verify(storage, never()).delete(delayed);
        doNothing().when(storage).delete(key);
        deletions.saveAndFlush(new StorageDeletion(key, java.time.Instant.now().minusSeconds(1)));
        worker.clean();
        assertThat(deletions.findById(key)).isEmpty();
        assertThat(deletions.findById(delayed)).isPresent();
    }

    @Test
    void unconfiguredStorageRejectsImageAndShowsUnavailableResponse() throws Exception {
        var m = material(1L);
        when(storage.configured()).thenReturn(false);
        mvc.perform(update(m).file(image()).with(user(admin)).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("form"));
        assertThat(m.getImageKey()).isNull();
        verify(storage, never()).upload(anyString(), any());
        m.setImageKey("materials/test/existing.png");
        materials.saveAndFlush(m);
        when(storage.readUrl(m.getImageKey())).thenThrow(new ImageStorageException("not configured"));
        mvc.perform(get("/materials/" + m.getId() + "/image").with(user(admin))).andExpect(
            status().isServiceUnavailable()
        );
    }

    @Test
    void invalidOversizedConflictingAndStaleImagesNeverUpload() throws Exception {
        var m = material(1L);
        mvc.perform(
            update(m)
                .file(new MockMultipartFile("image", "fake.png", "image/png", "not an image".getBytes()))
                .with(user(admin))
                .with(csrf())
        ).andExpect(model().attributeHasErrors("form"));
        mvc.perform(
            update(m)
                .file(
                    new MockMultipartFile("image", "large.png", "image/png", new byte[(int) ImageUpload.MAX_BYTES + 1])
                )
                .with(user(admin))
                .with(csrf())
        ).andExpect(model().attributeHasErrors("form"));
        mvc.perform(update(m).file(image()).param("removeImage", "true").with(user(admin)).with(csrf())).andExpect(
            model().attributeHasErrors("form")
        );
        var stale = update(m).file(image());
        m.updateDetails("ENTRANCE", "FLOOR_TILE", "변경된 품명", null, null, null);
        materials.saveAndFlush(m);
        mvc.perform(stale.with(user(admin)).with(csrf())).andExpect(model().attributeHasErrors("form"));
        verify(storage, never()).upload(anyString(), any());
        assertThat(m.getImageKey()).isNull();
    }

    @Test
    void imageReadRequiresCompanyAndRoleAndCsrfForUpload() throws Exception {
        var m = material(9999L);
        m.setImageKey("materials/9999/foreign.png");
        materials.saveAndFlush(m);
        mvc.perform(get("/materials/" + m.getId() + "/image").with(user(admin))).andExpect(status().isNotFound());
        mvc.perform(get("/materials/" + m.getId() + "/image")).andExpect(status().is3xxRedirection());
        mvc.perform(multipart("/materials").file(image()).with(user(admin))).andExpect(status().isForbidden());
        verify(storage, never()).readUrl(anyString());
        verify(storage, never()).upload(anyString(), any());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void uploadFailureRollsBackDetailsAndRetainsOldImage() throws Exception {
        var m = material(1L);
        String oldKey = "materials/1/" + m.getId() + "/old.png";
        m.setImageKey(oldKey);
        m = materials.saveAndFlush(m);
        doThrow(new ImageStorageException("이미지 업로드에 실패했습니다.")).when(storage).upload(anyString(), any());
        mvc.perform(update(m).file(image()).param("name", "실패한 변경").with(user(admin)).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("form"));
        var saved = materials.findById(m.getId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo("이미지 검증 자재");
        assertThat(saved.getImageKey()).isEqualTo(oldKey);
        var key = ArgumentCaptor.forClass(String.class);
        verify(storage).upload(key.capture(), any());
        assertThat(deletions.findById(key.getValue())).isPresent();
        assertThat(deletions.findById(oldKey)).isEmpty();
        verify(storage, never()).delete(anyString());
        materials.deleteById(m.getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void committedImageIsRetainedAndConcurrentEditDuringUploadIsRejected() throws Exception {
        mvc.perform(
            multipart("/materials")
                .file(image())
                .with(user(admin))
                .with(csrf())
                .param("category", "ENTRANCE")
                .param("subcategory", "FLOOR_TILE")
                .param("name", "이미지 커밋 검증")
        ).andExpect(redirectedUrlPattern("/materials/*"));
        var m = materials
            .findAll()
            .stream()
            .filter(x -> "이미지 커밋 검증".equals(x.getName()))
            .findFirst()
            .orElseThrow();
        String original = m.getImageKey();
        assertThat(deletions.findById(original)).isEmpty();
        Long id = m.getId();
        doAnswer(invocation -> {
            var concurrent = materials.findById(id).orElseThrow();
            concurrent.updateDetails("ENTRANCE", "FLOOR_TILE", "동시에 수정된 자재", null, null, null);
            materials.saveAndFlush(concurrent);
            return null;
        })
            .when(storage)
            .upload(anyString(), any());
        mvc.perform(update(m).file(image()).with(user(admin)).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("form"));
        var saved = materials.findById(id).orElseThrow();
        assertThat(saved.getName()).isEqualTo("동시에 수정된 자재");
        assertThat(saved.getImageKey()).isEqualTo(original);
        var uploadedKeys = ArgumentCaptor.forClass(String.class);
        verify(storage, times(2)).upload(uploadedKeys.capture(), any());
        String unlinkedKey = uploadedKeys.getAllValues().get(1);
        assertThat(deletions.findById(unlinkedKey)).isPresent();
        mvc.perform(
            post("/materials/" + id + "/delete")
                .with(user(admin))
                .with(csrf())
                .param("version", String.valueOf(saved.getVersion()))
        ).andExpect(redirectedUrl("/materials"));
        assertThat(materials.findById(id)).isEmpty();
        assertThat(deletions.findById(original)).isPresent();
        deletions.deleteById(original);
    }
}
