/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.config.kafka;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 *
 *	Kafka producer configuration.
 */
@Configuration
@Slf4j
public class KafkaProducerCFG {


	/**
	 *	Kafka properties.
	 */
	@Autowired
	private KafkaPropertiesCFG kafkaPropCFG;

	/**
	 *	Kafka producer properties.
	 */
	@Autowired
	private KafkaProducerPropertiesCFG kafkaProducerPropCFG;


	/**
	 * Kafka Producer Configuration 
	 * @return Map  ProducerConfigs 
	 */
	@Bean 
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();

		InetAddress id = getLocalHost();

		log.info("kafkaCFG.getClientId() : " + kafkaProducerPropCFG.getClientId() + "-tx" + "-" + id );
		props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProducerPropCFG.getClientId() + "-tx" + "-" + id );

		log.info("kafkaCFG.getRetries() : " + kafkaProducerPropCFG.getRetries());
		props.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerPropCFG.getRetries());

		log.info("kafkaCFG.getProducerBootstrapServers() : " + kafkaProducerPropCFG.getProducerBootstrapServers());
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerPropCFG.getProducerBootstrapServers());

		log.info("kafkaCFG.getKeySerializer() : " + kafkaProducerPropCFG.getKeySerializer());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getKeySerializer());

		log.info("kafkaCFG.getValueSerializer() : " + kafkaProducerPropCFG.getValueSerializer());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getValueSerializer());

		log.info("kafkaCFG.getTransactionalId() : " + id + "-" + kafkaProducerPropCFG.getTransactionalId());
		props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, id + "-" + kafkaProducerPropCFG.getTransactionalId());

		log.info("kafkaCFG.getAck() : " + kafkaProducerPropCFG.getAck());
		props.put(ProducerConfig.ACKS_CONFIG,kafkaProducerPropCFG.getAck());

		log.info("kafkaCFG.getIdempotence() : " + kafkaProducerPropCFG.getIdempotence());
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,kafkaProducerPropCFG.getIdempotence());
		//SSL
		if(kafkaPropCFG.isEnableSSL()) {
			props.put("security.protocol", kafkaPropCFG.getProtocol());
			props.put("sasl.mechanism", kafkaPropCFG.getMechanism());
			props.put("sasl.jaas.config", kafkaPropCFG.getConfigJaas());
			props.put("ssl.truststore.location", kafkaPropCFG.getTrustoreLocation());  
			props.put("ssl.truststore.password", String.valueOf(kafkaPropCFG.getTrustorePassword())); 
		}

		return props;
	}

	/**
	 * Retrieves the local host 
	 * 
	 * @return InetAddress  The local host 
	 */
	private InetAddress getLocalHost() {
		InetAddress id = null;
		try {
			id = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			log.error("Errore durante il recupero InetAddress.getLocalHost()", e);
		}
		return id;
	}


	/**
	 * Producer Factory 
	 * 
	 * @return ProducerFactory  factory 
	 */
	@Bean
	@Qualifier("txkafkatemplateFactory") 
	public ProducerFactory<String, String> producerFactory() {
		log.info("Inizializzo la factory transazionale");
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	/**
	 * Tx Kafka Template 
	 * 
	 * @return KafkaTemplate  KafkaTemplate
	 */
	@Bean
	@Qualifier("txkafkatemplate") 
	public KafkaTemplate<String, String> txKafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}


	/**
	 * Producer without tx config 
	 * 
	 * @return Map  producer 
	 */
	@Bean 
	public Map<String, Object> producerWithoutTransactionConfigs() {
		Map<String, Object> props = new HashMap<>();
		log.info("kafkaCFG.getClientId() -noTx :" + kafkaProducerPropCFG.getClientId()+ "-noTx");
		props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProducerPropCFG.getClientId()+ "-noTx");

		log.info("kafkaCFG.getRetries(): " + kafkaProducerPropCFG.getRetries());
		props.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerPropCFG.getRetries());

		log.info("kafkaCFG.getProducerBootstrapServers(): " + kafkaProducerPropCFG.getProducerBootstrapServers());
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerPropCFG.getProducerBootstrapServers());

		log.info("kafkaCFG.getKeySerializer(): " + kafkaProducerPropCFG.getKeySerializer());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getKeySerializer());

		log.info("kafkaCFG.getValueSerializer(): " + kafkaProducerPropCFG.getValueSerializer());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getValueSerializer());
		//SSL
		if(kafkaPropCFG.isEnableSSL()) {
			props.put("security.protocol", kafkaPropCFG.getProtocol());
			props.put("sasl.mechanism", kafkaPropCFG.getMechanism());
			props.put("sasl.jaas.config", kafkaPropCFG.getConfigJaas());
			props.put("ssl.truststore.location", kafkaPropCFG.getTrustoreLocation());
			props.put("ssl.truststore.password", String.valueOf(kafkaPropCFG.getTrustorePassword())); 
		}

		return props;
	}


	/**
	 * Non transactional producer 
	 * 
	 * @return ProducerFactory  factory 
	 */
	@Bean
	@Qualifier("notxkafkatemplateFactory") 
	public ProducerFactory<String, String> producerFactoryWithoutTransaction() {
		log.info("Inizializzo la factory non transazionale");
		return new DefaultKafkaProducerFactory<>(producerWithoutTransactionConfigs());
	}


	/**
	 * notxKafkaTemplate
	 * 
	 * @return KafkaTemplate  template 
	 */
	@Bean
	@Qualifier("notxkafkatemplate") 
	public KafkaTemplate<String, String> notxKafkaTemplate() {
		return new KafkaTemplate<>(producerFactoryWithoutTransaction());
	}


	/**
	 * Factory dead producer.
	 * 
	 * @return ProducerFactory	factory dead producer.
	 */
	@Bean
	public ProducerFactory<Object, Object> producerDeadFactory() {
		return new DefaultKafkaProducerFactory<>(producerWithoutTransactionConfigs());
	}

	/**
	 * Kafka template dead letter.
	 *
	 * @return KafkaTemplate  Kafka template
	 */
	@Bean
	@Qualifier("notxkafkadeadtemplate")
	public KafkaTemplate<Object, Object> noTxKafkaDeadTemplate() {
		return new KafkaTemplate<>(producerDeadFactory());
	}

}