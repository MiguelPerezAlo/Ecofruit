package com.mycompany.myapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.myapp.domain.Marca;
import com.mycompany.myapp.repository.MarcaRepository;
import com.mycompany.myapp.repository.search.MarcaSearchRepository;
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
 * REST controller for managing Marca.
 */
@RestController
@RequestMapping("/api")
public class MarcaResource {

    private final Logger log = LoggerFactory.getLogger(MarcaResource.class);
        
    @Inject
    private MarcaRepository marcaRepository;
    
    @Inject
    private MarcaSearchRepository marcaSearchRepository;
    
    /**
     * POST  /marcas : Create a new marca.
     *
     * @param marca the marca to create
     * @return the ResponseEntity with status 201 (Created) and with body the new marca, or with status 400 (Bad Request) if the marca has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/marcas",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Marca> createMarca(@Valid @RequestBody Marca marca) throws URISyntaxException {
        log.debug("REST request to save Marca : {}", marca);
        if (marca.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("marca", "idexists", "A new marca cannot already have an ID")).body(null);
        }
        Marca result = marcaRepository.save(marca);
        marcaSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/marcas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("marca", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /marcas : Updates an existing marca.
     *
     * @param marca the marca to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated marca,
     * or with status 400 (Bad Request) if the marca is not valid,
     * or with status 500 (Internal Server Error) if the marca couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/marcas",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Marca> updateMarca(@Valid @RequestBody Marca marca) throws URISyntaxException {
        log.debug("REST request to update Marca : {}", marca);
        if (marca.getId() == null) {
            return createMarca(marca);
        }
        Marca result = marcaRepository.save(marca);
        marcaSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("marca", marca.getId().toString()))
            .body(result);
    }

    /**
     * GET  /marcas : get all the marcas.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of marcas in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/marcas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Marca>> getAllMarcas(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Marcas");
        Page<Marca> page = marcaRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/marcas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /marcas/:id : get the "id" marca.
     *
     * @param id the id of the marca to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the marca, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/marcas/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Marca> getMarca(@PathVariable Long id) {
        log.debug("REST request to get Marca : {}", id);
        Marca marca = marcaRepository.findOne(id);
        return Optional.ofNullable(marca)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /marcas/:id : delete the "id" marca.
     *
     * @param id the id of the marca to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/marcas/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteMarca(@PathVariable Long id) {
        log.debug("REST request to delete Marca : {}", id);
        marcaRepository.delete(id);
        marcaSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("marca", id.toString())).build();
    }

    /**
     * SEARCH  /_search/marcas?query=:query : search for the marca corresponding
     * to the query.
     *
     * @param query the query of the marca search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/marcas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Marca>> searchMarcas(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Marcas for query {}", query);
        Page<Marca> page = marcaSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/marcas");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
