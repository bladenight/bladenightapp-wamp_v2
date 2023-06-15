package app.bladenight.wampv2.server.messages;

import com.google.gson.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Type;


public class HelloMessage extends Message {
	final static int PROTOCOL_VERSION = 13;

	public String sessionId;
	public int protocolVersion;
	public String serverIdent;

	public HelloMessage() {
		super(MessageType.HELLO);
	}

	public HelloMessage(String sessionId, String serverIdent) {
		super(MessageType.HELLO);
		this.sessionId = sessionId;
		this.protocolVersion = PROTOCOL_VERSION;
		this.serverIdent = serverIdent;
		
	}
	
	public boolean isValid() {
		return (type != null && type.equals(MessageType.HELLO)
				&& protocolVersion == 2
				&& sessionId != null && sessionId.length() > 0
				&& serverIdent != null && serverIdent.length() > 0
				);
	}

	public static class Serializer implements JsonSerializer<HelloMessage> {
		@Override
		public JsonElement serialize(HelloMessage msg, Type arg1,
                                     JsonSerializationContext context) {
			JsonArray array = new JsonArray();
			array.add(context.serialize(msg.getType().getCode()));
			array.add(context.serialize(msg.sessionId));
			array.add(context.serialize(PROTOCOL_VERSION));
			array.add(context.serialize(msg.serverIdent));
			return array;
		}
	}

	public static class Deserializer implements JsonDeserializer<HelloMessage> {
		@Override
		public HelloMessage deserialize(JsonElement element, Type arg1,
                                        JsonDeserializationContext context) throws JsonParseException {

			JsonArray array = element.getAsJsonArray();
			
			if ( MessageType.fromInteger(array.get(0).getAsInt()) != MessageType.WELCOME)
				return null;
			
			HelloMessage msg = new HelloMessage();
			msg.sessionId = array.get(1).getAsString();
			msg.protocolVersion = PROTOCOL_VERSION;
			msg.serverIdent = array.get(3).getAsString();
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
