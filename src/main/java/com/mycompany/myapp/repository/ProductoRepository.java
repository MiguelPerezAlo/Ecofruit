package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Producto;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Producto entity.
 */
@SuppressWarnings("unused")
public interface ProductoRepository extends JpaRepository<Producto,Long> {

    <!-- @Query("select producto from Producto producto, Subcategoria subcategoria where producto.subcategoria.nombre = 'Fruta'")
    Page<Producto> findBySubcategoriaIsFruta(Pageable pageable);-->
}
