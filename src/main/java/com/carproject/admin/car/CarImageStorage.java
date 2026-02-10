package com.carproject.admin.car;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * 관리자 CAR_IMAGE 업로드 저장소
 *
 * - 저장 경로(로컬): {app.upload-dir}/car-images/{modelId}/{viewType}/
 * - 반환 URL: /uploads/car-images/{modelId}/{viewType}/{filename}
 */
@Service
public class CarImageStorage {

    private final String uploadDir;

    public CarImageStorage(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public static String normalizeViewType(String raw) {
        String v = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (!v.equals("exterior") && !v.equals("con")) {
            throw new IllegalArgumentException("VIEW_TYPE은 exterior 또는 con 만 가능합니다.");
        }
        return v;
    }

    public String save(MultipartFile file, Long modelId, String viewType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("imageFile is empty");
        }
        if (modelId == null) {
            throw new IllegalArgumentException("modelId is required");
        }

        String vt = normalizeViewType(viewType);

        try {
            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
            );

            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);

            String name = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "_" + UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir, "car-images", String.valueOf(modelId), vt)
                    .toAbsolutePath().normalize();
            Files.createDirectories(dir);

            Path target = dir.resolve(name);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/car-images/" + modelId + "/" + vt + "/" + name;

        } catch (IOException e) {
            throw new RuntimeException("차량 이미지 저장 실패", e);
        }
    }

    /**
     * /uploads/** 로컬 파일만 삭제 시도
     */
    public void deleteIfLocal(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        if (!imageUrl.startsWith("/uploads/")) return;

        String relative = imageUrl.substring("/uploads/".length());
        Path p = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(relative).normalize();

        // uploadDir 밖으로 탈출 방지
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!p.startsWith(base)) return;

        try {
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // 파일 삭제 실패는 치명적이지 않으므로 무시
        }
    }
}
