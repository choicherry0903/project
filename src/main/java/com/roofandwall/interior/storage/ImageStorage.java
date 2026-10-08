package com.roofandwall.interior.storage;

import java.net.URI;

public interface ImageStorage {
    boolean configured();
    void upload(String key, ImageUpload.Image image);
    void delete(String key);
    URI readUrl(String key);
}
