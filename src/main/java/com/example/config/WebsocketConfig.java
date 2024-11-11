package com.example.config;

import com.example.handler.BaseSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author yyh
 */
@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

    private final BaseSocketHandler baseSocketHandler;

    public WebsocketConfig(BaseSocketHandler baseSocketHandler) {
        this.baseSocketHandler = baseSocketHandler;
    }





    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(baseSocketHandler, "/ws").setAllowedOrigins("*");
    }

}
