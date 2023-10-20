package com.games.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.games.models.yacht.YachtWebsocketHandler;

@Configuration
@EnableWebSocket
public class GamesWebsocketConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// TODO Auto-generated method stub
		registry.addHandler(new YachtWebsocketHandler(), "/yachtWS").setAllowedOrigins("*");
	}

}
