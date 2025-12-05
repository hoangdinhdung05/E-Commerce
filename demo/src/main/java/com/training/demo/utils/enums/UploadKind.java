package com.training.demo.utils.enums;

public enum UploadKind {

    AVATAR("avatars"),
    PRODUCT_IMAGE("products");

    public final String folder;

    UploadKind(String folder) {
        this.folder = folder;
    }
}
