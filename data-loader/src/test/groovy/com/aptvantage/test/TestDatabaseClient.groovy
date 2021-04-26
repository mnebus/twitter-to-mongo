package com.aptvantage.test

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

import javax.inject.Singleton

import static com.mongodb.client.model.Filters.eq

@Singleton
class TestDatabaseClient {
    private MongoClient mongoClient

    TestDatabaseClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient
    }

    long numberOfRecords() {
        MongoDatabase database = mongoClient.getDatabase('twitter')
        MongoCollection<Document> collection = database.getCollection('tweets')
        return collection.countDocuments()
    }

    Document findRecord(long id) {
        MongoDatabase database = mongoClient.getDatabase('twitter')
        MongoCollection<Document> collection = database.getCollection('tweets')
        return collection.find(eq('id', id)).first()
    }
}
