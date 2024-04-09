package src.websocketservice.pojo;
//服务器发送给浏览器的websocket数据

import lombok.Data;

@Data
public class ResultMessage {
    private boolean isSystem;
    private String fromName;
    private Object message;//如果系统消息是数组
}
