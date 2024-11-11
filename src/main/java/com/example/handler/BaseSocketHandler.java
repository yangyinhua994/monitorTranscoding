package com.example.handler;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.example.entity.Monitor;
import com.example.response.Result;
import com.github.xingshuangs.iot.protocol.rtsp.authentication.DigestAuthenticator;
import com.github.xingshuangs.iot.protocol.rtsp.authentication.UsernamePasswordCredential;
import com.github.xingshuangs.iot.protocol.rtsp.enums.ERtspTransportProtocol;
import com.github.xingshuangs.iot.protocol.rtsp.service.RtspClient;
import com.github.xingshuangs.iot.protocol.rtsp.service.RtspFMp4Proxy;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yyh
 */
@Slf4j
@Component
public class BaseSocketHandler extends AbstractWebSocketHandler {

    public static final ConcurrentHashMap<String, Monitor> SESSION_MAP = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        SESSION_MAP.put(session.getId(), new Monitor(session));
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        close(SESSION_MAP.get(session.getId()));
    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {
        close(SESSION_MAP.get(session.getId()));
    }

    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) {
        String string = message.getPayload().toString();
        String rtsp = "rtsp://";
        if (StrUtil.isNotBlank(string) && string.startsWith(rtsp)) {
            openRtspFmp4Proxy(string, session);
        }
    }

    private void openRtspFmp4Proxy(String rtspUrl, WebSocketSession session) {
        try {
            URI srcUri = URI.create(rtspUrl);
            int i = rtspUrl.indexOf("@");
            URI uri = i < 0 ? srcUri : URI.create("rtsp://" + rtspUrl.substring(i + 1));
            DigestAuthenticator authenticator = null;
            if (srcUri.getUserInfo() != null) {
                UsernamePasswordCredential credential = UsernamePasswordCredential.createBy(srcUri.getUserInfo());
                authenticator = new DigestAuthenticator(credential);
            }
            RtspClient client = new RtspClient(uri, authenticator, ERtspTransportProtocol.UDP);
            RtspFMp4Proxy proxy = new RtspFMp4Proxy(client);
            SESSION_MAP.get(session.getId()).setProxy(proxy);
            proxy.onFmp4DataHandle(x -> sendBinaryData(session, x));
            proxy.onCodecHandle(x -> sendTextData(session, x));
            proxy.onDestroyHandle(() -> close(SESSION_MAP.get(session.getId())));
            proxy.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            close(SESSION_MAP.get(session.getId()));
            sendTextData(session, JSON.toJSONString(Result.fail(e.getMessage())));
        }

    }

    private void sendBinaryData(WebSocketSession session, byte[] data) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new BinaryMessage(ByteBuffer.wrap(data)));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    private void sendTextData(WebSocketSession session, String data) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(data));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void close(Monitor monitor) {
        if (monitor == null) {
            return;
        }
        WebSocketSession session = monitor.getSession();
        closeSession(session);
        stopProxy(monitor.getProxy());
        SESSION_MAP.remove(session.getId());
    }

    private void closeSession(WebSocketSession session) {
        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void stopProxy(RtspFMp4Proxy proxy) {
        if (proxy != null) {
            proxy.stop();
        }
    }

}


