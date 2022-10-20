/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service;

import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.Serializable;

/** 
 * Interface for managing Kafka 
 *
 */
public interface IKafkaSRV extends Serializable {

    /**
	 * Send message over kafka topic
	 * 
	 * @param topic  The topic to create 
	 * @param key  The message key 
	 * @param value  The message value 
	 * @param trans  Boolean for transactionality 
	 * @return RecordMetadata  RecordMetadata
	 */
	RecordMetadata sendMessage(String topic, String key, String value, boolean trans);

    /**
     * Send create resource message via Kafka
     * 
     * @param resource  The FHIR resource to create on ElasticSearch 
     */
    void sendCreateElasticsearch(FhirPublicationDTO resource);

    
}
