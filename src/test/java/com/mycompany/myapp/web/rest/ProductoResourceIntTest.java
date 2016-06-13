package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.EcofruitApp;
import com.mycompany.myapp.domain.Producto;
import com.mycompany.myapp.repository.ProductoRepository;
import com.mycompany.myapp.repository.search.ProductoSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the ProductoResource REST controller.
 *
 * @see ProductoResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EcofruitApp.class)
@WebAppConfiguration
@IntegrationTest
public class ProductoResourceIntTest {

    private static final String DEFAULT_NOMBRE = "AAAAA";
    private static final String UPDATED_NOMBRE = "BBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBB";

    private static final byte[] DEFAULT_IMAGEN = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGEN = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_IMAGEN_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGEN_CONTENT_TYPE = "image/png";

    private static final Integer DEFAULT_PRECIO = 1;
    private static final Integer UPDATED_PRECIO = 2;

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private ProductoSearchRepository productoSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restProductoMockMvc;

    private Producto producto;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ProductoResource productoResource = new ProductoResource();
        ReflectionTestUtils.setField(productoResource, "productoSearchRepository", productoSearchRepository);
        ReflectionTestUtils.setField(productoResource, "productoRepository", productoRepository);
        this.restProductoMockMvc = MockMvcBuilders.standaloneSetup(productoResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        productoSearchRepository.deleteAll();
        producto = new Producto();
        producto.setNombre(DEFAULT_NOMBRE);
        producto.setDescripcion(DEFAULT_DESCRIPCION);
        producto.setImagen(DEFAULT_IMAGEN);
        producto.setImagenContentType(DEFAULT_IMAGEN_CONTENT_TYPE);
        producto.setPrecio(DEFAULT_PRECIO);
    }

    @Test
    @Transactional
    public void createProducto() throws Exception {
        int databaseSizeBeforeCreate = productoRepository.findAll().size();

        // Create the Producto

        restProductoMockMvc.perform(post("/api/productos")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(producto)))
                .andExpect(status().isCreated());

        // Validate the Producto in the database
        List<Producto> productos = productoRepository.findAll();
        assertThat(productos).hasSize(databaseSizeBeforeCreate + 1);
        Producto testProducto = productos.get(productos.size() - 1);
        assertThat(testProducto.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testProducto.getDescripcion()).isEqualTo(DEFAULT_DESCRIPCION);
        assertThat(testProducto.getImagen()).isEqualTo(DEFAULT_IMAGEN);
        assertThat(testProducto.getImagenContentType()).isEqualTo(DEFAULT_IMAGEN_CONTENT_TYPE);
        assertThat(testProducto.getPrecio()).isEqualTo(DEFAULT_PRECIO);

