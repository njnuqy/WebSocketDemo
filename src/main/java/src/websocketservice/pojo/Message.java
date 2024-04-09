package src.websocketservice.pojo;

import lombok.Data;
// 浏览器发送给服务器的websocket数据
@Data
public class Message {
    private String toName;
    private String message;
}
