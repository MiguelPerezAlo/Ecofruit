package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Comentario;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Comentario entity.
 */
public interface ComentarioSearchRepository extends ElasticsearchRepository<Comentario, Long> {
}
