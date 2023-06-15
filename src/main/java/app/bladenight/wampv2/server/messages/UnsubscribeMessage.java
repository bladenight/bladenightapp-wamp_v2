package app.bladenight.wampv2.server.messages;

import com.google.gson.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Type;

public class UnsubscribeMessage extends Message {
	static final MessageType concreteMessageType = MessageType.UNSUBSCRIBE;
	
	public String topicUri;
	
	public UnsubscribeMessage() {
		super(concreteMessageType);
	}

	public UnsubscribeMessage(String topicUri) {
		super(concreteMessageType);
		this.topicUri = topicUri;
	}
	public static class Serializer implements JsonSerializer<UnsubscribeMessage> {
		@Override
		public JsonElement serialize(UnsubscribeMessage msg, Type arg1,
				JsonSerializationContext context) {
			JsonArray array = new JsonArray();
			array.add(context.serialize(msg.getType().getCode()));
			array.add(context.serialize(msg.topicUri));
			return array;
		}
	}

	public static class Deserializer implements JsonDeserializer<UnsubscribeMessage> {
		@Override
		public UnsubscribeMessage deserialize(JsonElement element, Type arg1,
				JsonDeserializationContext context) throws JsonParseException {

			JsonArray array = element.getAsJsonArray();
			
			if ( MessageType.fromInteger(array.get(0).getAsInt()) != concreteMessageType)
				return null;
			
			UnsubscribeMessage msg = new UnsubscribeMessage();
			msg.topicUri = array.get(1).getAsString();
			return msg;
		}
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
