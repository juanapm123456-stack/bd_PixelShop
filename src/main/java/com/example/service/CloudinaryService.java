package com.example.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Sube una imagen a Cloudinary y retorna la URL pública
     * 
     * @param file Archivo de imagen a subir
     * @param folder Carpeta donde se guardará (ej: "juegos", "usuarios")
     * @return URL pública de la imagen subida
     * @throws IOException Si hay error en la subida
     */
    public String subirImagen(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        // Validar tamaño (máximo 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("La imagen no puede superar los 5MB");
        }

        try {
            // Subir imagen a Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "quality", "auto",
                    "fetch_format", "auto"
                )
            );

            // Retornar URL segura (HTTPS)
            return (String) uploadResult.get("secure_url");
            
        } catch (IOException e) {
            throw new IOException("Error al subir la imagen a Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Sube múltiples imágenes a Cloudinary
     * 
     * @param files Array de archivos de imagen
     * @param folder Carpeta donde se guardarán
     * @return Array de URLs públicas
     * @throws IOException Si hay error en alguna subida
     */
    public String[] subirMultiplesImagenes(MultipartFile[] files, String folder) throws IOException {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No se proporcionaron archivos");
        }

        String[] urls = new String[files.length];
        
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isEmpty()) {
                urls[i] = subirImagen(files[i], folder);
            }
        }
        
        return urls;
    }

    /**
     * Elimina una imagen de Cloudinary usando su URL
     * 
     * @param imageUrl URL de la imagen a eliminar
     * @throws IOException Si hay error al eliminar
     */
    public void eliminarImagen(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // Extraer el public_id de la URL
            String publicId = extraerPublicId(imageUrl);
            
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                System.out.println("Imagen eliminada de Cloudinary: " + publicId);
            }
            
        } catch (IOException e) {
            throw new IOException("Error al eliminar la imagen de Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Elimina múltiples imágenes de Cloudinary
     * 
     * @param imageUrls Array de URLs de imágenes a eliminar
     */
    public void eliminarMultiplesImagenes(String[] imageUrls) {
        if (imageUrls == null || imageUrls.length == 0) {
            return;
        }

        for (String url : imageUrls) {
            try {
                if (url != null && !url.isEmpty()) {
                    eliminarImagen(url);
                }
            } catch (IOException e) {
                System.err.println("Error al eliminar imagen: " + e.getMessage());
            }
        }
    }

    /**
     * Extrae el public_id de una URL de Cloudinary
     * Ejemplo: https://res.cloudinary.com/demo/image/upload/v1234567890/juegos/imagen.jpg
     * Retorna: juegos/imagen
     */
    private String extraerPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }

        try {
            // Buscar la posición de "/upload/"
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }

            // Extraer la parte después de /upload/vXXXXXXX/
            String afterUpload = imageUrl.substring(uploadIndex + 8);
            
            // Saltar la versión (vXXXXXXX/)
            int slashIndex = afterUpload.indexOf("/");
            if (slashIndex != -1) {
                afterUpload = afterUpload.substring(slashIndex + 1);
            }

            // Eliminar la extensión del archivo
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex != -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }

            return afterUpload;
            
        } catch (Exception e) {
            System.err.println("Error al extraer public_id: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si una URL es de Cloudinary
     */
    public boolean esUrlCloudinary(String url) {
        return url != null && url.contains("cloudinary.com");
    }
}
