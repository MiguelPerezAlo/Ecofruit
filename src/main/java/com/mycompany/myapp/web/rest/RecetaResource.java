package com.mycompany.myapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.myapp.domain.Receta;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.RecetaRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.repository.search.RecetaSearchRepository;
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
 * REST controller for managing Receta.
 */
@RestController
@RequestMapping("/api")
public class RecetaResource {

    private final Logger log = LoggerFactory.getLogger(RecetaResource.class);

    @Inject
    private RecetaRepository recetaRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private RecetaSearchRepository recetaSearchRepository;

    /**
     * POST  /recetas : Create a new receta.
     *
     * @param receta the receta to create
     * @return the ResponseEntity with status 201 (Created) and with body the new receta, or with status 400 (Bad Request) if the receta has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/recetas",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Receta> createReceta(@Valid @RequestBody Receta receta) throws URISyntaxException {
        log.debug("REST request to save Receta : {}", receta);
        if (receta.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("receta", "idexists", "A new receta cannot already have an ID")).body(null);
        }
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        receta.setUser(user);
        ZonedDateTime dia = ZonedDateTime.now(ZoneId.systemDefault());
        receta.setFecha(dia);
        Receta result = recetaRepository.save(receta);
        recetaSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/recetas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("receta", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /recetas : Updates an existing receta.
     *
     * @param receta the receta to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated receta,
     * or with status 400 (Bad Request) if the receta is not valid,
     * or with status 500 (Internal Server Error) if the receta couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/recetas",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Receta> updateReceta(@Valid @RequestBody Receta receta) throws URISyntaxException {
        log.debug("REST request to update Receta : {}", receta);
        if (receta.getId() == null) {
            return createReceta(receta);
        }
        Receta result = recetaRepository.save(receta);
        recetaSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("receta", receta.getId().toString()))
            .body(result);
    }

    /**
     * GET  /recetas : get all the recetas.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of recetas in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/recetas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Receta>> getAllRecetas(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Recetas");
        Page<Receta> page = recetaRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/recetas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/Misrecetas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Receta>> getMisRecetas(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Recetas");
        Page<Receta> page = recetaRepository.findByUserIsCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/recetas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

   /* @RequestMapping(value = "/recetasComentarios",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Receta>> getRecetasComentarios(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Recetas");
        Page<Receta> page = recetaRepository.findComentarioByReceta(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/recetas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }*/
    /**
     * GET  /recetas/:id : get the "id" receta.
     *
     * @param id the id of the receta to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the receta, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/recetas/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Receta> getReceta(@PathVariable Long id) {
        log.debug("REST request to get Receta : {}", id);
        Receta receta = recetaRepository.findOne(id);
        return Optional.ofNullable(receta)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /recetas/:id : delete the "id" receta.
     *
     * @param id the id of the receta to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/recetas/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteReceta(@PathVariable Long id) {
        log.debug("REST request to delete Receta : {}", id);
        recetaRepository.delete(id);
        recetaSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("receta", id.toString())).build();
    }

    /**
     * SEARCH  /_search/recetas?query=:query : search for the receta corresponding
     * to the query.
     *
     * @param query the query of the receta search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/recetas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Receta>> searchRecetas(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Recetas for query {}", query);
        Page<Receta> page = recetaSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/recetas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
