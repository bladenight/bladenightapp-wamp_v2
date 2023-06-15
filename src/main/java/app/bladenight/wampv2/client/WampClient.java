package app.bladenight.wampv2.client;

import app.bladenight.wampv2.server.common.Channel;
import app.bladenight.wampv2.server.messages.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class WampClient {

    private static final Logger logger = LogManager.getLogger(WampClient.class);
    public boolean hasBeenWelcomed;
    public Session session;
    public String serverIdent;
    public Long sessionId;
    protected Channel outgoingChannel;
    final Map<String, RpcResultReceiver> rpcResultReceivers = new HashMap<>();
    // TODO the current design doesn't allow to subscribe multiple receivers for a given topic
    final Map<String, EventReceiver> eventReceivers = new HashMap<>();
    protected WelcomeListener welcomeListener;

    public WampClient(Channel outgoingChannel, URI uri, String authorisationHeaderKey) {
        reset();
        if (Objects.equals(authorisationHeaderKey, "")) authorisationHeaderKey="test:test";
        this.outgoingChannel = outgoingChannel;

        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setTrustAll(true);

        WebSocketClient client = new WebSocketClient(sslContextFactory);
        try {
            client.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<String> clientReceived = new ArrayList<>();

        try {
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Authorization", "wss://" + java.util.Base64.getEncoder().encodeToString(authorisationHeaderKey.getBytes()));

            session = client.connect(
                            new org.eclipse.jetty.websocket.api.WebSocketAdapter() {
                                @Override
                                public void onWebSocketText(String message) {
                                    clientReceived.add(message);

                                    handleIncomingMessage(message);

                                }
                            },
                            uri,
                            request)
                    .get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    public WampClient(Channel outgoingChannel) {
        reset();
        this.outgoingChannel = outgoingChannel;

        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setTrustAll(true);

        WebSocketClient client = new WebSocketClient(sslContextFactory);
        try {
            client.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<String> clientReceived = new ArrayList<>();

        try {
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Authorization", "wss://" + java.util.Base64.getEncoder().encodeToString("test:test".getBytes()));

            session = client.connect(
                            new org.eclipse.jetty.websocket.api.WebSocketAdapter() {
                                @Override
                                public void onWebSocketText(String message) {
                                    clientReceived.add(message);

                                    handleIncomingMessage(message);

                                }
                            },
                            new URI("wss://localhost:8081/ws"),
                            request)
                    .get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        long timeBeforeWait = System.nanoTime();

    }

    public void reset() {
        rpcResultReceivers.clear();
        eventReceivers.clear();
        hasBeenWelcomed = false;
    }

    public void handleIncomingMessage(String jsonText) {
        Message message = MessageMapper.fromJson(jsonText);

        // System.out.println("handleIncomingMessage: " + jsonText);
        if (message.getType() == MessageType.WELCOME) {
            handleIncomingWelcomeMessage((WelcomeMessage) message);
            return;
        }

        if (!hasBeenWelcomed) {
            String msg = "handleIncomingMessage: Cannot receive messages until we got a WELCOME message";
            System.err.println(msg);
            throw new IllegalStateException("Client has not been welcomed yet");
        }

        switch (message.getType()) {
            case RESULT:
                handleIncomingCallResultMessage((CallResultMessage) message);
                break;
            case ERROR:
                handleIncomingCallErrorMessage((CallErrorMessage) message);
                break;
            case EVENT:
                handleIncomingEventMessage((EventMessage) message);
                break;
            default:
                logger.error("HandleIncomingMessage doesn't know how to handle message type " + message.getType());
                System.err.println("ERROR: handleIncomingMessage doesn't know how to handle message type " + message.getType());
        }
    }

    protected void handleIncomingWelcomeMessage(WelcomeMessage message) {
        logger.trace(("handleIncomingWelcomeMessage" + message));
        if (hasBeenWelcomed)
            throw new IllegalStateException("Client has already been welcomed");
        //checkNotNull("serverIdent", message.serverIdent);
        //serverIdent = message.serverIdent;
        sessionId = message.sessionId;
        hasBeenWelcomed = true;
        if (welcomeListener != null)
            welcomeListener.onWelcome();
    }

    protected void handleIncomingCallResultMessage(CallResultMessage message) {
        RpcResultReceiver receiver;
        synchronized (rpcResultReceivers) {
            receiver = rpcResultReceivers.remove(message.callId);
        }
        if (receiver == null) {
            logger.error(("ERROR: handleIncomingCallErrorMessage doesn't know a receiver for this call " + message.callId));
            System.err.println("ERROR: handleIncomingCallResultMessage doesn't know a handler for this call " + message.callId);
            return;
        }

        receiver.setCallResultMessage(message);
        receiver.onSuccess();
    }

    protected void handleIncomingCallErrorMessage(CallErrorMessage message) {
        RpcResultReceiver receiver;
        synchronized (rpcResultReceivers) {
            receiver = rpcResultReceivers.remove(message.callId);
        }
        if (receiver == null) {
            logger.error(("ERROR: handleIncomingCallErrorMessage doesn't know a handler for this call " + message.callId));
            System.err.println("ERROR: handleIncomingCallErrorMessage doesn't know a handler for this call " + message.callId);
            return;
        }

        receiver.setCallErrorMessage(message);
        receiver.onError();
    }


    public void handleIncomingEventMessage(EventMessage message) {
        EventReceiver receiver;
        synchronized (eventReceivers) {
            receiver = eventReceivers.remove(message.topicUri);
        }
        if (receiver == null) {
            // TODO logging
            System.err.println("ERROR: handleIncomingEventMessage doesn't know a handler for this topic " + message.topicUri);
            return;
        }
        receiver.setPayloadElement(message.getPayloadAsElement());
        receiver.onReceive();
    }

    public void call(String procedureId, RpcResultReceiver rpcResultHandler) throws IOException {
        String callId = UUID.randomUUID().toString();
        synchronized (rpcResultReceivers) {
            rpcResultReceivers.put(callId, rpcResultHandler);
        }
        session.getRemote().sendString(MessageMapper.toJson(new CallMessage(callId, procedureId)));
    }

    public void call(String procedureId, RpcResultReceiver rpcResultHandler, Object payload) throws IOException {
        String callId = UUID.randomUUID().toString();
        synchronized (rpcResultReceivers) {
            rpcResultReceivers.put(callId, rpcResultHandler);
        }
        CallMessage msg = new CallMessage(callId, procedureId);
        msg.setPayload(payload);
        session.getRemote().sendString(MessageMapper.toJson(msg));
    }

    public void publish(String topicId, Object payload) throws IOException {
        PublishMessage msg = new PublishMessage(topicId);
        msg.setPayload(payload);
        outgoingChannel.handle(MessageMapper.toJson(msg));
    }

    public void subscribe(String topicId, EventReceiver eventReceiver) throws IOException {
        synchronized (eventReceivers) {
            eventReceivers.put(topicId, eventReceiver);
        }
        outgoingChannel.handle(MessageMapper.toJson(new SubscribeMessage(topicId)));
    }

    public void unsubscribe(String topicId) throws IOException {
        synchronized (eventReceivers) {
            eventReceivers.remove(topicId);
        }
        outgoingChannel.handle(MessageMapper.toJson(new UnsubscribeMessage(topicId)));
    }

    public void setWelcomeListener(WelcomeListener welcomeListener) {
        this.welcomeListener = welcomeListener;
    }


    public Object getServerIdent() {
        return serverIdent;
    }

    public Object getSessionId() {
        return sessionId;
    }

    protected void checkNotNull(String id, String s) {
        if (s == null)
            throw new IllegalArgumentException(id + " is null");
    }

    public boolean hasBeenWelcomed() {
        return hasBeenWelcomed;
    }

}