        // Validate the Producto in ElasticSearch
        Producto productoEs = productoSearchRepository.findOne(testProducto.getId());
        assertThat(productoEs).isEqualToComparingFieldByField(testProducto);
    }

    @Test
    @Transactional
    public void checkNombreIsRequired() throws Exception {
        int databaseSizeBeforeTest = productoRepository.findAll().size();
        // set the field null
        producto.setNombre(null);

        // Create the Producto, which fails.

        restProductoMockMvc.perform(post("/api/productos")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(producto)))
                .andExpect(status().isBadRequest());

        List<Producto> productos = productoRepository.findAll();
        assertThat(productos).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDescripcionIsRequired() throws Exception {
        int databaseSizeBeforeTest = productoRepository.findAll().size();
        // set the field null
        producto.setDescripcion(null);

        // Create the Producto, which fails.

        restProductoMockMvc.perform(post("/api/productos")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(producto)))
                .andExpect(status().isBadRequest());

        List<Producto> productos = productoRepository.findAll();
        assertThat(productos).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPrecioIsRequired() throws Exception {
        int databaseSizeBeforeTest = productoRepository.findAll().size();
        // set the field null
        producto.setPrecio(null);

        // Create the Producto, which fails.

        restProductoMockMvc.perform(post("/api/productos")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(producto)))
                .andExpect(status().isBadRequest());

        List<Producto> productos = productoRepository.findAll();
        assertThat(productos).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllProductos() throws Exception {
        // Initialize the database
        productoRepository.saveAndFlush(producto);

        // Get all the productos
        restProductoMockMvc.perform(get("/api/productos?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(producto.getId().intValue())))
                .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
                .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())))
                .andExpect(jsonPath("$.[*].imagenContentType").value(hasItem(DEFAULT_IMAGEN_CONTENT_TYPE)))
                .andExpect(jsonPath("$.[*].imagen").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGEN))))
                .andExpect(jsonPath("$.[*].precio").value(hasItem(DEFAULT_PRECIO)));
    }

    @Test
    @Transactional
    public void getProducto() throws Exception {
        // Initialize the database
        productoRepository.saveAndFlush(producto);

        // Get the producto
        restProductoMockMvc.perform(get("/api/productos/{id}", producto.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(producto.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE.toString()))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION.toString()))
            .andExpect(jsonPath("$.imagenContentType").value(DEFAULT_IMAGEN_CONTENT_TYPE))
            .andExpect(jsonPath("$.imagen").value(Base64Utils.encodeToString(DEFAULT_IMAGEN)))
            .andExpect(jsonPath("$.precio").value(DEFAULT_PRECIO));
    }

    @Test
    @Transactional
    public void getNonExistingProducto() throws Exception {
        // Get the producto
        restProductoMockMvc.perform(get("/api/productos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProducto() throws Exception {
        // Initialize the database
        productoRepository.saveAndFlush(producto);
        productoSearchRepository.save(producto);
        int databaseSizeBeforeUpdate = productoRepository.findAll().size();

        // Update the producto
        Producto updatedProducto = new Producto();
        updatedProducto.setId(producto.getId());
        updatedProducto.setNombre(UPDATED_NOMBRE);
        updatedProducto.setDescripcion(UPDATED_DESCRIPCION);
        updatedProducto.setImagen(UPDATED_IMAGEN);
        updatedProducto.setImagenContentType(UPDATED_IMAGEN_CONTENT_TYPE);
        updatedProducto.setPrecio(UPDATED_PRECIO);

        restProductoMockMvc.perform(put("/api/productos")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedProducto)))
                .andExpect(status().isOk());

        // Validate the Producto in the database
        List<Producto> productos = productoRepository.findAll();
        assertThat(productos).hasSize(databaseSizeBeforeUpdate);
        Producto testProducto = productos.get(productos.size() - 1);
        assertThat(testProducto.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testProducto.getDescripcion()).isEqualTo(UPDATED_DESCRIPCION);
        assertThat(testProducto.getImagen()).isEqualTo(UPDATED_IMAGEN);
        assertThat(testProducto.getImagenContentType()).isEqualTo(UPDATED_IMAGEN_CONTENT_TYPE);
        assertThat(testProducto.getPrecio()).isEqualTo(UPDATED_PRECIO);

        // Validate the Producto in ElasticSearch
        Producto productoEs = productoSearchRepository.findOne(testProducto.getId());
        assertThat(productoEs).isEqualToComparingFieldByField(testProducto);
    }

    @Test
    @Transactional
    public void deleteProducto() throws Exception {
        // Initialize the database
        productoRepository.saveAndFlush(producto);
        productoSearchRepository.save(producto);
        int databaseSizeBeforeDelete = productoRepository.findAll().size();

        // Get the producto
        restProductoMockMvc.perform(delete("/api/productos/{id}", producto.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean productoExistsInEs = productoSearchRepository.exists(producto.getId());
        assertThat(productoExistsInEs).isFalse();

        // Validate the database is empty
        List<Producto> productos = productoRepository.findAll();
        assertThat(productos).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchProducto() throws Exception {
        // Initialize the database
        productoRepository.saveAndFlush(producto);
        productoSearchRepository.save(producto);

        // Search the producto
        restProductoMockMvc.perform(get("/api/_search/productos?query=id:" + producto.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(producto.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())))
            .andExpect(jsonPath("$.[*].imagenContentType").value(hasItem(DEFAULT_IMAGEN_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].imagen").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGEN))))
            .andExpect(jsonPath("$.[*].precio").value(hasItem(DEFAULT_PRECIO)));
    }
}
