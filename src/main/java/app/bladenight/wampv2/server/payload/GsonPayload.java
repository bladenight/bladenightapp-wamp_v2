package app.bladenight.wampv2.server.payload;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Simple wrapper for a JsonElement from Gson.
 * It reduces partially the dependency of the client code towards Gson.
 *  
 * @author ocroquette
 *
 */
public class GsonPayload {

	public GsonPayload() {
	}

	public GsonPayload(Object object) {
		setFromObject(object);
	}

	public GsonPayload(JsonElement jsonElement) {
		this.jsonElement = jsonElement;
	}

	public void setFromGson(JsonElement jsonElement) {
		this.jsonElement = jsonElement;
	}

	public void setFromObject(Object payload) {
		Gson gson = new Gson();
		jsonElement = gson.toJsonTree(payload);
	}

	public <PayloadType> PayloadType get(Class<PayloadType> type) {
		Gson gson = new Gson();
		return gson.fromJson(jsonElement, type);
	}

	public JsonElement getGsonElement() {
		return jsonElement;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString(){
		if (jsonElement == null){
			return "GSONPayLoad - jsonElement = null";
		}
		return jsonElement.toString();}

	private JsonElement jsonElement;

}
