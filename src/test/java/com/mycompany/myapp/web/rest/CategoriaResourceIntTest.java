package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.EcofruitApp;
import com.mycompany.myapp.domain.Categoria;
import com.mycompany.myapp.repository.CategoriaRepository;
import com.mycompany.myapp.repository.search.CategoriaSearchRepository;

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
 * Test class for the CategoriaResource REST controller.
 *
 * @see CategoriaResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EcofruitApp.class)
@WebAppConfiguration
@IntegrationTest
public class CategoriaResourceIntTest {

    private static final String DEFAULT_NOMBRE = "AAAAA";
    private static final String UPDATED_NOMBRE = "BBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBB";

    @Inject
    private CategoriaRepository categoriaRepository;

    @Inject
    private CategoriaSearchRepository categoriaSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restCategoriaMockMvc;

    private Categoria categoria;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CategoriaResource categoriaResource = new CategoriaResource();
        ReflectionTestUtils.setField(categoriaResource, "categoriaSearchRepository", categoriaSearchRepository);
        ReflectionTestUtils.setField(categoriaResource, "categoriaRepository", categoriaRepository);
        this.restCategoriaMockMvc = MockMvcBuilders.standaloneSetup(categoriaResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        categoriaSearchRepository.deleteAll();
        categoria = new Categoria();
        categoria.setNombre(DEFAULT_NOMBRE);
        categoria.setDescripcion(DEFAULT_DESCRIPCION);
    }

    @Test
    @Transactional
    public void createCategoria() throws Exception {
        int databaseSizeBeforeCreate = categoriaRepository.findAll().size();

        // Create the Categoria

        restCategoriaMockMvc.perform(post("/api/categorias")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(categoria)))
                .andExpect(status().isCreated());

        // Validate the Categoria in the database
        List<Categoria> categorias = categoriaRepository.findAll();
        assertThat(categorias).hasSize(databaseSizeBeforeCreate + 1);
        Categoria testCategoria = categorias.get(categorias.size() - 1);
        assertThat(testCategoria.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testCategoria.getDescripcion()).isEqualTo(DEFAULT_DESCRIPCION);

        // Validate the Categoria in ElasticSearch
        Categoria categoriaEs = categoriaSearchRepository.findOne(testCategoria.getId());
        assertThat(categoriaEs).isEqualToComparingFieldByField(testCategoria);
    }

    @Test
    @Transactional
    public void checkNombreIsRequired() throws Exception {
        int databaseSizeBeforeTest = categoriaRepository.findAll().size();
        // set the field null
        categoria.setNombre(null);

        // Create the Categoria, which fails.

        restCategoriaMockMvc.perform(post("/api/categorias")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(categoria)))
                .andExpect(status().isBadRequest());

        List<Categoria> categorias = categoriaRepository.findAll();
        assertThat(categorias).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCategorias() throws Exception {
        // Initialize the database
        categoriaRepository.saveAndFlush(categoria);

        // Get all the categorias
        restCategoriaMockMvc.perform(get("/api/categorias?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(categoria.getId().intValue())))
                .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
                .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())));
    }

    @Test
    @Transactional
    public void getCategoria() throws Exception {
        // Initialize the database
        categoriaRepository.saveAndFlush(categoria);

        // Get the categoria
        restCategoriaMockMvc.perform(get("/api/categorias/{id}", categoria.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(categoria.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE.toString()))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCategoria() throws Exception {
        // Get the categoria
        restCategoriaMockMvc.perform(get("/api/categorias/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCategoria() throws Exception {
        // Initialize the database
        categoriaRepository.saveAndFlush(categoria);
        categoriaSearchRepository.save(categoria);
        int databaseSizeBeforeUpdate = categoriaRepository.findAll().size();

        // Update the categoria
        Categoria updatedCategoria = new Categoria();
        updatedCategoria.setId(categoria.getId());
        updatedCategoria.setNombre(UPDATED_NOMBRE);
        updatedCategoria.setDescripcion(UPDATED_DESCRIPCION);

        restCategoriaMockMvc.perform(put("/api/categorias")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedCategoria)))
                .andExpect(status().isOk());

        // Validate the Categoria in the database
        List<Categoria> categorias = categoriaRepository.findAll();
        assertThat(categorias).hasSize(databaseSizeBeforeUpdate);
        Categoria testCategoria = categorias.get(categorias.size() - 1);
        assertThat(testCategoria.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testCategoria.getDescripcion()).isEqualTo(UPDATED_DESCRIPCION);

        // Validate the Categoria in ElasticSearch
        Categoria categoriaEs = categoriaSearchRepository.findOne(testCategoria.getId());
        assertThat(categoriaEs).isEqualToComparingFieldByField(testCategoria);
    }

    @Test
    @Transactional
    public void deleteCategoria() throws Exception {
        // Initialize the database
        categoriaRepository.saveAndFlush(categoria);
        categoriaSearchRepository.save(categoria);
        int databaseSizeBeforeDelete = categoriaRepository.findAll().size();

        // Get the categoria
        restCategoriaMockMvc.perform(delete("/api/categorias/{id}", categoria.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean categoriaExistsInEs = categoriaSearchRepository.exists(categoria.getId());
        assertThat(categoriaExistsInEs).isFalse();

        // Validate the database is empty
        List<Categoria> categorias = categoriaRepository.findAll();
        assertThat(categorias).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchCategoria() throws Exception {
        // Initialize the database
        categoriaRepository.saveAndFlush(categoria);
        categoriaSearchRepository.save(categoria);

        // Search the categoria
        restCategoriaMockMvc.perform(get("/api/_search/categorias?query=id:" + categoria.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(categoria.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())));
    }
}
