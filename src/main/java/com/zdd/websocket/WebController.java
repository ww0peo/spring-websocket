package com.zdd.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    /**
     * 通过这个对象可以实现主动向客户端发送消息
     */
    @Autowired
    SimpMessagingTemplate template;

    //接收/hello的消息，注意这里还需要要加上前缀，在websocket的配置类中设置前缀
    //这里的前缀是/app，所以会接收/app/hello的消息
    @MessageMapping("/hello")
    //群发，发送给订阅了/topic/greetings消息类型的客户端
    //如果要对单个对象发送可以使用@SendToUser注解
    @SendTo("/topic/greetings")
    public String sayHello(Message message){
        //这里得到的是通过stomp协议编写的消息体，也可以通过getHeaders()方法得到消息头
        //stomp协议是websocket的子协议
        byte[] payload = (byte[]) message.getPayload();
        String str = new String(payload);
        System.out.println(str);
        //响应消息，这里会见消息发送给订阅了/topic/greetings的客户端，属于群发
        return "hello!";
    }

    /**
     * 私聊方法，传递过来的消息体的格式是
     *
     *  需要发送的人-发送的消息
     *
     * @param message
     */
    @MessageMapping("/sayTo")
    public void sayTo(Message message){
        byte[] payload = (byte[]) message.getPayload();
        String str = new String(payload);
        String[] strings = str.split("-");
        System.out.println(str);
        sendMessage(strings[0],strings[1]);
    }

    @GetMapping("/sendMessage")
    public void sendMessage(String message,String userName){
        //template主要有两种方法，一种是convertAndSend，还有一种是convertAndSendToUser
        //其中第一种属于群发，只要订阅了指定的消息类型就可以收到消息
        //第二种属于点对点发送，可以发送给单独的客户端
        //template.convertAndSend("/topic/greetings",message);
        //需要注意的是这种对单独的客户端发送消息也是通过一个消息类型发送的
        //而且消息的类型默认是/user，这个是websocket对单个客户端发送消息特殊的消息类型
        //所以需要在websocket配置类中注册/user消息类型
        //第二个参数时加在名字后面的路径
        //这里的消息类型时/user/userName/ptp
        //接收时必须这么接收
        template.convertAndSendToUser(userName,"/ptp",message);
    }

}
