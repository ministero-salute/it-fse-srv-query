package it.finanze.sanita.fse2.ms.srvquery.service;

import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.Serializable;


public interface IKafkaSRV extends Serializable {

    /**
	 * Send message over kafka topic
	 * @param topic
	 * @param key
	 * @param value
	 * @param trans
	 * @return
	 */
	RecordMetadata sendMessage(String topic, String key, String value, boolean trans);

    /**
     * Send create resource message via Kafka
     * @param resource
     */
    void sendCreateElasticsearch(FhirPublicationDTO resource);

    
}
