/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.service;

import java.io.IOException;
import java.io.Serializable;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ElementNotFoundException; 

/**
 * Interface for the ElasticSearch Service 
 * 
 * @author Guido Rocco
 * 
 */
public interface IElasticSearchSRV extends Serializable {

	
	/** 
	 * Indexes a document on ElasticSearch 
	 * 
	 * @param index  The index where the document needs to be added on ElasticSearch 
	 * @param id  The ID of the document 
	 * @param document  The document to index 
	 * @return Object  The result of the indexing 
	 * @throws ElasticsearchException  Generic ElasticSearch Exception 
	 * @throws IOException  Generic IO Exception 
	 * @throws ElementNotFoundException  An exception thrown when the element is not found on ElasticSearch 
	 */
	public Object index(String index, String id, Object document) throws ElasticsearchException, IOException, ElementNotFoundException; 
	

	/** 
	 * Retrieves a document from ElasticSearch given its index and id 
	 * 
	 * @param index  The index of the document to search 
	 * @param id  The ID of the document 
	 * @return Object  The document retrieved from ElasticSearch, if found 
	 * @throws ElasticsearchException  Generic ElasticSearch Exception 
	 * @throws IOException  Generic IO Exception 
	 * @throws ElementNotFoundException  An exception thrown when the element is not found on ElasticSearch 
	 */
	public Object getElementByIndexAndId(String index, String id) throws ElasticsearchException, IOException, ElementNotFoundException; 
}
