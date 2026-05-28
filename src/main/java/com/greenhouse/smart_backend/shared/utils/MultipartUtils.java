package com.greenhouse.smart_backend.shared.utils;

import org.apache.tika.Tika;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;

public final class MultipartUtils {

    private static final Tika tika = new Tika();
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private MultipartUtils() {}

    public static Resource base64ToResource(String base64) {
        return base64ToResource(base64, "image");
    }

    public static Resource base64ToResource(String base64, String baseName) {
        Objects.requireNonNull(base64, "Base64 input must not be null");
        Objects.requireNonNull(baseName, "Base name must not be null");

        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            String mimeType = tika.detect(bytes);
            String extension = EXTENSIONS.getOrDefault(mimeType, "bin");
            String filename = baseName + "." + extension;

            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encoding", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process image data", e);
        }
    }
}