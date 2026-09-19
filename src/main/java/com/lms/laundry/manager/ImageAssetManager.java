package com.lms.laundry.manager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class ImageAssetManager {
    public static final String IMAGE_RESOURCE_PREFIX = "/com/lms/laundry/assets/images/";
    private static final Path IMAGE_DIRECTORY = Path.of(
            "src", "main", "resources", "com", "lms", "laundry", "assets", "images"
    );

    private ImageAssetManager() {}

    public static String saveImage(File sourceFile, String nameHint) throws Exception {
        if (sourceFile == null) return "";
        Files.createDirectories(IMAGE_DIRECTORY);
        String extension = extensionOf(sourceFile.getName());
        String baseName = sanitize(nameHint == null || nameHint.isBlank() ? sourceFile.getName() : nameHint);
        String fileName = baseName + "-" + System.currentTimeMillis() + extension;
        Path target = IMAGE_DIRECTORY.resolve(fileName);
        Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    /**
     * Resolves the resource path for a given filename.
     * Automatically redirects null or blank records to the default placeholder graphic.
     */
    public static String getImage(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return IMAGE_RESOURCE_PREFIX + "no-image.png";
        }
        return IMAGE_RESOURCE_PREFIX + fileName;
    }

    private static String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) return ".png";
        String extension = fileName.substring(index).toLowerCase(Locale.ROOT);
        return extension.matches("\\.(png|jpg|jpeg|gif|webp)") ? extension : ".png";
    }

    private static String sanitize(String value) {
        String sanitized = value.toLowerCase(Locale.ROOT)
                .replaceAll("\\.[a-z0-9]+$", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return sanitized.isBlank() ? "image" : sanitized;
    }
}
