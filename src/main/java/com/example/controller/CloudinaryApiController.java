package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * API REST para subir imágenes a Cloudinary
 */
@RestController
@RequestMapping("/api/cloudinary")
@PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN')")
public class CloudinaryApiController {

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Sube una imagen individual a Cloudinary
     * POST /api/cloudinary/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "juegos") String folder) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("El archivo está vacío"));
            }

            // Subir a Cloudinary
            String imageUrl = cloudinaryService.subirImagen(file, folder);

            // Preparar respuesta
            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            data.put("filename", file.getOriginalFilename());

            return ResponseEntity
                .ok()
                .body(ApiResponse.success("Imagen subida correctamente", data));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al subir la imagen: " + e.getMessage()));
        }
    }

    /**
     * Sube múltiples imágenes a Cloudinary
     * POST /api/cloudinary/upload-multiple
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", defaultValue = "juegos") String folder) {
        
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("No se proporcionaron archivos"));
            }

            // Subir todas las imágenes
            String[] imageUrls = cloudinaryService.subirMultiplesImagenes(files, folder);

            // Preparar respuesta
            Map<String, Object> data = new HashMap<>();
            data.put("urls", imageUrls);
            data.put("count", imageUrls.length);

            return ResponseEntity
                .ok()
                .body(ApiResponse.success("Imágenes subidas correctamente", data));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al subir las imágenes: " + e.getMessage()));
        }
    }

    /**
     * Elimina una imagen de Cloudinary
     * DELETE /api/cloudinary/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("URL de imagen no proporcionada"));
            }

            cloudinaryService.eliminarImagen(imageUrl);

            return ResponseEntity
                .ok()
                .body(ApiResponse.success("Imagen eliminada correctamente", null));

        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al eliminar la imagen: " + e.getMessage()));
        }
    }
}
