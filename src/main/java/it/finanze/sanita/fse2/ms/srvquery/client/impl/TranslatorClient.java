package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.TranslateResultDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TranslatorClient extends AbstractTerminologyClient {

	private String srvURL;
	
	public TranslatorClient(final String serverURL) {
		srvURL = serverURL;
	}

	public TranslateResultDTO translate(String text, String sourceLang, String targetLang) {
        HttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(srvURL);
        TranslateResultDTO out = null;
        try {
            String requestBody = "q=" + text + "&source=" + sourceLang + "&target=" + targetLang;
            StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_FORM_URLENCODED);
            request.setEntity(entity);

            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity);

            out = new Gson().fromJson(responseBody, TranslateResultDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
	}

}
