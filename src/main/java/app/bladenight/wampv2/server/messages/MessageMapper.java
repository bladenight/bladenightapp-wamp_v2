package app.bladenight.wampv2.server.messages;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;

public class MessageMapper {

    static JsonParser parser;
    static Gson gson;

    public static Message fromJson(String json) throws JsonParseException {
        Message message = null;
        try {
            message = fromJsonTryBlock(json);
        } catch (Exception e) {
            fromJsonCaughtException(e, json);
        }
        return message;
    }

    public static Message fromJsonTryBlock(String json) {
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        MessageType messageType = MessageType.fromInteger(getGson().fromJson(array.get(0), Integer.class));
        switch (messageType) {
            case WELCOME:
                return getGson().fromJson(json, WelcomeMessage.class);
            case HELLO:
                return getGson().fromJson(array.get(2), HelloMessage.class);
            case CALL:
                return getGson().fromJson(json, CallMessage.class);
            case RESULT:
                return getGson().fromJson(json, CallResultMessage.class);
            case ERROR:
                return getGson().fromJson(json, CallErrorMessage.class);
            case SUBSCRIBE:
                return getGson().fromJson(json, SubscribeMessage.class);
            case UNSUBSCRIBE:
                return getGson().fromJson(json, UnsubscribeMessage.class);
            case PUBLISH:
                return getGson().fromJson(json, PublishMessage.class);
            case EVENT:
                return getGson().fromJson(json, EventMessage.class);
         /*   case REGISTER:
                return getGson().fromJson(json, RegisterMessage.class);
            case REGISTERED:
                return getGson().fromJson(json, RegisteredMessage.class);
            case UNREGISTER:
                return getGson().fromJson(json, RegisterMessage.class);
            case UNREGISTERED:
                return getGson().fromJson(json, RegisteredMessage.class);*/

        }
        LogManager.getLogger(MessageMapper.class).error("MessageMapper.fromJson: Unknown type in: " + json);
        return null;
    }

    public static void fromJsonCaughtException(Exception e, String json) throws JsonParseException {
        String text = "Failed to parse: \"" + json + "\"\nException: " + e;
        System.err.println("fromJsonCaughtException: " + text);
    }

    public static String toJson(Message message) {
        return getGson().toJson(message);
    }

    static Gson getGson() {
        if (gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(WelcomeMessage.class, new WelcomeMessage.Serializer());
            builder.registerTypeAdapter(WelcomeMessage.class, new WelcomeMessage.Deserializer());
            builder.registerTypeAdapter(CallMessage.class, new CallMessage.Serializer());
            builder.registerTypeAdapter(CallMessage.class, new CallMessage.Deserializer());
            builder.registerTypeAdapter(CallResultMessage.class, new CallResultMessage.Serializer());
            builder.registerTypeAdapter(CallResultMessage.class, new CallResultMessage.Deserializer());
            builder.registerTypeAdapter(CallErrorMessage.class, new CallErrorMessage.Serializer());
            builder.registerTypeAdapter(CallErrorMessage.class, new CallErrorMessage.Deserializer());
            builder.registerTypeAdapter(SubscribeMessage.class, new SubscribeMessage.Serializer());
            builder.registerTypeAdapter(SubscribeMessage.class, new SubscribeMessage.Deserializer());
            builder.registerTypeAdapter(PublishMessage.class, new PublishMessage.Serializer());
            builder.registerTypeAdapter(PublishMessage.class, new PublishMessage.Deserializer());
            builder.registerTypeAdapter(EventMessage.class, new EventMessage.Serializer());
            builder.registerTypeAdapter(EventMessage.class, new EventMessage.Deserializer());
            builder.registerTypeAdapter(UnsubscribeMessage.class, new UnsubscribeMessage.Serializer());
            builder.registerTypeAdapter(UnsubscribeMessage.class, new UnsubscribeMessage.Deserializer());
            gson = builder.create();
        }
        return gson;
    }

}
