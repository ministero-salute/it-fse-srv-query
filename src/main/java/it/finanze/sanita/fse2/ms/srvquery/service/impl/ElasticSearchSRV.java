/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ElementNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.service.IElasticSearchSRV;
import lombok.extern.slf4j.Slf4j;


/**
 * ElasticSearch Service implementation 
 * 
 * 
 */
@Service
@Slf4j
public class ElasticSearchSRV implements IElasticSearchSRV {

	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = -6476423834179828573L; 
	
	
	@Autowired
	private ElasticsearchClient client; 



	@Override
	public IndexResponse index(String index, String id, Object document)
			throws ElasticsearchException, IOException {
		return client.index(i -> i.index(index).id(id).document(document)); 
	}


	@Override
	public Object getElementByIndexAndId(String index, String id) throws ElasticsearchException, IOException, ElementNotFoundException {
		if(client.exists(b -> b.index(index).id(id)).value()) {
			try {
				Object elem = client.get(g -> g.index(index).id(id), Object.class); 
				return elem; 
			} catch(Exception e) {
				log.error("Error while querying ElasticSearch"); 
				return null; 
			}

		} else {
			throw new ElementNotFoundException("Element does not exists on ElasticSearch"); 
		}
	}

}
