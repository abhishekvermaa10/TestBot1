package org.example.nasa;

import java.io.IOException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Utils {

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


	public static String getUrl(String url) throws IOException {
		HttpGet request = new HttpGet(url);
		try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            if (status < 200 || status >= 300) {
                throw new IOException("NASA API call failed with status: " + status);
            }
            NASA nasa = OBJECT_MAPPER.readValue(
                    response.getEntity().getContent(),
                    NASA.class
            );
            return nasa.getUrl();
        }

	}

}
