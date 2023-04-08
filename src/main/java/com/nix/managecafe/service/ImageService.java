package com.nix.managecafe.service;

import com.nix.managecafe.util.AppConstants;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {
    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String randomId = UUID.randomUUID().toString();
        String name = null;
        if (fileName != null) {
            name = randomId.concat(fileName.substring(fileName.lastIndexOf(".")));
        }
        String filePath = AppConstants.IMAGE_PATH + File.separator + name;

        File f = new File(AppConstants.IMAGE_PATH);
        if (!f.exists()) {
            f.mkdir();
        }
        Files.copy(file.getInputStream(), Paths.get(filePath));

        name = "http://localhost:8080/api/v1/images/" + name;
        return name;
    }
    public InputStream getResource(String fileName) throws FileNotFoundException {
        String fullPath = AppConstants.IMAGE_PATH + File.separator + fileName;
        return new FileInputStream(fullPath);
    }

}
