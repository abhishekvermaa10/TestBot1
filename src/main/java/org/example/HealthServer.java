package org.example;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.example.config.BotConfig;

import com.sun.net.httpserver.HttpServer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HealthServer {

	public static void start(int port) {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/", exchange -> {
				String response = BotConfig.botUsername() + " is running";
				exchange.sendResponseHeaders(200, response.length());
				try (OutputStream os = exchange.getResponseBody()) {
					os.write(response.getBytes());
				}
			});
			server.start();
			log.info("Health server started on port {}", port);
		} catch (Exception e) {
			log.error("Failed to start health server", e);
		}
	}
	
}
