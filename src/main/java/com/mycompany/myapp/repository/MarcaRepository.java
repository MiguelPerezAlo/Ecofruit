package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Marca;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Marca entity.
 */
@SuppressWarnings("unused")
public interface MarcaRepository extends JpaRepository<Marca,Long> {

}
