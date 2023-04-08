package com.nix.managecafe.controller;

import com.nix.managecafe.payload.response.ApiResponse;
import com.nix.managecafe.payload.response.ImageResponse;
import com.nix.managecafe.service.ImageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) {
        try {
            String filename = imageService.uploadImage(image);
            return new ResponseEntity<>(new ImageResponse(filename, true), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ApiResponse(false, "upload image failed"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
    @Cacheable("image")
    public ResponseEntity<?> getImage(@PathVariable("imageName") String imageName, HttpServletResponse httpServletResponse) {
        InputStream inputStream = null;
        try {
            inputStream = imageService.getResource(imageName);
            httpServletResponse.setContentType(MediaType.IMAGE_JPEG_VALUE);
            StreamUtils.copy(inputStream, httpServletResponse.getOutputStream());
            return new ResponseEntity<Void>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ApiResponse(false, "file not found"), HttpStatus.BAD_REQUEST);
        }
    }
}
