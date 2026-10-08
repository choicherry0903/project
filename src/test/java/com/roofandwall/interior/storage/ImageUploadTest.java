package com.roofandwall.interior.storage;

import static org.assertj.core.api.Assertions.*;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ImageUploadTest {

    @Test
    void supportsJpegAndWebp() throws Exception {
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(
            new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB),
            "jpg",
            bytes
        );
        assertThat(
            ImageUpload.validate(
                new MockMultipartFile("image", "test.jpg", "image/jpeg", bytes.toByteArray())
            ).contentType()
        ).isEqualTo("image/jpeg");
        byte[] webp = Base64.getDecoder().decode("UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEADsD+JaQAA3AAAAAA");
        assertThat(
            ImageUpload.validate(new MockMultipartFile("image", "test.webp", "image/webp", webp)).contentType()
        ).isEqualTo("image/webp");
    }

    @Test
    void detectsContentInsteadOfTrustingMimeOrFilename() {
        byte[] png = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aX1kAAAAASUVORK5CYII="
        );
        var image = ImageUpload.validate(
            new MockMultipartFile("image", "../../script.exe", "application/octet-stream", png)
        );
        assertThat(image.contentType()).isEqualTo("image/png");
        assertThat(image.extension()).isEqualTo("png");
        assertThat(ImageUpload.key(ImageUpload.Folder.PROJECTS, 2L, image))
            .startsWith("projects/2/")
            .doesNotContain("script");
    }

    @Test
    void rejectsSvgAndCorruptFiles() {
        assertThatThrownBy(() ->
            ImageUpload.validate(new MockMultipartFile("image", "x.svg", "image/svg+xml", "<svg></svg>".getBytes()))
        ).isInstanceOf(IllegalArgumentException.class);
        assertThat(ImageUpload.validate(new MockMultipartFile("image", new byte[0]))).isNull();
    }

    @Test
    void endpointMustExcludeBucketAndUseR2Https() {
        var p = new R2Properties();
        p.setEndpoint("https://account.r2.cloudflarestorage.com/roofandwall");
        assertThatThrownBy(p::endpointUri).isInstanceOf(IllegalArgumentException.class);
        p.setEndpoint("https://account.r2.cloudflarestorage.com");
        assertThat(p.endpointUri().getHost()).isEqualTo("account.r2.cloudflarestorage.com");
    }
}
