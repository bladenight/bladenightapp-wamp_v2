package app.bladenight.wampv2.server;

import app.bladenight.wampv2.server.common.Channel;
import app.bladenight.wampv2.server.common.WampCloseCodes;
import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.fail2ban.Banned;
import app.bladenight.wampv2.server.fail2ban.Fail2Ban;
import app.bladenight.wampv2.server.messages.*;
import app.bladenight.wampv2.server.utilities.GeneralUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WampBnServerImpl implements WampBnServer {

    private static final Logger logger = LogManager.getLogger(WampBnServerImpl.class);
    public static ConcurrentHashMap<String, SessionIdContainer> openSessionMapping = new ConcurrentHashMap<>();

    protected Map<String, RpcHandler> rpcHandlers;
    protected Subscriptions subscriptions;
    protected String serverIdent;
    protected List<TextFrameEavesdropper> incomingFrameEavesDropper = new ArrayList<>();
    protected Channel channel;
    final GeneralUtilities generalUtilities = new GeneralUtilities();

    static {
        System.setProperty("log4j.configurationFile", "log4j.xml");
    }

    private Fail2Ban fail2ban;

    public static void main(String[] args) {
        logger.info("WampBNImpl Main init");
    }

    public WampBnServerImpl() {
        init();
    }

    public void init() {
        rpcHandlers = new ConcurrentHashMap<>();
        serverIdent = "BNWampServer";
        subscriptions = new Subscriptions();
        this.fail2ban = new Fail2Ban();
        new Thread(fail2ban).start();

    }


    public WampBnServerImpl(String serverIdent) {
        init();
        this.serverIdent = serverIdent;
    }

    /**
     * Stores all websocket client sessions in the internal map 'sessionMap'.
     * Note sessions are 'internally' identified using the encrypted handshake 'Sec-WebSocket-Accept' value and
     * 'externally' identified using an allocated UUID.
     */
    public Session registerSession(final Session session, Channel channel) {
        logger.debug("registerSession called " + session.getRemote() + ":" + session.getRemoteAddress());
        session.setIdleTimeout(MAX_IDLE_TIMEOUT_MILLIS);
        final Long assignedId = generalUtilities.getNextSessionID();
        String WS_HAND_SHAKE_KEY = "Sec-WebSocket-Accept";
        final String handShakeKey = session.getUpgradeResponse().getHeader(WS_HAND_SHAKE_KEY);
        final SessionIdContainer sessionIdContainer = new SessionIdContainer(assignedId, handShakeKey, session, GeneralUtilities.getCurrentDate());
        this.channel = channel;
        openSessionMapping.put(getSessionIdent(session), sessionIdContainer);
        sendMessageToClient(session, MessageMapper.toJson(new WelcomeMessage(getExternalId(session), serverIdent)));
        return session;

    }

    public void closeSession(final Session session, int closeCode, String reason) {
        if (session.isOpen()) session.close();
        openSessionMapping.remove(getSessionIdent(session));
        logger.info("WAMP session closed: " + closeCode + " Reason:" + reason);

    }

    public Long getExternalId(final Session session) {
        if (openSessionMapping.containsKey(getSessionIdent(session))) {
            final SessionIdContainer idWrapper = openSessionMapping.get(getSessionIdent(session));
            return idWrapper.getId();
        }
        return 0L;
    }

    public void handleIncomingString(Session session, String jsonText) throws IOException, BadArgumentException {
        final Long externalId = getExternalId(session);
        if (externalId == null) {
            closeSession(session, WampCloseCodes.CLOSE_BAD_DATA, "unknown external Id");
            throw new IllegalStateException("unknown external Id");
        }
        Message message = MessageMapper.fromJson(jsonText);
        if (message == null) {
            this.fail2ban.addOrUpdateBanned(session,jsonText);
            session.getRemote().sendString("[" + MessageType.ERROR + "] " + WampCloseCodes.CLOSE_BAD_DATA + ",Invalid message:" + jsonText);
            closeSession(session, WampCloseCodes.CLOSE_BAD_DATA, "Invalid message"); //close session on bad requests
            throw new BadArgumentException("Could not parse the input jsonText:" + jsonText);
        }
        handleIncomingMessage(session, message);
        if (this.channel != null) {
            this.channel.handle(String.valueOf(message));
        }
    }

    public void handleIncomingMessage(Session session, Message message) throws BadArgumentException {
        if (incomingFrameEavesDropper.size() > 0)
            notifyIncomingFramesEavesdroppers(String.valueOf(session.hashCode()), MessageMapper.toJson(message));
        /*if (!isValidSession(session)) {
            session.close();
            throw new BadArgumentException("Invalid session " + session);
        }*/
        final Long externalId = getExternalId(session);
        if (externalId == null) {
            closeSession(session, WampCloseCodes.CLOSE_BAD_DATA, "unknown external Id");
            throw new IllegalStateException("unknown external Id");
        }
        switch (message.getType()) {
            case CALL:
                handleIncomingCallMessage(session, (CallMessage) message);
                break;
            case SUBSCRIBE:
                handleIncomingSubscribeMessage(session, (SubscribeMessage) message);
                break;
            case UNSUBSCRIBE:
                handleIncomingUnsubscribeMessage(session, (UnsubscribeMessage) message);
                break;
            case PUBLISH:
                handleIncomingPublishMessage(session, (PublishMessage) message);
                break;
            case HELLO:
                sendMessageToClient(session, MessageMapper.toJson(new WelcomeMessage(getExternalId(session), serverIdent)));
                break;
            default:
                throw new BadArgumentException("Unsupported message type: " + message.getType());
        }
    }

    protected void handleIncomingSubscribeMessage(Session session, SubscribeMessage message) {
        subscriptions.subscribe(getExternalId(session).toString(), message.topicUri);
    }

    protected void handleIncomingUnsubscribeMessage(Session session, UnsubscribeMessage message) {
        subscriptions.unsubscribe(String.valueOf(session.hashCode()), message.topicUri);

    }

    protected void handleIncomingCallMessage(Session session, CallMessage message) {
        String procedureId = message.procedureId;
        RpcCall rpcCall = new RpcCall(String.valueOf(session.hashCode()), message);
        RpcHandler handler = rpcHandlers.get(procedureId);
        if (handler != null) {
            handler.execute(rpcCall);
            sendMessageToClient(session, rpcCall.getCallResultAsJson());
        } else {
            rpcCall.setError("http://bladenight.app/error/noHandlerForProcedure", "No handler defined for " + procedureId);
            sendMessageToClient(session, rpcCall.getCallResultAsJson());
        }
    }

    protected void handleIncomingPublishMessage(final Session session, final PublishMessage message) {

        EventMessage eventMessage = new EventMessage(message.topicUri);
        eventMessage.setPayloadJsonElement(message.payload);
        final String eventMessageJson = MessageMapper.toJson(eventMessage);
        logger.debug("handleIncomingSubscriptionMessage " + getExternalId(session) + " Message:" + message);

        Subscriptions.ActionOnSubscriber action = new Subscriptions.ActionOnSubscriber() {
            @Override
            public void execute(String subscriberClientId) {
                if (shallSendPublish(message.excludeMe, String.valueOf(session.hashCode()), subscriberClientId)) {
                    try {
                        sendMessageToClient(getSession(subscriberClientId), eventMessageJson);
                    } catch (BadArgumentException e) {
                        // The session has been discarded in the meantime, there is not much we can do about it
                    }
                }
            }
        };
        subscriptions.forAllSubscribers(message.topicUri, action);
    }


    protected boolean shallSendPublish(Boolean excludeMe, String from, String to) {
        return excludeMe == null || !excludeMe || !Objects.equals(from, to);
    }

    protected void sendMessageToClient(Session session, String message) {
        try {
            logger.debug("sendMessageToClient " + getExternalId(session) + " Message:" + message);
            session.getRemote().sendString(message);
            if (this.channel != null) {
                this.channel.handle(String.valueOf(message));
            }
        } catch (IOException e) {
            // TODO
            System.out.println("Looks like client " + session + " is not reachable anymore. Discarding.");
            closeSession(session, 0, e.getLocalizedMessage());
        }
    }

    public Session getSession(String sessionId) throws BadArgumentException {
        boolean isInSessionMap = openSessionMapping.containsKey(sessionId);
        if (isInSessionMap) return openSessionMapping.get(sessionId).getSession();
        else {
            throw new BadArgumentException("unknown Session");
        }

    }

    public boolean isValidSession(Session session) {
        return openSessionMapping.containsValue(getSessionIdent(session));
    }

    private String getSessionIdent(@NotNull Session session) {
        return session.getRemoteAddress().toString().replace("/", "_").replace(":", "_");
    }

    public boolean isValidSession(String sessionId) {
        return openSessionMapping.containsKey(sessionId);
    }


    public void registerRpcHandler(String procedureId, RpcHandler rpcHandler) {
        rpcHandlers.put(procedureId, rpcHandler);
    }

    private boolean isSessionOpen(Session session) {
        return (session != null) && (session.isOpen());
    }

    public void addIncomingFramesEavesdropper(TextFrameEavesdropper incomingEavesdropper) {
        incomingFrameEavesDropper.add(incomingEavesdropper);
    }

    public void removeIncomingFramesEavesdropper(TextFrameEavesdropper incomingEavesdropper) {
        incomingFrameEavesDropper.remove(incomingEavesdropper);
    }

    public void notifyIncomingFramesEavesdroppers(String session, String frame) {
        for (TextFrameEavesdropper eavesdropper : incomingFrameEavesDropper)
            eavesdropper.handler(session, frame);
    }

    public boolean isBanned(ServletUpgradeRequest req) {
        final String remoteAddress = req.getRemoteAddress();
        Banned ban = new Banned(remoteAddress.replace("/",""), "");
        return false;
       // return this.fail2ban.isBanned(ban);
    }

    public boolean isBanned(String remoteAddress, String deviceId) {
        Banned ban = new Banned(remoteAddress, "");
        return false;
        //return this.fail2ban.isBanned(ban);
    }

    public void addToBanned(ServletUpgradeRequest req) {
        //this.fail2ban.addOrUpdateBanned(req);
    }

    private final class SessionIdContainer {
        private final Long id;
        private final int hashCode;
        private final String handShakeKey;
        private final Session session;
        private final Date dateJoined;

        public long getId() {
            return id;
        }

        public int hashCode() {
            return this.hashCode;
        }

        public String getHandShakeKey() {
            return handShakeKey;
        }

        public Session getSession() {
            return session;
        }

        public Date getDateJoined() {
            return dateJoined;
        }

        public SessionIdContainer(final Long id, final String handShakeKey, final Session session, final Date dateJoined) {
            this.hashCode = session.hashCode();
            this.id = id;
            this.handShakeKey = handShakeKey;
            this.session = session;
            this.dateJoined = dateJoined;
        }
    }
}
