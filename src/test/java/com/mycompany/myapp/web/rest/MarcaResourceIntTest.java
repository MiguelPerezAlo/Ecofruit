package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.EcofruitApp;
import com.mycompany.myapp.domain.Marca;
import com.mycompany.myapp.repository.MarcaRepository;
import com.mycompany.myapp.repository.search.MarcaSearchRepository;

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
 * Test class for the MarcaResource REST controller.
 *
 * @see MarcaResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EcofruitApp.class)
@WebAppConfiguration
@IntegrationTest
public class MarcaResourceIntTest {

    private static final String DEFAULT_NOMBRE = "AAAAA";
    private static final String UPDATED_NOMBRE = "BBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBB";

    @Inject
    private MarcaRepository marcaRepository;

    @Inject
    private MarcaSearchRepository marcaSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restMarcaMockMvc;

    private Marca marca;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MarcaResource marcaResource = new MarcaResource();
        ReflectionTestUtils.setField(marcaResource, "marcaSearchRepository", marcaSearchRepository);
        ReflectionTestUtils.setField(marcaResource, "marcaRepository", marcaRepository);
        this.restMarcaMockMvc = MockMvcBuilders.standaloneSetup(marcaResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        marcaSearchRepository.deleteAll();
        marca = new Marca();
        marca.setNombre(DEFAULT_NOMBRE);
        marca.setDescripcion(DEFAULT_DESCRIPCION);
    }

    @Test
    @Transactional
    public void createMarca() throws Exception {
        int databaseSizeBeforeCreate = marcaRepository.findAll().size();

        // Create the Marca

        restMarcaMockMvc.perform(post("/api/marcas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marca)))
                .andExpect(status().isCreated());

        // Validate the Marca in the database
        List<Marca> marcas = marcaRepository.findAll();
        assertThat(marcas).hasSize(databaseSizeBeforeCreate + 1);
        Marca testMarca = marcas.get(marcas.size() - 1);
        assertThat(testMarca.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testMarca.getDescripcion()).isEqualTo(DEFAULT_DESCRIPCION);

        // Validate the Marca in ElasticSearch
        Marca marcaEs = marcaSearchRepository.findOne(testMarca.getId());
        assertThat(marcaEs).isEqualToComparingFieldByField(testMarca);
    }

    @Test
    @Transactional
    public void checkNombreIsRequired() throws Exception {
        int databaseSizeBeforeTest = marcaRepository.findAll().size();
        // set the field null
        marca.setNombre(null);

        // Create the Marca, which fails.

        restMarcaMockMvc.perform(post("/api/marcas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(marca)))
                .andExpect(status().isBadRequest());

        List<Marca> marcas = marcaRepository.findAll();
        assertThat(marcas).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllMarcas() throws Exception {
        // Initialize the database
        marcaRepository.saveAndFlush(marca);

        // Get all the marcas
        restMarcaMockMvc.perform(get("/api/marcas?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(marca.getId().intValue())))
                .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
                .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())));
    }

    @Test
    @Transactional
    public void getMarca() throws Exception {
        // Initialize the database
        marcaRepository.saveAndFlush(marca);

        // Get the marca
        restMarcaMockMvc.perform(get("/api/marcas/{id}", marca.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(marca.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE.toString()))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingMarca() throws Exception {
        // Get the marca
        restMarcaMockMvc.perform(get("/api/marcas/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMarca() throws Exception {
        // Initialize the database
        marcaRepository.saveAndFlush(marca);
        marcaSearchRepository.save(marca);
        int databaseSizeBeforeUpdate = marcaRepository.findAll().size();

        // Update the marca
        Marca updatedMarca = new Marca();
        updatedMarca.setId(marca.getId());
        updatedMarca.setNombre(UPDATED_NOMBRE);
        updatedMarca.setDescripcion(UPDATED_DESCRIPCION);

        restMarcaMockMvc.perform(put("/api/marcas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedMarca)))
                .andExpect(status().isOk());

        // Validate the Marca in the database
        List<Marca> marcas = marcaRepository.findAll();
        assertThat(marcas).hasSize(databaseSizeBeforeUpdate);
        Marca testMarca = marcas.get(marcas.size() - 1);
        assertThat(testMarca.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testMarca.getDescripcion()).isEqualTo(UPDATED_DESCRIPCION);

        // Validate the Marca in ElasticSearch
        Marca marcaEs = marcaSearchRepository.findOne(testMarca.getId());
        assertThat(marcaEs).isEqualToComparingFieldByField(testMarca);
    }

    @Test
    @Transactional
    public void deleteMarca() throws Exception {
        // Initialize the database
        marcaRepository.saveAndFlush(marca);
        marcaSearchRepository.save(marca);
        int databaseSizeBeforeDelete = marcaRepository.findAll().size();

        // Get the marca
        restMarcaMockMvc.perform(delete("/api/marcas/{id}", marca.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean marcaExistsInEs = marcaSearchRepository.exists(marca.getId());
        assertThat(marcaExistsInEs).isFalse();

        // Validate the database is empty
        List<Marca> marcas = marcaRepository.findAll();
        assertThat(marcas).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchMarca() throws Exception {
        // Initialize the database
        marcaRepository.saveAndFlush(marca);
        marcaSearchRepository.save(marca);

        // Search the marca
        restMarcaMockMvc.perform(get("/api/_search/marcas?query=id:" + marca.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(marca.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())));
    }
}
