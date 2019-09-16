# spring-websocket
简单实现springwebsocket
## 原生websocket
jetty

## spring websocket
在spring中可以使用spring整合过的websocket，对于spring websocket的学习我们可以通过官网来系统学习。

[spring websocket官网引导](https://spring.io/guides/gs/messaging-stomp-websocket/)

同样可以在官网可以找到spring为我们准备好的例程
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019091617472126.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1MjYyNDA1,size_16,color_FFFFFF,t_70)
在例程中我们可以了解到spring websocket所需要的依赖和如何使用

### 依赖
```xml
    <!-- spring websocket的依赖 -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>

<!-- spring web的依赖，提供web的支持 -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>


    <dependency>
      <groupId>org.webjars</groupId>
      <artifactId>webjars-locator-core</artifactId>
    </dependency>

<!-- sockjs的依赖，提供对于js中通信的支持 -->
    <dependency>
      <groupId>org.webjars</groupId>
      <artifactId>sockjs-client</artifactId>
      <version>1.0.2</version>
    </dependency>
<!--stomp的依赖，提供一种通信协议，属于websocket的子协议-->
    <dependency>
      <groupId>org.webjars</groupId>
      <artifactId>stomp-websocket</artifactId>
      <version>2.3.3</version>
    </dependency>

    <dependency>
      <groupId>org.webjars</groupId>
      <artifactId>bootstrap</artifactId>
      <version>3.3.7</version>
    </dependency>
<!--打包了jquery的依赖，提供jquery框架-->
    <dependency>
      <groupId>org.webjars</groupId>
      <artifactId>jquery</artifactId>
      <version>3.1.0</version>
    </dependency>
```
至于页面也是使用的spring官网提供的项目中的页面

### websocket配置类
接下来就是对于websocket的配置，首先需要一个WebSocketMessageBrokerConfigurer的实现类，需要实现下面两个方法
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190916175931672.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1MjYyNDA1,size_16,color_FFFFFF,t_70)
这个类上还需要加上两个注解
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019091618003638.png)

下面是这两个方法的具体使用，registerStompEndpoints用来设置客户端与服务端建立通信的路径，configureMessageBroker用来注册客户端能够订阅的消息类型和能够发送的消息前缀。
```java
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
        registry.enableSimpleBroker("/topic");
        //设置可以服务端接收消息的前缀，只有下面注册的前缀的消息才会接收
        registry.setApplicationDestinationPrefixes("/app");

    }
}
```
### controller对象
服务端真正用来接收消息和发送消息使用controller对象来实现的，在这个controller对象我们可以实现发送各种类型的信息，群发或者发送某一个用户组或者对某一用户发送。

也可以接收不同类型的信息，在接收信息后可以做出响应，当然也可以主动对客户端发送信息。

**下面实现了对于消息的接收，并对消息做出了响应**
```java
@Controller
public class WebController {

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

}
```
**同样也可以直接向客户端发送消息**

需要借助``org.springframework.messaging.Message``这个对象向客户端发送消息，同样可以群发或者向特定的客户端发送，不过需要知道客户端约定的名字
```java

/**
 * 通过这个对象可以实现主动向客户端发送消息
 */
@Autowired
SimpMessagingTemplate template;

@GetMapping("/sendMessage")
public void sendMessage(String message){
    //template主要有两种方法，一种是convertAndSend，还有一种是convertAndSendToUser
    //其中第一种属于群发，只要订阅了指定的消息类型就可以收到消息
    //第二种属于点对点发送，可以发送给单独的客户端
    template.convertAndSend("/topic/greetings",message);
    //需要注意的是这种对单独的客户端发送消息也是通过一个消息类型发送的
    //而且消息的类型默认是/user，这个是websocket对单个客户端发送消息特殊的消息类型
    //所以需要在websocket配置类中注册/user消息类型
    //第二个参数时加在名字后面的路径
    //这里的消息类型是/user/zdd/ptp
    //接收时必须这么接收
    template.convertAndSendToUser("zdd","/ptp",message);
}
```

### 客户端
客户端的js代码如下，包括与服务端连接还有消息的接收和发送
```js
var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (data) {
            console.log(data);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});
```
### 点对点聊天的实现思路
这里通过SimpMessagingTemplate对象的convertAndSendToUser方法向特定的客户端发送消息。

客户端的js代码
```js
var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/guide-websocket');
    var uname = $("#uname").val();
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/'+uname+'/ptp', function (data) {
            console.log(data);
            $("#buffer").append("<div>"+data+"</div>");
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}

function sendOther() {
    var oname = $("#oname").val();
    var message = $("#message").val();
    stompClient.send("/app/sayTo", {}, message+"-"+oname);
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendOther(); });
});
```
```html
<!DOCTYPE html>
<html>
<head>
    <title>Hello WebSocket</title>
    <link href="/webjars/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
    <link href="/main.css" rel="stylesheet">
    <script src="/webjars/jquery/3.1.0/jquery.min.js"></script>
    <script src="/webjars/sockjs-client/1.0.2/sockjs.min.js"></script>
    <script src="/webjars/stomp-websocket/2.3.3/stomp.min.js"></script>
    <script src="/app.js"></script>
</head>
<body>
<div id="main-content" class="container">
    <div class="row">
        <div class="col-md-6">
            <form class="form-inline">
                <div class="form-group">
                    <label for="connect">WebSocket connection:</label>
                    <button id="connect" class="btn btn-default" type="submit">Connect</button>
                    <button id="disconnect" class="btn btn-default" type="submit" disabled="disabled">Disconnect
                    </button>
                </div>
            </form>
        </div>
        <div class="col-md-6">
            <form class="form-inline">
                <div class="form-group">
                    <label >What is your name?</label>
                    <input type="text" id="uname" class="form-control" placeholder="Your name here...">
                    <label >What is other name?</label>
                    <input type="text" id="oname" class="form-control" placeholder="other name here...">
                    <label >What is your message?</label>
                    <input type="text" id="message" class="form-control" placeholder="Your message here...">
                </div>
                <button id="send" class="btn btn-default" type="submit">Send</button>
            </form>
        </div>

    </div>
    <div class="row">
        <div class="col-md-12">
            <table id="conversation" class="table table-striped">
                <thead>
                <tr>
                    <th>Greetings</th>
                </tr>
                </thead>
                <tbody id="greetings">
                </tbody>
                <div id="buffer">
                </div>
            </table>
        </div>
    </div>
</div>
</body>
</html>
```
服务端的controller
```java
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

```
注意websocket的配置类一定要注册/user对象
![在这里插入图片描述](https://img-blog.csdnimg.cn/20190916211737910.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM1MjYyNDA1,size_16,color_FFFFFF,t_70)
