package com.carproject.admin.banner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class LandingFileStorage {

    private final String uploadDir;
    private final LandingTemplateBuildService templateBuildService;

    public LandingFileStorage(
            @Value("${app.upload-dir:uploads}") String uploadDir,
            LandingTemplateBuildService templateBuildService
    ) {
        this.uploadDir = uploadDir;
        this.templateBuildService = templateBuildService;
    }

    // =========================
    // 배너 이미지 저장
    // =========================
    public String save(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("imageFile is empty");
        }

        try {
            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
            );

            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);

            String name = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "_" + UUID.randomUUID() + ext;

            Path dir = Paths.get(uploadDir, "landing").toAbsolutePath().normalize();
            Files.createDirectories(dir);

            Path target = dir.resolve(name);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/landing/" + name;

        } catch (IOException e) {
            throw new RuntimeException("랜딩 배너 이미지 저장 실패", e);
        }
    }

    // =========================
    // 랜딩 페이지 ZIP 업로드 (동적 템플릿 변환)
    // =========================
    public String savePageZip(MultipartFile zipFile) {

        if (zipFile == null || zipFile.isEmpty()) {
            throw new IllegalArgumentException("pageZip is empty");
        }

        String original = Optional.ofNullable(zipFile.getOriginalFilename())
                .orElse("")
                .toLowerCase(Locale.ROOT);

        if (!original.endsWith(".zip")) {
            throw new IllegalArgumentException("zip 파일만 업로드 가능합니다.");
        }

        String pageId = UUID.randomUUID().toString();

        Path baseDir = Paths.get(uploadDir, "landing-pages").toAbsolutePath().normalize();
        Path destDir = baseDir.resolve(pageId);

        try {
            Files.createDirectories(destDir);

            Path tmpZip = Files.createTempFile("landing-page-", ".zip");
            zipFile.transferTo(tmpZip.toFile());

            try (ZipFile zf = new ZipFile(tmpZip.toFile())) {

                String stripPrefix = detectSingleTopFolderPrefix(zf);

                unzipWithOptionalStrip(zf, destDir, stripPrefix);

                Path entryHtml = findEntryHtml(destDir);

                if (entryHtml == null) {
                    deleteDirectory(destDir);
                    throw new IllegalArgumentException("ZIP 안에서 HTML 파일을 찾을 수 없습니다.");
                }

                // ✅ 동적 템플릿 변환
                String publicBaseUrl = "/uploads/landing-pages/" + pageId + "/";

                templateBuildService.buildTemplate(
                        entryHtml,
                        pageId,
                        publicBaseUrl
                );

                // ✅ 반환 URL
                return "/landing/" + pageId;

            } finally {
                Files.deleteIfExists(tmpZip);
            }

        } catch (Exception e) {
            try { deleteDirectory(destDir); } catch (Exception ignore) {}
            throw new RuntimeException("HTML 페이지 ZIP 저장 실패", e);
        }
    }

    // =========================
    // ZIP 최상위 폴더 제거
    // =========================
    private String detectSingleTopFolderPrefix(ZipFile zf) {

        Set<String> top = new HashSet<>();

        Enumeration<? extends ZipEntry> en = zf.entries();

        while (en.hasMoreElements()) {
            ZipEntry entry = en.nextElement();
            String name = entry.getName();

            if (name == null || name.startsWith("__MACOSX/")) continue;

            String normalized = name.replace("\\", "/");
            int idx = normalized.indexOf('/');

            if (idx <= 0) return null;

            String first = normalized.substring(0, idx);
            top.add(first);

            if (top.size() > 1) return null;
        }

        if (top.size() == 1) {
            return top.iterator().next() + "/";
        }

        return null;
    }

    private void unzipWithOptionalStrip(ZipFile zf, Path destDir, String stripPrefix) throws IOException {

        Enumeration<? extends ZipEntry> en = zf.entries();

        while (en.hasMoreElements()) {

            ZipEntry entry = en.nextElement();

            if (entry.isDirectory()) continue;

            String name = entry.getName();
            if (name == null) continue;

            String normalized = name.replace("\\", "/");

            if (normalized.startsWith("__MACOSX/")) continue;

            if (stripPrefix != null && normalized.startsWith(stripPrefix)) {
                normalized = normalized.substring(stripPrefix.length());
            }

            if (normalized.isBlank()) continue;

            if (!isAllowedAsset(normalized.toLowerCase(Locale.ROOT))) continue;

            Path target = destDir.resolve(normalized).normalize();

            if (!target.startsWith(destDir)) {
                throw new IllegalArgumentException("잘못된 ZIP 경로 포함");
            }

            Files.createDirectories(target.getParent());

            try (InputStream is = zf.getInputStream(entry);
                 OutputStream os = Files.newOutputStream(target)) {

                is.transferTo(os);
            }
        }
    }

    private boolean isAllowedAsset(String name) {

        return name.endsWith(".html")
                || name.endsWith(".css")
                || name.endsWith(".js")
                || name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".webp")
                || name.endsWith(".gif")
                || name.endsWith(".svg")
                || name.endsWith(".woff")
                || name.endsWith(".woff2")
                || name.endsWith(".ttf")
                || name.endsWith(".otf")
                || name.endsWith(".map")
                // ✅ 비디오 허용
                || name.endsWith(".mp4")
                || name.endsWith(".webm")
                || name.endsWith(".mov");
    }

    private Path findEntryHtml(Path destDir) throws IOException {

        Path direct = destDir.resolve("index.html");
        if (Files.exists(direct)) return direct;

        return Files.walk(destDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".html"))
                .findFirst()
                .orElse(null);
    }

    private void deleteDirectory(Path dir) throws IOException {

        if (!Files.exists(dir)) return;

        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.deleteIfExists(p); }
                    catch (IOException ignored) {}
                });
    }
}
