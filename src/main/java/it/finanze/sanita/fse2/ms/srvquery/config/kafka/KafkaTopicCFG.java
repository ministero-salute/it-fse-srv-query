package it.finanze.sanita.fse2.ms.srvquery.config.kafka;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.utility.ProfileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

import javax.annotation.PostConstruct;

/**
 *	@author Riccardo Bonesi
 *
 *	Kafka topic configuration.
 */
@Data
@Component
public class KafkaTopicCFG {

	@Autowired
	private ProfileUtility profileUtility;
	
	/**
	 * Topic for ElasticSearch create feature 
	 */
	@Value("${kafka.create-elasticsearch.topic}")
	private String createElasticsearchTopic;

	// TODO: capire se serve o se questo ms fa solo da producer
	/**
	 * Dead letter Topic. 
	*/
	@Value("${kafka.query.deadletter.topic}")
	private String queryDeadLetterTopic;

	@PostConstruct
	public void afterInit() {
		if (profileUtility.isTestProfile()) {
			this.createElasticsearchTopic = Constants.Profile.TEST_PREFIX + this.createElasticsearchTopic;
			this.queryDeadLetterTopic = Constants.Profile.TEST_PREFIX + this.queryDeadLetterTopic;

		}
	}

}
