package com.roofandwall.interior.storage;

import java.io.*;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.web.multipart.MultipartFile;

/** Validates actual image data, independent of browser MIME/name declarations. */
public final class ImageUpload {

    public record Image(byte[] bytes, String contentType, String extension) {}

    public enum Folder {
        MATERIALS,
        PROJECTS,
        AFTERSERVICES,
    }

    public static final long MAX_BYTES = 10 * 1024 * 1024;

    public static Image validate(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("이미지는 10MB 이하로 선택해 주세요.");
        try {
            byte[] bytes;
            try (var stream = file.getInputStream()) {
                bytes = stream.readNBytes((int) MAX_BYTES + 1);
            }
            if (bytes.length > MAX_BYTES) throw new IllegalArgumentException("이미지는 10MB 이하로 선택해 주세요.");
            try (var stream = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) throw new IllegalArgumentException(
                    "JPG, PNG, WEBP 이미지 파일만 등록할 수 있습니다."
                );
                var reader = readers.next();
                try {
                    reader.setInput(stream, true, true);
                    String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                    String extension = switch (format) {
                        case "jpeg", "jpg" -> "jpg";
                        case "png" -> "png";
                        case "webp" -> "webp";
                        default -> throw new IllegalArgumentException(
                            "JPG, PNG, WEBP 이미지 파일만 등록할 수 있습니다."
                        );
                    };
                    int width = reader.getWidth(0),
                        height = reader.getHeight(0);
                    if (
                        width <= 0 || height <= 0 || (long) width * height > 25_000_000
                    ) throw new IllegalArgumentException("이미지는 2,500만 화소 이하로 선택해 주세요.");
                    if (reader.read(0) == null) throw new IllegalArgumentException("이미지를 읽을 수 없습니다.");
                    return new Image(bytes, "image/" + (extension.equals("jpg") ? "jpeg" : extension), extension);
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지를 읽을 수 없습니다. 다른 파일을 선택해 주세요.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("이미지를 읽을 수 없습니다. 다른 파일을 선택해 주세요.");
        }
    }

    public static String key(Folder folder, Long companyId, Image image) {
        return (
            folder.name().toLowerCase(Locale.ROOT) + "/" + companyId + "/" + UUID.randomUUID() + "." + image.extension()
        );
    }

    private ImageUpload() {}
}
