package com.godare.app.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;

    /**
     * Borra un archivo de Cloudinary por su publicId.
     * resourceType: "image" o "video"
     */
    public void deleteFile(String publicId, String resourceType) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            Map result = cloudinary.uploader().destroy(
                    publicId,
                    Map.of("resource_type", resourceType)
            );
            log.info("Cloudinary delete {} ({}): {}", publicId, resourceType, result.get("result"));
        } catch (Exception e) {
            log.error("Error borrando archivo de Cloudinary {}: {}", publicId, e.getMessage());
        }
    }

    /**
     * Infiere el tipo de recurso a partir de la URL de Cloudinary.
     */
    public String inferResourceType(String url) {
        if (url == null) return "image";
        if (url.contains("/video/")) return "video";
        return "image";
    }
}