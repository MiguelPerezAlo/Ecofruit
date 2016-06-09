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
 * A Comentario.
 */
@Entity
@Table(name = "comentario")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "comentario")
public class Comentario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Lob
    @Column(name = "texto", nullable = false)
    private String texto;

    @Column(name = "fecha")
    private ZonedDateTime fecha;

    @ManyToOne
    private Receta receta;

    @ManyToOne
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public ZonedDateTime getFecha() {
        return fecha;
    }

    public void setFecha(ZonedDateTime fecha) {
        this.fecha = fecha;
    }

    public Receta getReceta() {
        return receta;
    }

    public void setReceta(Receta receta) {
        this.receta = receta;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Comentario comentario = (Comentario) o;
        if(comentario.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, comentario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Comentario{" +
            "id=" + id +
            ", texto='" + texto + "'" +
            ", fecha='" + fecha + "'" +
            '}';
    }
}
