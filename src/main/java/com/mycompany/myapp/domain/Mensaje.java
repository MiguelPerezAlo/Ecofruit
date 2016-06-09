package com.mycompany.myapp.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A Mensaje.
 */
@Entity
@Table(name = "mensaje")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "mensaje")
public class Mensaje implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "titulo", nullable = false)
    private String titulo;

    @NotNull
    @Lob
    @Column(name = "contenido", nullable = false)
    private String contenido;

    @Column(name = "fecha")
    private ZonedDateTime fecha;

    @ManyToOne
    private User emisor;

    @ManyToOne
    private User receptor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public ZonedDateTime getFecha() {
        return fecha;
    }

    public void setFecha(ZonedDateTime fecha) {
        this.fecha = fecha;
    }

    public User getEmisor() {
        return emisor;
    }

    public void setEmisor(User user) {
        this.emisor = user;
    }

    public User getReceptor() {
        return receptor;
    }

    public void setReceptor(User user) {
        this.receptor = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Mensaje mensaje = (Mensaje) o;
        if(mensaje.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, mensaje.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Mensaje{" +
            "id=" + id +
            ", titulo='" + titulo + "'" +
            ", contenido='" + contenido + "'" +
            ", fecha='" + fecha + "'" +
            '}';
    }
}
