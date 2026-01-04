package org.example.util;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PicsumUtility {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int SOCKET_TIMEOUT_MS = 30_000;

    private static final CloseableHttpClient HTTP_CLIENT =
            HttpClientBuilder.create()
                    .disableRedirectHandling()
                    .setDefaultRequestConfig(
                            RequestConfig.custom()
                                    .setConnectTimeout(CONNECT_TIMEOUT_MS)
                                    .setSocketTimeout(SOCKET_TIMEOUT_MS)
                                    .build()
                    )
                    .build();

    public static String getDailyImageUrl(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            if (status == 302 || status == 301) {
                Header location = response.getFirstHeader("Location");
                if (location != null) {
                    return location.getValue();
                }
                throw new IOException("Picsum redirect missing Location header");
            }
            if (status >= 200 && status < 300) {
                return url;
            }
            throw new IOException("Picsum API failed with status: " + status);
        }
    }
    
}
