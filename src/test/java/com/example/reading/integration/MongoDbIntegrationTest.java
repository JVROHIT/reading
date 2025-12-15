package com.example.reading.integration;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test against a locally running MongoDB (no Testcontainers).
 * Start Mongo before running tests: docker run -d --name reading-mongo -p 27017:27017 mongo:7
 */
@SpringBootTest
@ActiveProfiles("local-mongo")
class MongoDbIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {
        assertThat(mongoTemplate).isNotNull();
    }

    @Test
    void shouldInsertAndReadDocument() {
        Document document = new Document();
        document.put("name", "test-document");
        document.put("value", 42);

        mongoTemplate.insert(document, "test_collection");

        Document retrieved = mongoTemplate.findById(document.get("_id"), Document.class, "test_collection");

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getString("name")).isEqualTo("test-document");
        assertThat(retrieved.getInteger("value")).isEqualTo(42);
    }

    @Test
    void shouldCountDocumentsInCollection() {
        String collectionName = "count_test_collection";

        // clean slate so the test is repeatable
        mongoTemplate.dropCollection(collectionName);

        for (int i = 0; i < 3; i++) {
            Document doc = new Document();
            doc.put("index", i);
            mongoTemplate.insert(doc, collectionName);
        }

        long count = mongoTemplate.getCollection(collectionName).countDocuments();
        assertThat(count).isEqualTo(3);
    }
}
