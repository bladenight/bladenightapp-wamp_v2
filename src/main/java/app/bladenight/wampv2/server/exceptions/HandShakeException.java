
package app.bladenight.wampv2.server.exceptions;

import org.eclipse.jetty.websocket.api.WebSocketException;


public class HandShakeException extends WebSocketException
{
    public HandShakeException(String message) {
    super(message);
}
}