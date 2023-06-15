package app.bladenight.wampv2.client;

import com.google.gson.JsonElement;
import app.bladenight.wampv2.server.payload.GsonPayload;

public abstract class EventReceiver {

	protected abstract void onReceive();
	
	public <PayloadType> PayloadType getPayload(Class<PayloadType> payloadType) {
		return payload.get(payloadType); 
	}
	
	void setPayloadElement(JsonElement jsonElement) {
		payload.setFromGson(jsonElement);
	}
	
	private GsonPayload payload = new GsonPayload();
}
