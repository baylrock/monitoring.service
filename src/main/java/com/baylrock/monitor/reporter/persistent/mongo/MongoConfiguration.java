package com.baylrock.monitor.reporter.persistent.mongo;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import cz.jirutka.spring.embedmongo.EmbeddedMongoFactoryBean;

@EnableMongoRepositories
@Configuration
public class MongoConfiguration {

    /**
     * Embed mongo db initialization
     */
    @Bean
    public MongoTemplate mongoTemplate() throws IOException {
        EmbeddedMongoFactoryBean mongo = new EmbeddedMongoFactoryBean();
        mongo.setBindIp("127.0.0.1");
        return new MongoTemplate(mongo.getObject(), "embeded_db");
    }
}
