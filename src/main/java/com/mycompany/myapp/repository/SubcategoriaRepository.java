package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Subcategoria;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Subcategoria entity.
 */
@SuppressWarnings("unused")
public interface SubcategoriaRepository extends JpaRepository<Subcategoria,Long> {

}
