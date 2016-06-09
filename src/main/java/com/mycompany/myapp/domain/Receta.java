package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Receta.
 */
@Entity
@Table(name = "receta")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "receta")
public class Receta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Lob
    @Column(name = "descripcion")
    private String descripcion;

    @Lob
    @Column(name = "imagen")
    private byte[] imagen;

    @Column(name = "imagen_content_type")
    private String imagenContentType;

    @Column(name = "fecha")
    private ZonedDateTime fecha;

    @OneToMany(mappedBy = "receta")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Comentario> comentarios = new HashSet<>();

    @ManyToOne
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public String getImagenContentType() {
        return imagenContentType;
    }

    public void setImagenContentType(String imagenContentType) {
        this.imagenContentType = imagenContentType;
    }

    public ZonedDateTime getFecha() {
        return fecha;
    }

    public void setFecha(ZonedDateTime fecha) {
        this.fecha = fecha;
    }

    public Set<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(Set<Comentario> comentarios) {
        this.comentarios = comentarios;
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
        Receta receta = (Receta) o;
        if(receta.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, receta.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Receta{" +
            "id=" + id +
            ", nombre='" + nombre + "'" +
            ", descripcion='" + descripcion + "'" +
            ", imagen='" + imagen + "'" +
            ", imagenContentType='" + imagenContentType + "'" +
            ", fecha='" + fecha + "'" +
            '}';
    }
}
