package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.EcofruitApp;
import com.mycompany.myapp.domain.Subcategoria;
import com.mycompany.myapp.repository.SubcategoriaRepository;
import com.mycompany.myapp.repository.search.SubcategoriaSearchRepository;

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
 * Test class for the SubcategoriaResource REST controller.
 *
 * @see SubcategoriaResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EcofruitApp.class)
@WebAppConfiguration
@IntegrationTest
public class SubcategoriaResourceIntTest {

    private static final String DEFAULT_NOMBRE = "AAAAA";
    private static final String UPDATED_NOMBRE = "BBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBB";

    @Inject
    private SubcategoriaRepository subcategoriaRepository;

    @Inject
    private SubcategoriaSearchRepository subcategoriaSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restSubcategoriaMockMvc;

    private Subcategoria subcategoria;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        SubcategoriaResource subcategoriaResource = new SubcategoriaResource();
        ReflectionTestUtils.setField(subcategoriaResource, "subcategoriaSearchRepository", subcategoriaSearchRepository);
        ReflectionTestUtils.setField(subcategoriaResource, "subcategoriaRepository", subcategoriaRepository);
        this.restSubcategoriaMockMvc = MockMvcBuilders.standaloneSetup(subcategoriaResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        subcategoriaSearchRepository.deleteAll();
        subcategoria = new Subcategoria();
        subcategoria.setNombre(DEFAULT_NOMBRE);
        subcategoria.setDescripcion(DEFAULT_DESCRIPCION);
    }

    @Test
    @Transactional
    public void createSubcategoria() throws Exception {
        int databaseSizeBeforeCreate = subcategoriaRepository.findAll().size();

        // Create the Subcategoria

        restSubcategoriaMockMvc.perform(post("/api/subcategorias")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subcategoria)))
                .andExpect(status().isCreated());

        // Validate the Subcategoria in the database
        List<Subcategoria> subcategorias = subcategoriaRepository.findAll();
        assertThat(subcategorias).hasSize(databaseSizeBeforeCreate + 1);
        Subcategoria testSubcategoria = subcategorias.get(subcategorias.size() - 1);
        assertThat(testSubcategoria.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testSubcategoria.getDescripcion()).isEqualTo(DEFAULT_DESCRIPCION);

        // Validate the Subcategoria in ElasticSearch
        Subcategoria subcategoriaEs = subcategoriaSearchRepository.findOne(testSubcategoria.getId());
        assertThat(subcategoriaEs).isEqualToComparingFieldByField(testSubcategoria);
    }

    @Test
    @Transactional
    public void checkNombreIsRequired() throws Exception {
        int databaseSizeBeforeTest = subcategoriaRepository.findAll().size();
        // set the field null
        subcategoria.setNombre(null);

        // Create the Subcategoria, which fails.

        restSubcategoriaMockMvc.perform(post("/api/subcategorias")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subcategoria)))
                .andExpect(status().isBadRequest());

        List<Subcategoria> subcategorias = subcategoriaRepository.findAll();
        assertThat(subcategorias).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSubcategorias() throws Exception {
        // Initialize the database
        subcategoriaRepository.saveAndFlush(subcategoria);

        // Get all the subcategorias
        restSubcategoriaMockMvc.perform(get("/api/subcategorias?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(subcategoria.getId().intValue())))
                .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
                .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())));
    }

    @Test
    @Transactional
    public void getSubcategoria() throws Exception {
        // Initialize the database
        subcategoriaRepository.saveAndFlush(subcategoria);

        // Get the subcategoria
        restSubcategoriaMockMvc.perform(get("/api/subcategorias/{id}", subcategoria.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(subcategoria.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE.toString()))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSubcategoria() throws Exception {
        // Get the subcategoria
        restSubcategoriaMockMvc.perform(get("/api/subcategorias/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSubcategoria() throws Exception {
        // Initialize the database
        subcategoriaRepository.saveAndFlush(subcategoria);
        subcategoriaSearchRepository.save(subcategoria);
        int databaseSizeBeforeUpdate = subcategoriaRepository.findAll().size();

        // Update the subcategoria
        Subcategoria updatedSubcategoria = new Subcategoria();
        updatedSubcategoria.setId(subcategoria.getId());
        updatedSubcategoria.setNombre(UPDATED_NOMBRE);
        updatedSubcategoria.setDescripcion(UPDATED_DESCRIPCION);

        restSubcategoriaMockMvc.perform(put("/api/subcategorias")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedSubcategoria)))
                .andExpect(status().isOk());

        // Validate the Subcategoria in the database
        List<Subcategoria> subcategorias = subcategoriaRepository.findAll();
        assertThat(subcategorias).hasSize(databaseSizeBeforeUpdate);
        Subcategoria testSubcategoria = subcategorias.get(subcategorias.size() - 1);
        assertThat(testSubcategoria.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testSubcategoria.getDescripcion()).isEqualTo(UPDATED_DESCRIPCION);

        // Validate the Subcategoria in ElasticSearch
        Subcategoria subcategoriaEs = subcategoriaSearchRepository.findOne(testSubcategoria.getId());
        assertThat(subcategoriaEs).isEqualToComparingFieldByField(testSubcategoria);
    }

    @Test
    @Transactional
    public void deleteSubcategoria() throws Exception {
        // Initialize the database
        subcategoriaRepository.saveAndFlush(subcategoria);
        subcategoriaSearchRepository.save(subcategoria);
        int databaseSizeBeforeDelete = subcategoriaRepository.findAll().size();

        // Get the subcategoria
        restSubcategoriaMockMvc.perform(delete("/api/subcategorias/{id}", subcategoria.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean subcategoriaExistsInEs = subcategoriaSearchRepository.exists(subcategoria.getId());
        assertThat(subcategoriaExistsInEs).isFalse();

        // Validate the database is empty
        List<Subcategoria> subcategorias = subcategoriaRepository.findAll();
        assertThat(subcategorias).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchSubcategoria() throws Exception {
        // Initialize the database
        subcategoriaRepository.saveAndFlush(subcategoria);
        subcategoriaSearchRepository.save(subcategoria);

        // Search the subcategoria
        restSubcategoriaMockMvc.perform(get("/api/_search/subcategorias?query=id:" + subcategoria.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(subcategoria.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())));
    }
}
