package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.EcofruitApp;
import com.mycompany.myapp.domain.Comentario;
import com.mycompany.myapp.repository.ComentarioRepository;
import com.mycompany.myapp.repository.search.ComentarioSearchRepository;

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
 * Test class for the ComentarioResource REST controller.
 *
 * @see ComentarioResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EcofruitApp.class)
@WebAppConfiguration
@IntegrationTest
public class ComentarioResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));


    private static final String DEFAULT_TEXTO = "";
    private static final String UPDATED_TEXTO = "";

    private static final ZonedDateTime DEFAULT_FECHA = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_FECHA = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_FECHA_STR = dateTimeFormatter.format(DEFAULT_FECHA);

    @Inject
    private ComentarioRepository comentarioRepository;

    @Inject
    private ComentarioSearchRepository comentarioSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restComentarioMockMvc;

    private Comentario comentario;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ComentarioResource comentarioResource = new ComentarioResource();
        ReflectionTestUtils.setField(comentarioResource, "comentarioSearchRepository", comentarioSearchRepository);
        ReflectionTestUtils.setField(comentarioResource, "comentarioRepository", comentarioRepository);
        this.restComentarioMockMvc = MockMvcBuilders.standaloneSetup(comentarioResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        comentarioSearchRepository.deleteAll();
        comentario = new Comentario();
        comentario.setTexto(DEFAULT_TEXTO);
        comentario.setFecha(DEFAULT_FECHA);
    }

    @Test
    @Transactional
    public void createComentario() throws Exception {
        int databaseSizeBeforeCreate = comentarioRepository.findAll().size();

        // Create the Comentario

        restComentarioMockMvc.perform(post("/api/comentarios")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comentario)))
                .andExpect(status().isCreated());

        // Validate the Comentario in the database
        List<Comentario> comentarios = comentarioRepository.findAll();
        assertThat(comentarios).hasSize(databaseSizeBeforeCreate + 1);
        Comentario testComentario = comentarios.get(comentarios.size() - 1);
        assertThat(testComentario.getTexto()).isEqualTo(DEFAULT_TEXTO);
        assertThat(testComentario.getFecha()).isEqualTo(DEFAULT_FECHA);

        // Validate the Comentario in ElasticSearch
        Comentario comentarioEs = comentarioSearchRepository.findOne(testComentario.getId());
        assertThat(comentarioEs).isEqualToComparingFieldByField(testComentario);
    }

    @Test
    @Transactional
    public void checkTextoIsRequired() throws Exception {
        int databaseSizeBeforeTest = comentarioRepository.findAll().size();
        // set the field null
        comentario.setTexto(null);

        // Create the Comentario, which fails.

        restComentarioMockMvc.perform(post("/api/comentarios")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comentario)))
                .andExpect(status().isBadRequest());

        List<Comentario> comentarios = comentarioRepository.findAll();
        assertThat(comentarios).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllComentarios() throws Exception {
        // Initialize the database
        comentarioRepository.saveAndFlush(comentario);

        // Get all the comentarios
        restComentarioMockMvc.perform(get("/api/comentarios?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(comentario.getId().intValue())))
                .andExpect(jsonPath("$.[*].texto").value(hasItem(DEFAULT_TEXTO.toString())))
                .andExpect(jsonPath("$.[*].fecha").value(hasItem(DEFAULT_FECHA_STR)));
    }

    @Test
    @Transactional
    public void getComentario() throws Exception {
        // Initialize the database
        comentarioRepository.saveAndFlush(comentario);

        // Get the comentario
        restComentarioMockMvc.perform(get("/api/comentarios/{id}", comentario.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(comentario.getId().intValue()))
            .andExpect(jsonPath("$.texto").value(DEFAULT_TEXTO.toString()))
            .andExpect(jsonPath("$.fecha").value(DEFAULT_FECHA_STR));
    }

    @Test
    @Transactional
    public void getNonExistingComentario() throws Exception {
        // Get the comentario
        restComentarioMockMvc.perform(get("/api/comentarios/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateComentario() throws Exception {
        // Initialize the database
        comentarioRepository.saveAndFlush(comentario);
        comentarioSearchRepository.save(comentario);
        int databaseSizeBeforeUpdate = comentarioRepository.findAll().size();

        // Update the comentario
        Comentario updatedComentario = new Comentario();
        updatedComentario.setId(comentario.getId());
        updatedComentario.setTexto(UPDATED_TEXTO);
        updatedComentario.setFecha(UPDATED_FECHA);

        restComentarioMockMvc.perform(put("/api/comentarios")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedComentario)))
                .andExpect(status().isOk());

        // Validate the Comentario in the database
        List<Comentario> comentarios = comentarioRepository.findAll();
        assertThat(comentarios).hasSize(databaseSizeBeforeUpdate);
        Comentario testComentario = comentarios.get(comentarios.size() - 1);
        assertThat(testComentario.getTexto()).isEqualTo(UPDATED_TEXTO);
        assertThat(testComentario.getFecha()).isEqualTo(UPDATED_FECHA);

        // Validate the Comentario in ElasticSearch
        Comentario comentarioEs = comentarioSearchRepository.findOne(testComentario.getId());
        assertThat(comentarioEs).isEqualToComparingFieldByField(testComentario);
    }

    @Test
    @Transactional
    public void deleteComentario() throws Exception {
        // Initialize the database
        comentarioRepository.saveAndFlush(comentario);
        comentarioSearchRepository.save(comentario);
        int databaseSizeBeforeDelete = comentarioRepository.findAll().size();

        // Get the comentario
        restComentarioMockMvc.perform(delete("/api/comentarios/{id}", comentario.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean comentarioExistsInEs = comentarioSearchRepository.exists(comentario.getId());
        assertThat(comentarioExistsInEs).isFalse();

        // Validate the database is empty
        List<Comentario> comentarios = comentarioRepository.findAll();
        assertThat(comentarios).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchComentario() throws Exception {
        // Initialize the database
        comentarioRepository.saveAndFlush(comentario);
        comentarioSearchRepository.save(comentario);

        // Search the comentario
        restComentarioMockMvc.perform(get("/api/_search/comentarios?query=id:" + comentario.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(comentario.getId().intValue())))
            .andExpect(jsonPath("$.[*].texto").value(hasItem(DEFAULT_TEXTO.toString())))
            .andExpect(jsonPath("$.[*].fecha").value(hasItem(DEFAULT_FECHA_STR)));
    }
}
