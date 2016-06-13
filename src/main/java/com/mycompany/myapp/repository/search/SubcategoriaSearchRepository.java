package com.mycompany.myapp.repository.search;

import com.mycompany.myapp.domain.Subcategoria;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Subcategoria entity.
 */
public interface SubcategoriaSearchRepository extends ElasticsearchRepository<Subcategoria, Long> {
}
