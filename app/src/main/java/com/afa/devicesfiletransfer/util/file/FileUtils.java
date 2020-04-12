package com.afa.devicesfiletransfer.util.file;

import java.util.Arrays;
import java.util.List;

public class FileUtils {
    private static final String EXTENSION_REGEX = "\\.(?=[^.]+$)";

    private static final List<String> audioExtensions =
            Arrays.asList("wav", "aiff", "mp3", "aac", "ogg", "wma", "flac", "alac");
    private static final List<String> imageExtensions =
            Arrays.asList("jpg", "jpeg", "jpe", "png", "gif", "tif", "tiff", "bmp", "dib",
                    "eps", "raw", "cr2", "nef", "orf", "sr2", "heic");
    private static final List<String> videoExtensions =
            Arrays.asList("webm", "mpg", "mp2", "mpeg", "mpe", "mpv", "mp4", "m4p",
                    "m4v", "avi", "wmv", "mov", "qt", "flv", "swf", "avchd");

    public static FileType getFileType(String fileName) {
        String extension = getFileExtension(fileName);
        if (audioExtensions.contains(extension)) {
            return FileType.AUDIO;
        }
        if (imageExtensions.contains(extension)) {
            return FileType.IMAGE;
        }
        if (videoExtensions.contains(extension)) {
            return FileType.VIDEO;
        }

        return FileType.OTHER;
    }

    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return "";

        return fileName.split(EXTENSION_REGEX)[0];
    }

    public static String getFileExtension(String fileName) {
        String[] fileParts = fileName.split(EXTENSION_REGEX);
        String extension = "";
        if (fileParts.length > 1) {
            extension = fileParts[fileParts.length - 1].toLowerCase();
        }

        return extension;
    }
}
