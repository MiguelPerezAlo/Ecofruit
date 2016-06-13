package com.mycompany.myapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.myapp.domain.Subcategoria;
import com.mycompany.myapp.repository.SubcategoriaRepository;
import com.mycompany.myapp.repository.search.SubcategoriaSearchRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Subcategoria.
 */
@RestController
@RequestMapping("/api")
public class SubcategoriaResource {

    private final Logger log = LoggerFactory.getLogger(SubcategoriaResource.class);
        
    @Inject
    private SubcategoriaRepository subcategoriaRepository;
    
    @Inject
    private SubcategoriaSearchRepository subcategoriaSearchRepository;
    
    /**
     * POST  /subcategorias : Create a new subcategoria.
     *
     * @param subcategoria the subcategoria to create
     * @return the ResponseEntity with status 201 (Created) and with body the new subcategoria, or with status 400 (Bad Request) if the subcategoria has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/subcategorias",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Subcategoria> createSubcategoria(@Valid @RequestBody Subcategoria subcategoria) throws URISyntaxException {
        log.debug("REST request to save Subcategoria : {}", subcategoria);
        if (subcategoria.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("subcategoria", "idexists", "A new subcategoria cannot already have an ID")).body(null);
        }
        Subcategoria result = subcategoriaRepository.save(subcategoria);
        subcategoriaSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/subcategorias/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("subcategoria", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /subcategorias : Updates an existing subcategoria.
     *
     * @param subcategoria the subcategoria to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated subcategoria,
     * or with status 400 (Bad Request) if the subcategoria is not valid,
     * or with status 500 (Internal Server Error) if the subcategoria couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/subcategorias",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Subcategoria> updateSubcategoria(@Valid @RequestBody Subcategoria subcategoria) throws URISyntaxException {
        log.debug("REST request to update Subcategoria : {}", subcategoria);
        if (subcategoria.getId() == null) {
            return createSubcategoria(subcategoria);
        }
        Subcategoria result = subcategoriaRepository.save(subcategoria);
        subcategoriaSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("subcategoria", subcategoria.getId().toString()))
            .body(result);
    }

    /**
     * GET  /subcategorias : get all the subcategorias.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of subcategorias in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/subcategorias",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Subcategoria>> getAllSubcategorias(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Subcategorias");
        Page<Subcategoria> page = subcategoriaRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/subcategorias");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /subcategorias/:id : get the "id" subcategoria.
     *
     * @param id the id of the subcategoria to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the subcategoria, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/subcategorias/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Subcategoria> getSubcategoria(@PathVariable Long id) {
        log.debug("REST request to get Subcategoria : {}", id);
        Subcategoria subcategoria = subcategoriaRepository.findOne(id);
        return Optional.ofNullable(subcategoria)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /subcategorias/:id : delete the "id" subcategoria.
     *
     * @param id the id of the subcategoria to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/subcategorias/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteSubcategoria(@PathVariable Long id) {
        log.debug("REST request to delete Subcategoria : {}", id);
        subcategoriaRepository.delete(id);
        subcategoriaSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("subcategoria", id.toString())).build();
    }

    /**
     * SEARCH  /_search/subcategorias?query=:query : search for the subcategoria corresponding
     * to the query.
     *
     * @param query the query of the subcategoria search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/subcategorias",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Subcategoria>> searchSubcategorias(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Subcategorias for query {}", query);
        Page<Subcategoria> page = subcategoriaSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/subcategorias");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
