package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.EcofruitApp;
import com.mycompany.myapp.domain.Mensaje;
import com.mycompany.myapp.repository.MensajeRepository;
import com.mycompany.myapp.repository.search.MensajeSearchRepository;

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
 * Test class for the MensajeResource REST controller.
 *
 * @see MensajeResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EcofruitApp.class)
@WebAppConfiguration
@IntegrationTest
public class MensajeResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));

    private static final String DEFAULT_TITULO = "AAAAA";
    private static final String UPDATED_TITULO = "BBBBB";

    private static final String DEFAULT_CONTENIDO = "AAAAA";
    private static final String UPDATED_CONTENIDO = "BBBBB";

    private static final ZonedDateTime DEFAULT_FECHA = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_FECHA = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_FECHA_STR = dateTimeFormatter.format(DEFAULT_FECHA);

    @Inject
    private MensajeRepository mensajeRepository;

    @Inject
    private MensajeSearchRepository mensajeSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restMensajeMockMvc;

    private Mensaje mensaje;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MensajeResource mensajeResource = new MensajeResource();
        ReflectionTestUtils.setField(mensajeResource, "mensajeSearchRepository", mensajeSearchRepository);
        ReflectionTestUtils.setField(mensajeResource, "mensajeRepository", mensajeRepository);
        this.restMensajeMockMvc = MockMvcBuilders.standaloneSetup(mensajeResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        mensajeSearchRepository.deleteAll();
        mensaje = new Mensaje();
        mensaje.setTitulo(DEFAULT_TITULO);
        mensaje.setContenido(DEFAULT_CONTENIDO);
        mensaje.setFecha(DEFAULT_FECHA);
    }

    @Test
    @Transactional
    public void createMensaje() throws Exception {
        int databaseSizeBeforeCreate = mensajeRepository.findAll().size();

        // Create the Mensaje

        restMensajeMockMvc.perform(post("/api/mensajes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mensaje)))
                .andExpect(status().isCreated());

        // Validate the Mensaje in the database
        List<Mensaje> mensajes = mensajeRepository.findAll();
        assertThat(mensajes).hasSize(databaseSizeBeforeCreate + 1);
        Mensaje testMensaje = mensajes.get(mensajes.size() - 1);
        assertThat(testMensaje.getTitulo()).isEqualTo(DEFAULT_TITULO);
        assertThat(testMensaje.getContenido()).isEqualTo(DEFAULT_CONTENIDO);
        assertThat(testMensaje.getFecha()).isEqualTo(DEFAULT_FECHA);

        // Validate the Mensaje in ElasticSearch
        Mensaje mensajeEs = mensajeSearchRepository.findOne(testMensaje.getId());
        assertThat(mensajeEs).isEqualToComparingFieldByField(testMensaje);
    }

    @Test
    @Transactional
    public void checkTituloIsRequired() throws Exception {
        int databaseSizeBeforeTest = mensajeRepository.findAll().size();
        // set the field null
        mensaje.setTitulo(null);

        // Create the Mensaje, which fails.

        restMensajeMockMvc.perform(post("/api/mensajes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mensaje)))
                .andExpect(status().isBadRequest());

        List<Mensaje> mensajes = mensajeRepository.findAll();
        assertThat(mensajes).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkContenidoIsRequired() throws Exception {
        int databaseSizeBeforeTest = mensajeRepository.findAll().size();
        // set the field null
        mensaje.setContenido(null);

        // Create the Mensaje, which fails.

        restMensajeMockMvc.perform(post("/api/mensajes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mensaje)))
                .andExpect(status().isBadRequest());

        List<Mensaje> mensajes = mensajeRepository.findAll();
        assertThat(mensajes).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllMensajes() throws Exception {
        // Initialize the database
        mensajeRepository.saveAndFlush(mensaje);

        // Get all the mensajes
        restMensajeMockMvc.perform(get("/api/mensajes?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(mensaje.getId().intValue())))
                .andExpect(jsonPath("$.[*].titulo").value(hasItem(DEFAULT_TITULO.toString())))
                .andExpect(jsonPath("$.[*].contenido").value(hasItem(DEFAULT_CONTENIDO.toString())))
                .andExpect(jsonPath("$.[*].fecha").value(hasItem(DEFAULT_FECHA_STR)));
    }

    @Test
    @Transactional
    public void getMensaje() throws Exception {
        // Initialize the database
        mensajeRepository.saveAndFlush(mensaje);

        // Get the mensaje
        restMensajeMockMvc.perform(get("/api/mensajes/{id}", mensaje.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(mensaje.getId().intValue()))
            .andExpect(jsonPath("$.titulo").value(DEFAULT_TITULO.toString()))
            .andExpect(jsonPath("$.contenido").value(DEFAULT_CONTENIDO.toString()))
            .andExpect(jsonPath("$.fecha").value(DEFAULT_FECHA_STR));
    }

    @Test
    @Transactional
    public void getNonExistingMensaje() throws Exception {
        // Get the mensaje
        restMensajeMockMvc.perform(get("/api/mensajes/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMensaje() throws Exception {
        // Initialize the database
        mensajeRepository.saveAndFlush(mensaje);
        mensajeSearchRepository.save(mensaje);
        int databaseSizeBeforeUpdate = mensajeRepository.findAll().size();

        // Update the mensaje
        Mensaje updatedMensaje = new Mensaje();
        updatedMensaje.setId(mensaje.getId());
        updatedMensaje.setTitulo(UPDATED_TITULO);
        updatedMensaje.setContenido(UPDATED_CONTENIDO);
        updatedMensaje.setFecha(UPDATED_FECHA);

        restMensajeMockMvc.perform(put("/api/mensajes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedMensaje)))
                .andExpect(status().isOk());

        // Validate the Mensaje in the database
        List<Mensaje> mensajes = mensajeRepository.findAll();
        assertThat(mensajes).hasSize(databaseSizeBeforeUpdate);
        Mensaje testMensaje = mensajes.get(mensajes.size() - 1);
        assertThat(testMensaje.getTitulo()).isEqualTo(UPDATED_TITULO);
        assertThat(testMensaje.getContenido()).isEqualTo(UPDATED_CONTENIDO);
        assertThat(testMensaje.getFecha()).isEqualTo(UPDATED_FECHA);

        // Validate the Mensaje in ElasticSearch
        Mensaje mensajeEs = mensajeSearchRepository.findOne(testMensaje.getId());
        assertThat(mensajeEs).isEqualToComparingFieldByField(testMensaje);
    }

    @Test
    @Transactional
    public void deleteMensaje() throws Exception {
        // Initialize the database
        mensajeRepository.saveAndFlush(mensaje);
        mensajeSearchRepository.save(mensaje);
        int databaseSizeBeforeDelete = mensajeRepository.findAll().size();

        // Get the mensaje
        restMensajeMockMvc.perform(delete("/api/mensajes/{id}", mensaje.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean mensajeExistsInEs = mensajeSearchRepository.exists(mensaje.getId());
        assertThat(mensajeExistsInEs).isFalse();

        // Validate the database is empty
        List<Mensaje> mensajes = mensajeRepository.findAll();
        assertThat(mensajes).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchMensaje() throws Exception {
        // Initialize the database
        mensajeRepository.saveAndFlush(mensaje);
        mensajeSearchRepository.save(mensaje);

        // Search the mensaje
        restMensajeMockMvc.perform(get("/api/_search/mensajes?query=id:" + mensaje.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(mensaje.getId().intValue())))
            .andExpect(jsonPath("$.[*].titulo").value(hasItem(DEFAULT_TITULO.toString())))
            .andExpect(jsonPath("$.[*].contenido").value(hasItem(DEFAULT_CONTENIDO.toString())))
            .andExpect(jsonPath("$.[*].fecha").value(hasItem(DEFAULT_FECHA_STR)));
    }
}
