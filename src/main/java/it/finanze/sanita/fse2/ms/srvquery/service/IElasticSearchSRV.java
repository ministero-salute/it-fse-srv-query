package it.finanze.sanita.fse2.ms.srvquery.service;

import java.io.IOException;
import java.io.Serializable;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.ElementNotFoundException; 

/**
 * 
 * @author Guido Rocco
 * 
 */
public interface IElasticSearchSRV extends Serializable {

	
	/*
	 * Index a document on ElasticSearch 
	 */
	public Object index(String index, String id, Object document) throws ElasticsearchException, IOException, ElementNotFoundException; 
	
	
	/*
	 * Retrieves an element from ElasticSearch given its index and its ID 
	 */
	public Object getElementByIndexAndId(String index, String id) throws ElasticsearchException, IOException, ElementNotFoundException; 
}
