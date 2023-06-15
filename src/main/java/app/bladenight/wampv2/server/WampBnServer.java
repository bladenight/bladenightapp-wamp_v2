package app.bladenight.wampv2.server;

import app.bladenight.wampv2.server.common.Channel;
import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import java.io.IOException;

public interface WampBnServer {


    int MAX_IDLE_TIMEOUT_MILLIS = 3600000; //max 1hour
    String WS_SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";

    void init();

    Session registerSession(final Session session, Channel channel);

    void closeSession(final Session session, final int closeCode, String reason);

    void handleIncomingString(final Session session, final String incomingString) throws IOException, BadArgumentException;

    void addIncomingFramesEavesdropper(TextFrameEavesdropper incomingEavesdropper);

    Long getExternalId(final Session session);

    boolean isBanned(ServletUpgradeRequest req);

    boolean isBanned(String remoteAddress,String deviceId);

    void addToBanned(ServletUpgradeRequest req);

}