package app.bladenight.wampv2.testutils;

import app.bladenight.wampv2.server.WampBnServerImpl;
import app.bladenight.wampv2.server.common.Channel;
import org.eclipse.jetty.websocket.api.Session;

public class SessionFactory {
    public Session getNew(WampBnServerImpl wampBnServer , Channel channel) {
        return wampBnServer.registerSession(new ServerClient(wampBnServer).getTestSession(),channel);
    }
}
