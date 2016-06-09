package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.EcofruitApp;
import com.mycompany.myapp.domain.Receta;
import com.mycompany.myapp.repository.RecetaRepository;
import com.mycompany.myapp.repository.search.RecetaSearchRepository;

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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the RecetaResource REST controller.
 *
 * @see RecetaResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EcofruitApp.class)
@WebAppConfiguration
@IntegrationTest
public class RecetaResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));

    private static final String DEFAULT_NOMBRE = "AAAAA";
    private static final String UPDATED_NOMBRE = "BBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBB";

    private static final byte[] DEFAULT_IMAGEN = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGEN = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_IMAGEN_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGEN_CONTENT_TYPE = "image/png";

    private static final ZonedDateTime DEFAULT_FECHA = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_FECHA = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_FECHA_STR = dateTimeFormatter.format(DEFAULT_FECHA);

    @Inject
    private RecetaRepository recetaRepository;

    @Inject
    private RecetaSearchRepository recetaSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restRecetaMockMvc;

    private Receta receta;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        RecetaResource recetaResource = new RecetaResource();
        ReflectionTestUtils.setField(recetaResource, "recetaSearchRepository", recetaSearchRepository);
        ReflectionTestUtils.setField(recetaResource, "recetaRepository", recetaRepository);
        this.restRecetaMockMvc = MockMvcBuilders.standaloneSetup(recetaResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        recetaSearchRepository.deleteAll();
        receta = new Receta();
        receta.setNombre(DEFAULT_NOMBRE);
        receta.setDescripcion(DEFAULT_DESCRIPCION);
        receta.setImagen(DEFAULT_IMAGEN);
        receta.setImagenContentType(DEFAULT_IMAGEN_CONTENT_TYPE);
        receta.setFecha(DEFAULT_FECHA);
    }

    @Test
    @Transactional
    public void createReceta() throws Exception {
        int databaseSizeBeforeCreate = recetaRepository.findAll().size();

        // Create the Receta

        restRecetaMockMvc.perform(post("/api/recetas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(receta)))
                .andExpect(status().isCreated());

        // Validate the Receta in the database
        List<Receta> recetas = recetaRepository.findAll();
        assertThat(recetas).hasSize(databaseSizeBeforeCreate + 1);
        Receta testReceta = recetas.get(recetas.size() - 1);
        assertThat(testReceta.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testReceta.getDescripcion()).isEqualTo(DEFAULT_DESCRIPCION);
        assertThat(testReceta.getImagen()).isEqualTo(DEFAULT_IMAGEN);
        assertThat(testReceta.getImagenContentType()).isEqualTo(DEFAULT_IMAGEN_CONTENT_TYPE);
        assertThat(testReceta.getFecha()).isEqualTo(DEFAULT_FECHA);

        // Validate the Receta in ElasticSearch
        Receta recetaEs = recetaSearchRepository.findOne(testReceta.getId());
        assertThat(recetaEs).isEqualToComparingFieldByField(testReceta);
    }

    @Test
    @Transactional
    public void checkNombreIsRequired() throws Exception {
        int databaseSizeBeforeTest = recetaRepository.findAll().size();
        // set the field null
        receta.setNombre(null);

        // Create the Receta, which fails.

        restRecetaMockMvc.perform(post("/api/recetas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(receta)))
                .andExpect(status().isBadRequest());

        List<Receta> recetas = recetaRepository.findAll();
        assertThat(recetas).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRecetas() throws Exception {
        // Initialize the database
        recetaRepository.saveAndFlush(receta);

        // Get all the recetas
        restRecetaMockMvc.perform(get("/api/recetas?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(receta.getId().intValue())))
                .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
                .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())))
                .andExpect(jsonPath("$.[*].imagenContentType").value(hasItem(DEFAULT_IMAGEN_CONTENT_TYPE)))
                .andExpect(jsonPath("$.[*].imagen").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGEN))))
                .andExpect(jsonPath("$.[*].fecha").value(hasItem(DEFAULT_FECHA_STR)));
    }

    @Test
    @Transactional
    public void getReceta() throws Exception {
        // Initialize the database
        recetaRepository.saveAndFlush(receta);

        // Get the receta
        restRecetaMockMvc.perform(get("/api/recetas/{id}", receta.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(receta.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE.toString()))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION.toString()))
            .andExpect(jsonPath("$.imagenContentType").value(DEFAULT_IMAGEN_CONTENT_TYPE))
            .andExpect(jsonPath("$.imagen").value(Base64Utils.encodeToString(DEFAULT_IMAGEN)))
            .andExpect(jsonPath("$.fecha").value(DEFAULT_FECHA_STR));
    }

    @Test
    @Transactional
    public void getNonExistingReceta() throws Exception {
        // Get the receta
        restRecetaMockMvc.perform(get("/api/recetas/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateReceta() throws Exception {
        // Initialize the database
        recetaRepository.saveAndFlush(receta);
        recetaSearchRepository.save(receta);
        int databaseSizeBeforeUpdate = recetaRepository.findAll().size();

        // Update the receta
        Receta updatedReceta = new Receta();
        updatedReceta.setId(receta.getId());
        updatedReceta.setNombre(UPDATED_NOMBRE);
        updatedReceta.setDescripcion(UPDATED_DESCRIPCION);
        updatedReceta.setImagen(UPDATED_IMAGEN);
        updatedReceta.setImagenContentType(UPDATED_IMAGEN_CONTENT_TYPE);
        updatedReceta.setFecha(UPDATED_FECHA);

        restRecetaMockMvc.perform(put("/api/recetas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedReceta)))
                .andExpect(status().isOk());

        // Validate the Receta in the database
        List<Receta> recetas = recetaRepository.findAll();
        assertThat(recetas).hasSize(databaseSizeBeforeUpdate);
        Receta testReceta = recetas.get(recetas.size() - 1);
        assertThat(testReceta.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testReceta.getDescripcion()).isEqualTo(UPDATED_DESCRIPCION);
        assertThat(testReceta.getImagen()).isEqualTo(UPDATED_IMAGEN);
        assertThat(testReceta.getImagenContentType()).isEqualTo(UPDATED_IMAGEN_CONTENT_TYPE);
        assertThat(testReceta.getFecha()).isEqualTo(UPDATED_FECHA);

        // Validate the Receta in ElasticSearch
        Receta recetaEs = recetaSearchRepository.findOne(testReceta.getId());
        assertThat(recetaEs).isEqualToComparingFieldByField(testReceta);
    }

    @Test
    @Transactional
    public void deleteReceta() throws Exception {
        // Initialize the database
        recetaRepository.saveAndFlush(receta);
        recetaSearchRepository.save(receta);
        int databaseSizeBeforeDelete = recetaRepository.findAll().size();

        // Get the receta
        restRecetaMockMvc.perform(delete("/api/recetas/{id}", receta.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean recetaExistsInEs = recetaSearchRepository.exists(receta.getId());
        assertThat(recetaExistsInEs).isFalse();

        // Validate the database is empty
        List<Receta> recetas = recetaRepository.findAll();
        assertThat(recetas).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchReceta() throws Exception {
        // Initialize the database
        recetaRepository.saveAndFlush(receta);
        recetaSearchRepository.save(receta);

        // Search the receta
        restRecetaMockMvc.perform(get("/api/_search/recetas?query=id:" + receta.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(receta.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE.toString())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION.toString())))
            .andExpect(jsonPath("$.[*].imagenContentType").value(hasItem(DEFAULT_IMAGEN_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].imagen").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGEN))))
            .andExpect(jsonPath("$.[*].fecha").value(hasItem(DEFAULT_FECHA_STR)));
    }
}
