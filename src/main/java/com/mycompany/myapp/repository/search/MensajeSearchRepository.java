package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Mensaje;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Mensaje entity.
 */
public interface MensajeSearchRepository extends ElasticsearchRepository<Mensaje, Long> {
}
