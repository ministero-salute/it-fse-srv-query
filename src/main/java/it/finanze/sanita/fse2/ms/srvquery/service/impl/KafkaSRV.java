/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import it.finanze.sanita.fse2.ms.srvquery.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.KafkaCreateDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.FhirPublicationDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

/**
 * 
 *
 * Kafka management service implementation.
 */
@Service
@Slf4j
public class KafkaSRV implements IKafkaSRV {

    /**
	 * Transactional producer.
	 */
	@Autowired
	@Qualifier("txkafkatemplate")
	private transient KafkaTemplate<String, String> txKafkaTemplate;

    @Autowired
	private transient KafkaTopicCFG kafkaTopicCFG;

	/**
	 * Not transactional producer.
	 */
	@Autowired
	@Qualifier("notxkafkatemplate")
	private transient KafkaTemplate<String, String> notxKafkaTemplate;

    @Override
	public RecordMetadata sendMessage(String topic, String key, String value, boolean trans) {
		RecordMetadata out = null;
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);
		try { 
			out = kafkaSend(producerRecord, trans);
		} catch (Exception e) {
			log.error("Send failed.", e); 
			throw new BusinessException(e);
		}   
		return out;
	} 

    @SuppressWarnings("unchecked")
	private RecordMetadata kafkaSend(ProducerRecord<String, String> producerRecord, boolean trans) {
		RecordMetadata out = null;
		Object result = null;

		if (trans) {  
			result = txKafkaTemplate.executeInTransaction(t -> { 
				try {
					return t.send(producerRecord).get();
				} catch(InterruptedException e) {
					log.error("InterruptedException caught. Interrupting thread...");					
					Thread.currentThread().interrupt(); 
					throw new BusinessException(e); 
				}
				catch (Exception e) {
					throw new BusinessException(e);
				}  
			});  
		} else { 
			notxKafkaTemplate.send(producerRecord);
		} 

		if(result != null) {
			SendResult<String,String> sendResult = (SendResult<String,String>) result;
			out = sendResult.getRecordMetadata();
			log.info("Send success.");
		}

		return out;
	}

    @Override
    public void sendCreateElasticsearch(FhirPublicationDTO resource) {
        try {
            KafkaCreateDTO createMessage = KafkaCreateDTO.builder().
                resource(resource.getJsonString()).
                build();

			String json = StringUtility.toJSONJackson(createMessage);
			sendMessage(kafkaTopicCFG.getCreateElasticsearchTopic(), resource.getIdentifier(), json, true);
		} catch(Exception ex) {
			log.error("Error while send create message on srv-query : " , ex);
			throw new BusinessException(ex);
		}
        
    }


    

    
    
}
