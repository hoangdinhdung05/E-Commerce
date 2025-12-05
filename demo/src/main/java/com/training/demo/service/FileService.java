package com.training.demo.service;

import com.training.demo.dto.response.System.UploadResponse;
import com.training.demo.utils.enums.UploadKind;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    /**
     * Upload file
     * @param kind kiểu upload
     * @param file file
     * @return UploadResponse
     * @throws Exception nếu có lỗi xảy ra
     */
    UploadResponse upload(UploadKind kind, MultipartFile file) throws Exception;

    /**
     * Upload file vào thư mục con
     * @param kind kiểu upload
     * @param file file
     * @param subfolder thư mục con
     * @return UploadResponse
     * @throws Exception nếu có lỗi xảy ra
     */
    UploadResponse upload(UploadKind kind, MultipartFile file, String subfolder) throws Exception;

    /**
     * Xoá file theo publicUrl
     * @param publicUrl đường dẫn public của file
     * @throws IOException nếu có lỗi xảy ra
     */
    void deleteByPublicUrl(String publicUrl) throws IOException;
}
