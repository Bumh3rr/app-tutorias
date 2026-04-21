package com.bumh3r.service.impl;

import com.bumh3r.service.FileStoreService;
import com.bumh3r.service.enums.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStoreServiceImp implements FileStoreService {
    private static final Logger log = LoggerFactory.getLogger(FileStoreServiceImp.class);

    @Value("${file.upload.dir}")
    private String urlBase;

    @Override
    public String save(MultipartFile file, FileType fileType) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String ruta = String.format("%s/%s",this.urlBase,fileType.getValue()); // <- Se obtiene la ruta de subida del archivo

        Path uploadPath = Paths.get(ruta);
        if (Files.notExists(uploadPath))
            Files.createDirectory(uploadPath);

        String orinalFilename = file.getOriginalFilename(); // <- retorna el nombre del archivo originalmente
        String fileExtension = orinalFilename.substring(orinalFilename.lastIndexOf(".")); // <- retorna la extension del archivo
        String storedFileName = String.format("%s%s", UUID.randomUUID(), fileExtension); // retorna el nuevo nombre del archivo a guardar

        // Guardar archivo
        File fileSave = new  File(uploadPath.toFile(), storedFileName);
        file.transferTo(fileSave); // <- Guardar

        return storedFileName;
    }

    @Override
    public void delete(String filePath, FileType fileType) {
        if (filePath == null || filePath.isBlank()) return;

        try {
            Path base = Paths.get(this.urlBase).toAbsolutePath().normalize();
            Path typeDir = base.resolve(fileType.getValue()).normalize();
            Path target = typeDir.resolve(filePath).normalize();

            Files.deleteIfExists(target); // <- Elimina el archivo si existe
        } catch (Exception e) {
            log.error("Delete file exception", e);
        }
    }

    @Override
    public String getUrlBase() {
        return this.urlBase;
    }

}
