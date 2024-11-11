package com.example.entity;

import com.github.xingshuangs.iot.protocol.rtsp.service.RtspFMp4Proxy;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author yyh
 */
@Data
public class Monitor {

    private WebSocketSession session;
    private RtspFMp4Proxy proxy;
    private String monitoringCode;

    public Monitor(WebSocketSession session) {
        this.session = session;
    }

}
