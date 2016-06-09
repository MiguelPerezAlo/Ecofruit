package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Mensaje;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Mensaje entity.
 */
@SuppressWarnings("unused")
public interface MensajeRepository extends JpaRepository<Mensaje,Long> {

    @Query("select mensaje from Mensaje mensaje where mensaje.emisor.login = ?#{principal.username}")
    Page<Mensaje> findByEmisorIsCurrentUser(Pageable pageable);

    @Query("select mensaje from Mensaje mensaje where mensaje.receptor.login = ?#{principal.username}")
    Page<Mensaje> findByReceptorIsCurrentUser(Pageable pageable);

}
