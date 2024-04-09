package src.websocketservice.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import src.websocketservice.pojo.Message;
import src.websocketservice.util.MessageUtil;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/chat",configurator = GetHttpSessionConfigurator.class)
@Component
public class ChatEndPoint {
    //  用来存储每一个客户端对象对应的ChatEndPoint对象
    private static Map<String,ChatEndPoint> onlineUsers = new ConcurrentHashMap<>();
    // 声明session对象，通过该对象可以发送消息给指定的用户
    private Session session;
    // 声明一个httpSession对象，我们之前在HttpSession中存储了用户名
    private HttpSession httpSession;
    @OnOpen
    // 连接建立时被调用
    public void onOpen(Session session, EndpointConfig config){
        // 将局部的session对象赋值给成员session
        this.session = session;
        // 获取httpSession对象
        HttpSession httpSession =(HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        this.httpSession = httpSession;
        // 从httpSession对象中获取用户名
        String username = (String) httpSession.getAttribute("user");
        // 将当前对象
        onlineUsers.put(username,this);
        // 将当前在线用户的用户名推送给所有的客户端
        // 1. 获取消息
        String message = MessageUtil.getMessage(true, null, getNames());
        // 2. 调用方法进行系统消息的推送
    }
    private void broadcastAllUsers(String message) throws IOException {
        // 要将该消息推送给所有的客户端
        Set<String> names = onlineUsers.keySet();
        for(String name : names){
            ChatEndPoint chatEndPoint = onlineUsers.get(name);
            chatEndPoint.session.getBasicRemote().sendText(message);
        }
    }
    private Set<String> getNames(){
        return onlineUsers.keySet();
    }
    @OnMessage
    // 接收到客户端发送的数据时被调用
    public void onMessage(String message,Session session) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Message mess = mapper.readValue(message, Message.class);
        // 获取要将数据发送给的用户
        String toName = mess.getToName();
        // 获取消息数据
        String data = mess.getMessage();
        // 获取当前登录的用户
        String username = (String) httpSession.getAttribute("user");
        // 获取推送给指定用户的消息格式的数据
        String resultMessage = MessageUtil.getMessage(false, username, data);
        // 发送数据
        onlineUsers.get(toName).session.getBasicRemote().sendText(resultMessage);
    }
    @OnClose
    // 连接关闭时被调用
    public void onClose(Session session) throws IOException {
        String username = (String) httpSession.getAttribute("user");
        // 从容器中删除指定的用户
        onlineUsers.remove(username);
        // 获取推送的消息
        String message = MessageUtil.getMessage(true, null, getNames());
        broadcastAllUsers(message);
    }
}
