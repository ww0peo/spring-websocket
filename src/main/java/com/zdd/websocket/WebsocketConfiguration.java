package com.zdd.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //加上客户端通信的点路径，客户端就是通过这个点路径建立通信的
        registry.addEndpoint("/guide-websocket").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //客户端可以订阅的信息类型，如果没有在这里注册的信息类型客户端是收不到的
        //参数是可变参数，可以传进去多种信息类型
        //其中第二个/user参数是发送单个客户端时的消息类型
        registry.enableSimpleBroker("/topic","/user");
        //设置可以服务端接收消息的前缀，只有下面注册的前缀的消息才会接收
        registry.setApplicationDestinationPrefixes("/app");

    }
}
