package src.websocketservice.pojo;

import lombok.Data;

// 用于登录响应
@Data
public class Result {
    private boolean flag;
    private String message;
}
