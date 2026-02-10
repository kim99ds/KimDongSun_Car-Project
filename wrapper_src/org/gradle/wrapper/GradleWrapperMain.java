package org.gradle.wrapper;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Minimal Gradle Wrapper implementation.
 * - Reads gradle/wrapper/gradle-wrapper.properties (distributionUrl)
 * - Downloads Gradle distribution zip into ~/.gradle/wrapper/dists
 * - Unzips if needed
 * - Executes Gradle 'bin/gradle' (or 'bin/gradle.bat' on Windows) with passed args
 */
public class GradleWrapperMain {

    public static void main(String[] args) throws Exception {
        Path projectDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path propsPath = projectDir.resolve("gradle").resolve("wrapper").resolve("gradle-wrapper.properties");

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(propsPath)) {
            props.load(in);
        }

        String distUrl = props.getProperty("distributionUrl");
        if (distUrl == null || distUrl.isBlank()) {
            throw new IllegalStateException("distributionUrl is missing in " + propsPath);
        }

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        Path gradleHome = ensureDistribution(distUrl);
        Path gradleExec = gradleHome.resolve("bin").resolve(isWindows ? "gradle.bat" : "gradle");

        if (!Files.exists(gradleExec)) {
            throw new FileNotFoundException("Gradle executable not found: " + gradleExec);
        }

        ProcessBuilder pb = new ProcessBuilder();
        pb.command().add(gradleExec.toString());
        for (String a : args) pb.command().add(a);
        pb.directory(projectDir.toFile());
        pb.inheritIO();

        Process p = pb.start();
        int code = p.waitFor();
        System.exit(code);
    }

    private static Path ensureDistribution(String distUrl) throws Exception {
        // Gradle wrapper standard location:
        // ~/.gradle/wrapper/dists/<distName>/<hash>/<distName>/
        String distName = distUrl.substring(distUrl.lastIndexOf('/') + 1).replace(".zip", "");
        String urlHash = sha256Hex(distUrl).substring(0, 16);

        Path userHome = Paths.get(System.getProperty("user.home"));
        Path distsDir = userHome.resolve(".gradle").resolve("wrapper").resolve("dists");
        Path distBaseDir = distsDir.resolve(distName).resolve(urlHash);

        // Find an extracted folder containing "bin"
        if (Files.exists(distBaseDir)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(distBaseDir)) {
                for (Path p : ds) {
                    if (Files.isDirectory(p) && Files.isDirectory(p.resolve("bin"))) {
                        return p;
                    }
                }
            }
        }

        Files.createDirectories(distBaseDir);

        Path zipPath = distBaseDir.resolve(distName + ".zip");
        if (!Files.exists(zipPath) || Files.size(zipPath) == 0) {
            download(distUrl, zipPath);
        }

        // Unzip into distBaseDir
        unzip(zipPath, distBaseDir);

        // Return extracted folder
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(distBaseDir)) {
            for (Path p : ds) {
                if (Files.isDirectory(p) && Files.isDirectory(p.resolve("bin"))) {
                    return p;
                }
            }
        }
        throw new IllegalStateException("Failed to extract Gradle distribution from: " + zipPath);
    }

    private static void download(String url, Path target) throws Exception {
        URL u = new URL(url.replace("\\:", ":").replace("\\/", "/"));
        URLConnection conn = u.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);

        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) >= 0) {
                out.write(buf, 0, r);
            }
        }
    }

    private static void unzip(Path zipFile, Path destDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                Path outPath = destDir.resolve(entry.getName()).normalize();
                if (!outPath.startsWith(destDir)) {
                    throw new SecurityException("Zip entry is outside target dir: " + entry.getName());
                }
                Files.createDirectories(outPath.getParent());
                try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(outPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = zis.read(buf)) >= 0) {
                        out.write(buf, 0, r);
                    }
                }
            }
        }
    }

    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(s.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : dig) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
