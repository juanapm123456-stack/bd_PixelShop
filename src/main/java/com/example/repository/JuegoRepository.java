package com.example.repository;

import com.example.model.Juego;
import com.example.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface JuegoRepository extends JpaRepository<Juego, Long> {
    List<Juego> findByActivoTrue();
    List<Juego> findByProveedor(Usuario proveedor);
    List<Juego> findByTituloContainingIgnoreCaseAndActivoTrue(String titulo);
    List<Juego> findByGeneroAndActivoTrue(String genero);
    
    @Transactional
    void deleteByProveedor(Usuario proveedor);
}
