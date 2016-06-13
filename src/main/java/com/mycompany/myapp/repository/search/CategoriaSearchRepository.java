package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Categoria;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Categoria entity.
 */
public interface CategoriaSearchRepository extends ElasticsearchRepository<Categoria, Long> {
}
