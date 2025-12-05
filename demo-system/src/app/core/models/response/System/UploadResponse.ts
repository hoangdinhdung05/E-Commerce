export interface UploadResponse {
    publicUrl: string;      // URL công khai để truy cập file
    storagePath: string;    // Đường dẫn lưu trữ trên server
    fileName: string;       // Tên file sau khi lưu
    size: number;           // Kích thước file (bytes)
    contentType: string;    // Loại MIME của file
}
