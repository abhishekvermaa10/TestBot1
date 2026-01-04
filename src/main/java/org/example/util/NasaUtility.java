package org.example.util;

import java.io.IOException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class NasaUtility {

	private static final int CONNECT_TIMEOUT_MS = 5_000;
	private static final int SOCKET_TIMEOUT_MS = 30_000;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private static final CloseableHttpClient HTTP_CLIENT =
            HttpClientBuilder.create()
                    .setDefaultRequestConfig(
                            RequestConfig.custom()
                                    .setConnectTimeout(CONNECT_TIMEOUT_MS)
                                    .setSocketTimeout(SOCKET_TIMEOUT_MS)
                                    .setRedirectsEnabled(false)
                                    .build()
                    )
                    .build();


	public static String getDailyImageUrl(String url) throws IOException {
		HttpGet request = new HttpGet(url);
		try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            if (status < 200 || status >= 300) {
            	log.error("NASA API call failed with status: " + status);
                throw new IOException("NASA API call failed with status: " + status);
            }
            JsonNode root = OBJECT_MAPPER.readTree(response.getEntity().getContent());
            JsonNode urlNode = root.get("url");
            if (urlNode == null || urlNode.isNull()) {
            	log.error("NASA API response missing 'url' field");
                throw new IOException("NASA API response missing 'url' field");
            }
            return urlNode.asText();
        }
	}

}
