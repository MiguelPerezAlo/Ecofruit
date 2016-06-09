package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Receta;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Receta entity.
 */
public interface RecetaSearchRepository extends ElasticsearchRepository<Receta, Long> {
}
