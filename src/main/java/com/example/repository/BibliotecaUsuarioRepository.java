package com.example.repository;

import com.example.model.BibliotecaUsuario;
import com.example.model.Usuario;
import com.example.model.Juego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BibliotecaUsuarioRepository extends JpaRepository<BibliotecaUsuario, Long> {
    List<BibliotecaUsuario> findByUsuarioOrderByFechaAdquisicionDesc(Usuario usuario);
    boolean existsByUsuarioAndJuego(Usuario usuario, Juego juego);
}
