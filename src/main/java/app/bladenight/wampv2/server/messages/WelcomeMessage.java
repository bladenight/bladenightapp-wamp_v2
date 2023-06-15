package app.bladenight.wampv2.server.messages;

import com.google.gson.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


public class WelcomeMessage extends Message {
	final static int PROTOCOL_VERSION = 13;

	public long sessionId;
	public int protocolVersion;
	public String serverIdent;
	public Map<String, Map> roles = new HashMap<>();
	public String realm="";

	public WelcomeMessage() {
		super(MessageType.WELCOME);
	}

	public WelcomeMessage(long sessionId, String serverIdent) {
		super(MessageType.WELCOME);
		this.sessionId = sessionId;
		this.protocolVersion = PROTOCOL_VERSION;
		this.serverIdent = serverIdent;
		
	}

	public WelcomeMessage(long sessionId,
						  java.util.Map<java.lang.String,java.util.Map> roles,
						  java.lang.String realm){
		super(MessageType.WELCOME);
		this.sessionId = sessionId;
		this.roles=roles;
		this.realm=realm;
	}
	
	public boolean isValid() {
		return (type != null && type.equals(MessageType.WELCOME)
				&& protocolVersion == 2
				&& sessionId != 0
				&& serverIdent != null && serverIdent.length() > 0
				);
	}

	public static class Serializer implements JsonSerializer<WelcomeMessage> {
		@Override
		public JsonElement serialize(WelcomeMessage msg, Type arg1,
									 JsonSerializationContext context) {
			JsonArray array = new JsonArray();
			array.add(context.serialize(msg.getType().getCode()));
			array.add(context.serialize(msg.sessionId));
			//connectanum contains array at index [2]
			//see connectanum-2.0.3/lib/src/serializer/json/serializer.dart

			Map<String, Object> details = new HashMap<String, Object>();
			details.put("roles",null	);
			details.put("realm",null);

			/*String realm = (String) details.get("realm");
			String authid = (String) details.get("authid");
			String authrole = (String) details.get("authrole");
			String authmethod = (String) details.get("authmethod");*/

			array.add(context.serialize(details));
			return array;
		}
	}

	public static class Deserializer implements JsonDeserializer<WelcomeMessage> {
		@Override
		public WelcomeMessage deserialize(JsonElement element, Type arg1,
				JsonDeserializationContext context) throws JsonParseException {

			JsonArray array = element.getAsJsonArray();
			
			if ( MessageType.fromInteger(array.get(0).getAsInt()) != MessageType.WELCOME)
				return null;
			
			WelcomeMessage msg = new WelcomeMessage();
			msg.sessionId = array.get(1).getAsLong();
			msg.protocolVersion = PROTOCOL_VERSION;
			//msg.serverIdent = array.get(3).getAsString();
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
