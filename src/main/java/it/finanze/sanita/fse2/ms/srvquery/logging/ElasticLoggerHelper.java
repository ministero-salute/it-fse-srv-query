package it.finanze.sanita.fse2.ms.srvquery.logging;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.ResultLogEnum;
import net.logstash.logback.argument.StructuredArguments;

/** 
 * 
 * @author: Guido Rocco - IBM 
 */ 
@Service
public class ElasticLoggerHelper implements Serializable {
    

	
	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 6847248381101047550L; 
	

	transient Logger log = LoggerFactory.getLogger("elastic-logger"); 
	
	@Value("${spring.application.name}")
	private String loggingApplication; 
	
	/* 
	 * Specify here the format for the dates 
	 */
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
	
	
	
	/* 
	 * Implements structured logs, at all logging levels
	 */
	public void trace(String message, ILogEnum operation, 
			   ResultLogEnum result, Date startDateOperation) {
		
		log.trace(message, StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_APP_NAME, loggingApplication), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_NAME, operation.getCode()), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP, dateFormat.format(new Date())),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_RESULT, result.getCode()),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_START, dateFormat.format(startDateOperation)),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_END, dateFormat.format(new Date()))); 
	} 
	
	public void debug(String message,  ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation) {
		
		log.debug(message, StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_APP_NAME, loggingApplication), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_NAME, operation.getCode()), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP, dateFormat.format(new Date())),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_RESULT, result.getCode()),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_START, dateFormat.format(startDateOperation)),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_END, dateFormat.format(new Date()))); 
	} 
	 
	public void info(String message, ILogEnum operation,  
			ResultLogEnum result, Date startDateOperation) {
		
		log.info(message, StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_APP_NAME, loggingApplication), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_NAME, operation.getCode()), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP, dateFormat.format(new Date())),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_RESULT, result.getCode()),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_START, dateFormat.format(startDateOperation)),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_END, dateFormat.format(new Date()))); 
	} 
	
	public void warn(String message, ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation) {
		
		log.warn(message, StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_APP_NAME, loggingApplication), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_NAME, operation.getCode()), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP, dateFormat.format(new Date())),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_RESULT, result.getCode()),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_START, dateFormat.format(startDateOperation)),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_END, dateFormat.format(new Date()))); 
	} 
	
	public void error(String message, ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation,
			   ILogEnum error) {
		
		log.error(message, StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_APP_NAME, loggingApplication), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_NAME, operation.getCode()), 
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP, dateFormat.format(new Date())),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_RESULT, result.getCode()),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_START, dateFormat.format(startDateOperation)),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_TIMESTAMP_END, dateFormat.format(new Date())),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_ERROR_CODE, error.getCode()),
				 StructuredArguments.kv(Constants.Logs.ELASTIC_LOGGER_OP_ERROR_DESCRIPTION, error.getDescription())); 
	}
    	
    
} 

