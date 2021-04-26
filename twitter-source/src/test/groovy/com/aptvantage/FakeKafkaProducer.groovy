package com.aptvantage

import io.micronaut.context.annotation.Replaces
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.Metric
import org.apache.kafka.common.MetricName
import org.apache.kafka.common.PartitionInfo
import org.apache.kafka.common.errors.ProducerFencedException

import javax.inject.Singleton
import java.time.Duration
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingDeque

@Singleton
@Replaces(Producer)
class FakeKafkaProducer<K, V> implements Producer {

    BlockingQueue<Object> messages = new LinkedBlockingDeque<>()

    boolean explodeNextMessage = false

    @Override
    void initTransactions() {}

    @Override
    void beginTransaction() throws ProducerFencedException {}

    @Override
    void commitTransaction() throws ProducerFencedException {}

    @Override
    void abortTransaction() throws ProducerFencedException {}

    @Override
    Future<RecordMetadata> send(ProducerRecord record) {
        if (explodeNextMessage) {
            explodeNextMessage = false
            throw new RuntimeException("Boom!!!!!!")
        }
        messages.add(record.value())
        return null
    }

    @Override
    Future<RecordMetadata> send(ProducerRecord record, Callback callback) { return null }

    @Override
    void flush() {}

    @Override
    List<PartitionInfo> partitionsFor(String topic) { return null }

    @Override
    Map<MetricName, ? extends Metric> metrics() { return null }

    @Override
    void close() {}

    @Override
    void close(Duration timeout) {}

    @Override
    void sendOffsetsToTransaction(Map offsets, ConsumerGroupMetadata groupMetadata) throws ProducerFencedException {
    }

    @Override
    void sendOffsetsToTransaction(Map offsets, String consumerGroupId) throws ProducerFencedException {}
}
