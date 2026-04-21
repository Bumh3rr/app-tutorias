package com.bumh3r.service;

import com.bumh3r.service.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStoreService {
    String getUrlBase();
    String save(MultipartFile file, FileType fileType)throws IOException;
    void delete(String ruta, FileType fileType) throws IOException;
}
