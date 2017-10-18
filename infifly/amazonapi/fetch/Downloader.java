package infifly.amazonapi.fetch;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import ansj.sun.fetch.amazonapi.api.core.Page;
import ansj.sun.fetch.amazonapi.api.core.Request;

public class Downloader {

	 
	private Logger logger = Logger.getLogger(getClass());
	public Page download(Request request) {
	 
		CloseableHttpResponse httpResponse = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		 
		Page page = new Page();
		try {
			HttpGet httpGet = new HttpGet(request.getUrl());
			httpResponse = httpClient.execute(httpGet);
			int code=httpResponse.getStatusLine().getStatusCode();
			page.setCode(code);
			byte[] bytes = IOUtils.toByteArray(httpResponse.getEntity()
					.getContent());
			//String content=new String(bytes, "utf8");
			page.setContent(bytes);
		    httpClient.close();
			return page;
		} catch (IOException e) {
			logger.info("download fail.."+request.getUrl());
			 
			return page;
		} finally {
			if (httpResponse != null) {
				// ensure the connection is released back to pool
				EntityUtils.consumeQuietly(httpResponse.getEntity());
			}
		}
	}
	 
}
