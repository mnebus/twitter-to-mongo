package com.aptvantage.data.loader;

import com.google.common.flogger.FluentLogger;
import com.google.gson.JsonParser;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import org.bson.Document;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class AtlasLoader {

    //TODO - fail starup on bad mongo connection

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private Counter loadedRecordsCounter;
    private Counter failedRecordsCounter;
    private MongoClient mongoClient;

    public AtlasLoader(MongoClient mongoClient, MeterRegistry meterRegistry) {
        this.mongoClient = mongoClient;
        this.loadedRecordsCounter = meterRegistry.counter("atlas.records.loaded");
        this.failedRecordsCounter = meterRegistry.counter("atlas.records.failed");
    }

    void loadIntoDatabase(List<String> tweets) {
        MongoDatabase database = mongoClient.getDatabase("twitter");
        MongoCollection<Document> tweetCollection = database.getCollection("tweets");

        //TODO handle "conversion exception" (if id can't be extracted)
        List<InsertOneModel<Document>> insertions = tweets.stream()
                .map(tweet -> Document.parse(tweet).append("_id", extractIdFromTweet(tweet)))
                .map(InsertOneModel::new)
                .collect(Collectors.toList());
        try {
            tweetCollection.bulkWrite(insertions);
            loadedRecordsCounter.increment(insertions.size());
            logger.atInfo().atMostEvery(10, TimeUnit.SECONDS).log("loaded %s records in atlas",insertions.size());
        } catch (MongoBulkWriteException e) {
            loadedRecordsCounter.increment(e.getWriteResult().getInsertedCount());
            failedRecordsCounter.increment(e.getWriteErrors().size());
            logger.atSevere().log("Exception loading data", e);
            e.getWriteErrors().forEach(bulkWriteError -> {
                //TODO - send these to error queue?
                //TODO - at metric for alerting
                logger.atSevere().log("insertion failed");
                logger.atSevere().log("reason: %s", bulkWriteError.getCategory());
                logger.atSevere().log("record: %s", bulkWriteError.getDetails());
            });
        }

    }

    Long extractIdFromTweet(String tweetJson) {
        return JsonParser.parseString(tweetJson)
                .getAsJsonObject()
                .get("id")
                .getAsLong();
    }
}
