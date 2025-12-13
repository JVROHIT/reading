package com.example.reading.integration;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test using Testcontainers with MongoDB.
 * Verifies MongoDB connection and basic CRUD operations.
 */
@DataMongoTest
@Testcontainers
class MongoDbIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {
        assertThat(mongoTemplate).isNotNull();
    }

    @Test
    void shouldInsertAndReadDocument() {
        // Create a simple document
        Document document = new Document();
        document.put("name", "test-document");
        document.put("value", 42);

        // Insert into a test collection
        mongoTemplate.insert(document, "test_collection");

        // Read it back
        Document retrieved = mongoTemplate.findById(document.get("_id"), Document.class, "test_collection");

        // Verify
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getString("name")).isEqualTo("test-document");
        assertThat(retrieved.getInteger("value")).isEqualTo(42);
    }

    @Test
    void shouldCountDocumentsInCollection() {
        String collectionName = "count_test_collection";
        
        // Insert multiple documents
        for (int i = 0; i < 3; i++) {
            Document doc = new Document();
            doc.put("index", i);
            mongoTemplate.insert(doc, collectionName);
        }

        // Count documents
        long count = mongoTemplate.getCollection(collectionName).countDocuments();

        // Verify
        assertThat(count).isEqualTo(3);
    }
}

