package com.mycompany.myapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.myapp.domain.Mensaje;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.MensajeRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.repository.search.MensajeSearchRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.web.rest.util.HeaderUtil;
import com.mycompany.myapp.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Mensaje.
 */
@RestController
@RequestMapping("/api")
public class MensajeResource {

    private final Logger log = LoggerFactory.getLogger(MensajeResource.class);

    @Inject
    private MensajeRepository mensajeRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private MensajeSearchRepository mensajeSearchRepository;

    /**
     * POST  /mensajes : Create a new mensaje.
     *
     * @param mensaje the mensaje to create
     * @return the ResponseEntity with status 201 (Created) and with body the new mensaje, or with status 400 (Bad Request) if the mensaje has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/mensajes",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Mensaje> createMensaje(@Valid @RequestBody Mensaje mensaje) throws URISyntaxException {
        log.debug("REST request to save Mensaje : {}", mensaje);
        if (mensaje.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("mensaje", "idexists", "A new mensaje cannot already have an ID")).body(null);
        }

        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        mensaje.setEmisor(user);
        ZonedDateTime dia = ZonedDateTime.now(ZoneId.systemDefault());
        mensaje.setFecha(dia);
        Mensaje result = mensajeRepository.save(mensaje);
        mensajeSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/mensajes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("mensaje", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /mensajes : Updates an existing mensaje.
     *
     * @param mensaje the mensaje to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated mensaje,
     * or with status 400 (Bad Request) if the mensaje is not valid,
     * or with status 500 (Internal Server Error) if the mensaje couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/mensajes",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Mensaje> updateMensaje(@Valid @RequestBody Mensaje mensaje) throws URISyntaxException {
        log.debug("REST request to update Mensaje : {}", mensaje);
        if (mensaje.getId() == null) {
            return createMensaje(mensaje);
        }
        Mensaje result = mensajeRepository.save(mensaje);
        mensajeSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("mensaje", mensaje.getId().toString()))
            .body(result);
    }

    /**
     * GET  /mensajes : get all the mensajes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of mensajes in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/mensajes",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Mensaje>> getAllMensajes(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Mensajes");
        Page<Mensaje> page = mensajeRepository.findByReceptorIsCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/mensajes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/mensajesEnviados",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Mensaje>> getMensajesEnviados(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Mensajes");
        Page<Mensaje> page = mensajeRepository.findByEmisorIsCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/mensajes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /mensajes/:id : get the "id" mensaje.
     *
     * @param id the id of the mensaje to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the mensaje, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/mensajes/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Mensaje> getMensaje(@PathVariable Long id) {
        log.debug("REST request to get Mensaje : {}", id);
        Mensaje mensaje = mensajeRepository.findOne(id);
        return Optional.ofNullable(mensaje)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /mensajes/:id : delete the "id" mensaje.
     *
     * @param id the id of the mensaje to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/mensajes/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteMensaje(@PathVariable Long id) {
        log.debug("REST request to delete Mensaje : {}", id);
        mensajeRepository.delete(id);
        mensajeSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("mensaje", id.toString())).build();
    }

    /**
     * SEARCH  /_search/mensajes?query=:query : search for the mensaje corresponding
     * to the query.
     *
     * @param query the query of the mensaje search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/mensajes",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Mensaje>> searchMensajes(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Mensajes for query {}", query);
        Page<Mensaje> page = mensajeSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/mensajes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
