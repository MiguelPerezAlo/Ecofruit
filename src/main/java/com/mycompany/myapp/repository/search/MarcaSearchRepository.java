package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Marca;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Marca entity.
 */
public interface MarcaSearchRepository extends ElasticsearchRepository<Marca, Long> {
}
