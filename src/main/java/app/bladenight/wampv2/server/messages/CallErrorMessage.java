package app.bladenight.wampv2.server.messages;

import com.google.gson.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Type;


public class CallErrorMessage extends Message {
    // [ TYPE_ID_CALLERROR , callID , errorURI , errorDesc , errorDetails ]
    public String callId;
    public String errorUri;
    public String errorDesc;
    public JsonElement errorDetails;

    private CallErrorMessage() {
        super(MessageType.ERROR);
    }

    public CallErrorMessage(String callId, String errorUri, String errorDesc) {
        super(MessageType.ERROR);
        this.callId = callId;
        this.errorUri = errorUri;
        this.errorDesc = errorDesc;
    }

    public CallErrorMessage(String callId, String errorUri, String errorDesc, JsonElement errorDetails) {
        super(MessageType.ERROR);
        this.callId = callId;
        this.errorUri = errorUri;
        this.errorDesc = errorDesc;
        this.errorDetails = errorDetails;
    }

    public static class Serializer implements JsonSerializer<CallErrorMessage> {
        @Override
        public JsonElement serialize(CallErrorMessage msg, Type arg1,
                                     JsonSerializationContext context) {
            //   [ERROR, CALL, CALL.Request|id, Details|dict, Error|uri]
            //[8, 48, 7814135, {}, "com.myapp.error.object_write_protected",
            //        ["Object is write protected."], {"severity": 3}]
            JsonArray array = new JsonArray();
            array.add(context.serialize(msg.getType().getCode()));
            array.add(context.serialize(MessageType.CALL.getCode()));
            array.add(context.serialize(msg.callId));
            array.add(context.serialize(msg.errorDesc));
            array.add(context.serialize(msg.errorUri));
            if (msg.errorDetails != null)
                array.add(msg.errorDetails);
            return array;
        }
    }

    public static class Deserializer implements JsonDeserializer<CallErrorMessage> {
        @Override
        public CallErrorMessage deserialize(JsonElement element, Type arg1,
                                            JsonDeserializationContext context) throws JsonParseException {

            JsonArray array = element.getAsJsonArray();

            if (MessageType.fromInteger(array.get(0).getAsInt()) != MessageType.ERROR)
                return null;

            CallErrorMessage msg = new CallErrorMessage();
            msg.callId = array.get(2).getAsString();
            msg.errorUri = array.get(4).getAsString();
            msg.errorDesc = array.get(3).getAsString();
            if (array.size() > 5)
                msg.errorDetails = array.get(5);
            return msg;
        }
    }

    public <ErrorDetailsType> ErrorDetailsType getErrorDetails(Class<ErrorDetailsType> type) {
        if (errorDetails == null)
            throw new NullPointerException("No payload has been set");
        Gson gson = new Gson();
        return gson.fromJson(errorDetails, type);
    }

    public void setErrorDetails(Object errorDetails) {
        Gson gson = new Gson();
        this.errorDetails = gson.toJsonTree(errorDetails);
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getErrorUri() {
        return errorUri;
    }

    public void setErrorUri(String errorUri) {
        this.errorUri = errorUri;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
