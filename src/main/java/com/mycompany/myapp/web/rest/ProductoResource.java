package com.mycompany.myapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.myapp.domain.Producto;
import com.mycompany.myapp.repository.ProductoRepository;
import com.mycompany.myapp.repository.search.ProductoSearchRepository;
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
 * REST controller for managing Producto.
 */
@RestController
@RequestMapping("/api")
public class ProductoResource {

    private final Logger log = LoggerFactory.getLogger(ProductoResource.class);
        
    @Inject
    private ProductoRepository productoRepository;
    
    @Inject
    private ProductoSearchRepository productoSearchRepository;
    
    /**
     * POST  /productos : Create a new producto.
     *
     * @param producto the producto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new producto, or with status 400 (Bad Request) if the producto has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/productos",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody Producto producto) throws URISyntaxException {
        log.debug("REST request to save Producto : {}", producto);
        if (producto.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("producto", "idexists", "A new producto cannot already have an ID")).body(null);
        }
        Producto result = productoRepository.save(producto);
        productoSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/productos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("producto", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /productos : Updates an existing producto.
     *
     * @param producto the producto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated producto,
     * or with status 400 (Bad Request) if the producto is not valid,
     * or with status 500 (Internal Server Error) if the producto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/productos",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Producto> updateProducto(@Valid @RequestBody Producto producto) throws URISyntaxException {
        log.debug("REST request to update Producto : {}", producto);
        if (producto.getId() == null) {
            return createProducto(producto);
        }
        Producto result = productoRepository.save(producto);
        productoSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("producto", producto.getId().toString()))
            .body(result);
    }

    /**
     * GET  /productos : get all the productos.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of productos in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/productos",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Producto>> getAllProductos(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Productos");
        Page<Producto> page = productoRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/productos");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /productos/:id : get the "id" producto.
     *
     * @param id the id of the producto to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the producto, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/productos/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Producto> getProducto(@PathVariable Long id) {
        log.debug("REST request to get Producto : {}", id);
        Producto producto = productoRepository.findOne(id);
        return Optional.ofNullable(producto)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /productos/:id : delete the "id" producto.
     *
     * @param id the id of the producto to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/productos/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        log.debug("REST request to delete Producto : {}", id);
        productoRepository.delete(id);
        productoSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("producto", id.toString())).build();
    }

    /**
     * SEARCH  /_search/productos?query=:query : search for the producto corresponding
     * to the query.
     *
     * @param query the query of the producto search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/productos",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Producto>> searchProductos(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Productos for query {}", query);
        Page<Producto> page = productoSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/productos");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
