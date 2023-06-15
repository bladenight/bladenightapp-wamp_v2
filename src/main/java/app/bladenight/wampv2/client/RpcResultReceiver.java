package app.bladenight.wampv2.client;

import app.bladenight.wampv2.server.messages.CallErrorMessage;
import app.bladenight.wampv2.server.messages.CallResultMessage;

public abstract class RpcResultReceiver {

	abstract public void onSuccess();

	abstract public void onError();

	public <PayloadType> PayloadType getPayload(Class<PayloadType> payloadType) {
		return callResultMessage.getPayload(payloadType);  
	}
	
	void setCallResultMessage(CallResultMessage message) {
		callResultMessage = message;
	}

	public void setCallErrorMessage(CallErrorMessage message) {
		callErrorMessage = message;
	}

	protected CallResultMessage callResultMessage;
	protected CallErrorMessage callErrorMessage;

}
