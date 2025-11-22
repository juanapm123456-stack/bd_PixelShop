package com.example.repository;

import com.example.model.MovimientoProveedor;
import com.example.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MovimientoProveedorRepository extends JpaRepository<MovimientoProveedor, Long> {
    List<MovimientoProveedor> findByProveedorOrderByFechaDesc(Usuario proveedor);
    List<MovimientoProveedor> findByProveedorAndPagadoFalse(Usuario proveedor);
    
    @Query("SELECT SUM(m.montoNeto) FROM MovimientoProveedor m WHERE m.proveedor = :proveedor AND m.pagado = false")
    BigDecimal calcularIngresosPendientes(@Param("proveedor") Usuario proveedor);
    
    @Transactional
    void deleteByProveedor(Usuario proveedor);
}
