package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Receta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Receta entity.
 */
@SuppressWarnings("unused")
public interface RecetaRepository extends JpaRepository<Receta,Long> {

    @Query("select receta from Receta receta where receta.user.login = ?#{principal.username}")
    List<Receta> findByUserIsCurrentUser();

   /* @Query("select comentario from Comentario comentario, Receta receta where receta.nombre=comentario.receta")
    Page<Receta> findComentarioByReceta(Pageable pageable);*/

}
