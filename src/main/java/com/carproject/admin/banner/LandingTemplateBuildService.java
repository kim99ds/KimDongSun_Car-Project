package com.carproject.admin.banner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
public class LandingTemplateBuildService {

    private final Path tplDir = Paths.get("./uploads/landing-tpl");

    public String buildTemplate(Path originalHtmlPath, String pageId, String publicBaseUrl) throws Exception {

        String html = Files.readString(originalHtmlPath, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);

        // 기존 header 제거 (프론트 정적 header 제거)
        doc.select("header").remove();

        // ✅ 상대경로 보정 (CSS/JS/IMG/VIDEO/POSTER 등)
        doc.select("link[href]").forEach(el -> fixUrl(el, "href", publicBaseUrl));
        doc.select("script[src]").forEach(el -> fixUrl(el, "src", publicBaseUrl));
        doc.select("img[src]").forEach(el -> fixUrl(el, "src", publicBaseUrl));

        // ✅ 비디오 관련 경로 보정 (추가)
        doc.select("video[src]").forEach(el -> fixUrl(el, "src", publicBaseUrl));
        doc.select("source[src]").forEach(el -> fixUrl(el, "src", publicBaseUrl));
        doc.select("video[poster]").forEach(el -> fixUrl(el, "poster", publicBaseUrl));
        doc.select("[data-video]").forEach(el -> fixDataUrl(el, "data-video", publicBaseUrl));

        // ✅ 공통 헤더/푸터 삽입
        Element body = doc.body();

        // body 최상단에 header
        body.prepend("""
            <th:block th:replace="fragments/header :: header"></th:block>
        """);

        // body 마지막에 footer
        body.append("""
            <th:block th:replace="fragments/footer :: footer"></th:block>
        """);

        // 템플릿 저장
        Files.createDirectories(tplDir);

        Path out = tplDir.resolve(pageId + ".html");

        Files.writeString(out, doc.outerHtml(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        return pageId;
    }

    private void fixUrl(Element el, String attr, String base) {

        String v = el.attr(attr);

        if (v == null || v.isBlank()) return;

        // 외부 링크 / data URI는 그대로 둠
        if (v.startsWith("http://") || v.startsWith("https://") || v.startsWith("//") || v.startsWith("data:")) return;

        // 이미 절대경로면 그대로 둠
        if (v.startsWith("/")) return;

        // "./" 제거
        while (v.startsWith("./")) v = v.substring(2);

        // 상대경로 -> 업로드 baseUrl 붙이기
        el.attr(attr, base + v);
    }
    private void fixDataUrl(Element el, String attr, String base) {

        String v = el.attr(attr);

        if (v == null || v.isBlank()) return;
        if (v.startsWith("http://") || v.startsWith("https://") || v.startsWith("//") || v.startsWith("data:")) return;
        if (v.startsWith("/")) return;

        while (v.startsWith("./")) v = v.substring(2);

        el.attr(attr, base + v);
    }
}
