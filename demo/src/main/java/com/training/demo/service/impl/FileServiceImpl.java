package com.training.demo.service.impl;

import com.training.demo.dto.response.System.UploadResponse;
import com.training.demo.service.FileService;
import com.training.demo.utils.enums.UploadKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String BASE_DIR  = "C:/demo-system/"; // Thư mục gốc lưu file
    private static final String BASE_URL  = "/uploads";        // URL public (map ở WebConfig)
    private static final Set<String> ALLOWED = Set.of("jpg","jpeg","png","webp","pdf");

    /**
     * Upload file
     *
     * @param kind kiểu upload
     * @param file file
     * @return UploadResponse
     * @throws Exception nếu có lỗi xảy ra
     */
    @Override
    public UploadResponse upload(UploadKind kind, MultipartFile file) throws Exception {
        return upload(kind, file, null);
    }

    /**
     * Upload file vào thư mục con
     *
     * @param kind      kiểu upload
     * @param file      file
     * @param subfolder thư mục con
     * @return UploadResponse
     * @throws Exception nếu có lỗi xảy ra
     */
    @Override
    public UploadResponse upload(UploadKind kind, MultipartFile file, String subfolder) throws Exception {
        log.info("[FileService] Upload file: {}, kind: {}, subfolder: {}", file.getOriginalFilename(), kind, subfolder);
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (original.contains("..")) throw new IllegalArgumentException("Invalid file name");

        String ext = getExt(original);
        if (!ALLOWED.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + ext);
        }

        // Tổ chức thư mục: <BASE_DIR>/<kind>/<yyyy>/<MM>/<subfolder?>/uuid.ext
        LocalDate now = LocalDate.now();
        String y = String.valueOf(now.getYear());
        String m = (now.getMonthValue() < 10 ? "0" : "") + now.getMonthValue();

        String mid = (StringUtils.hasText(subfolder) ? (normalizeSubfolder(subfolder) + "/") : "");
        String fileName = UUID.randomUUID() + "." + ext.toLowerCase();

        Path dir = Paths.get(BASE_DIR, kind.folder, y, m, mid);
        Path absolutePath = dir.resolve(fileName);

        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), absolutePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        // Tạo public URL: /uploads/<kind>/<yyyy>/<MM>/<subfolder?>/uuid.ext
        String relative = String.join("/", kind.folder, y, m, mid, fileName).replaceAll("//+", "/");
        String publicUrl = (BASE_URL + "/" + relative).replaceAll("//+", "/");

        return UploadResponse.builder()
                .publicUrl(publicUrl)
                .storagePath(absolutePath.toString())
                .fileName(fileName)
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    /**
     * Xoá file theo publicUrl
     *
     * @param publicUrl đường dẫn public của file
     */
    @Override
    public void deleteByPublicUrl(String publicUrl) {
        log.info("[FileService] Delete file by publicUrl: {}", publicUrl);

        if (!StringUtils.hasText(publicUrl)) return;
        // publicUrl dạng: /uploads/<kind>/.../file
        String rel = publicUrl.replace("\\", "/").replaceFirst("^/uploads/?", "");
        Path path = Paths.get(BASE_DIR, rel);
        try { Files.deleteIfExists(path); } catch (Exception ignored) {}
    }

    //========== PRIVATE METHOD ==========//
    private String getExt(String name) {
        int i = name.lastIndexOf('.');
        return (i == -1) ? "" : name.substring(i + 1);
    }

    private String normalizeSubfolder(String s) {
        // Loại bỏ ../ và ký tự không an toàn
        String cleaned = s.replace("\\", "/").replace("..", "");
        if (cleaned.startsWith("/")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("/")) cleaned = cleaned.substring(0, cleaned.length()-1);
        return cleaned;
    }
}
