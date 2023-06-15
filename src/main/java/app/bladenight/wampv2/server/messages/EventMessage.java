package app.bladenight.wampv2.server.messages;

import com.google.gson.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Type;

public class EventMessage extends Message {
	static final MessageType concreteMessageType = MessageType.EVENT;

	public String topicUri;
	public JsonElement payload;

	public EventMessage() {
		super(concreteMessageType);
	}

	public EventMessage(String topicUri) {
		super(concreteMessageType);
		this.topicUri = topicUri;
	}

	public static class Serializer implements JsonSerializer<EventMessage> {
		@Override
		public JsonElement serialize(EventMessage msg, Type arg1,
				JsonSerializationContext context) {
			JsonArray array = new JsonArray();
			array.add(context.serialize(msg.getType().getCode()));
			array.add(context.serialize(msg.topicUri));
			array.add(msg.payload);
			return array;
		}
	}

	public static class Deserializer implements JsonDeserializer<EventMessage> {
		@Override
		public EventMessage deserialize(JsonElement element, Type arg1,
				JsonDeserializationContext context) throws JsonParseException {

			JsonArray array = element.getAsJsonArray();

			if ( MessageType.fromInteger(array.get(0).getAsInt()) != concreteMessageType)
				return null;

			EventMessage msg = new EventMessage();
			msg.topicUri = array.get(1).getAsString();
			msg.payload = array.get(2);
			return msg;
		}
	}

	public JsonElement getPayloadAsElement() {
		return this.payload;
	}

	public <PayloadType> PayloadType getPayload(Class<PayloadType> type) {
		Gson gson = new Gson();
		return gson.fromJson(payload, type);
	}

	public void setPayload(Object payload) {
		Gson gson = new Gson();
		this.payload = gson.toJsonTree(payload);
	}

	public void setPayloadJsonElement(JsonElement payload) {
		this.payload = payload;
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
