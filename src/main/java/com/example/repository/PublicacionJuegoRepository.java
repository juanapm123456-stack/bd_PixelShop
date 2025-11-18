package com.example.repository;

import com.example.model.PublicacionJuego;
import com.example.model.Juego;
import com.example.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PublicacionJuegoRepository extends JpaRepository<PublicacionJuego, Long> {
    Optional<PublicacionJuego> findByJuego(Juego juego);
    List<PublicacionJuego> findByProveedorOrderByFechaPublicacionDesc(Usuario proveedor);
    
    @Query("SELECT COUNT(p) FROM PublicacionJuego p WHERE p.pagado = true")
    Long contarPublicacionesPagadas();
}
