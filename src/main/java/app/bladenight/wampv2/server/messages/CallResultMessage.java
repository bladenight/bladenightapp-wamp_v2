package app.bladenight.wampv2.server.messages;

import app.bladenight.wampv2.server.payload.GsonPayload;
import com.google.gson.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Type;


public class CallResultMessage extends Message {

	public String callId;
	public GsonPayload payload;

	public CallResultMessage() {
		super(MessageType.RESULT);
		init();
	}

	public CallResultMessage(String callId) {
		super(MessageType.RESULT);
		this.callId = callId;
		init();
	}

	private void init() {
		this.payload = new GsonPayload();
	}

	public static class Serializer implements JsonSerializer<CallResultMessage> {
		@Override
		public JsonElement serialize(CallResultMessage msg, Type arg1,
				JsonSerializationContext context) {
			JsonArray array = new JsonArray();
			array.add(context.serialize(msg.getType().getCode()));
			array.add(context.serialize(msg.callId));
			array.add(msg.payload.getGsonElement());
			return array;
		}
	}

	public static class Deserializer implements JsonDeserializer<CallResultMessage> {
		@Override
		public CallResultMessage deserialize(JsonElement element, Type arg1,
				JsonDeserializationContext context) throws JsonParseException {

			JsonArray array = element.getAsJsonArray();
			
			if ( MessageType.fromInteger(array.get(0).getAsInt()) != MessageType.RESULT)
				return null;
			
			CallResultMessage msg = new CallResultMessage();
			msg.callId = array.get(1).getAsString();
			msg.payload = new GsonPayload(array.get(2));
			return msg;
		}
	}

	public <PayloadType> PayloadType getPayload(Class<PayloadType> type) {
		return this.payload.get(type);
	}

	public void setPayload(Object payload) {
		this.payload.setFromObject(payload);
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
