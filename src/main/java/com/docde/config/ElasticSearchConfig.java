package com.docde.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.springframework.data.elasticsearch.repository")
public class ElasticSearchConfig extends ElasticsearchConfiguration {
    @Value("${ES_URL}")
    private String esUrl;

    @Value(("${ES_USERNAME}"))
    private String esUsername;

    @Value(("${ELASTIC_PASSWORD}"))
    private String esPassword;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(esUrl)
                .usingSsl()
                .withBasicAuth(esUsername, esPassword)
                .withConnectTimeout(1000000000)
                .withSocketTimeout(1000000000)
                .build();
    }
}
