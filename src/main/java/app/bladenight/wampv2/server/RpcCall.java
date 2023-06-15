package app.bladenight.wampv2.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import app.bladenight.wampv2.server.messages.CallErrorMessage;
import app.bladenight.wampv2.server.messages.CallMessage;
import app.bladenight.wampv2.server.messages.CallResultMessage;
import app.bladenight.wampv2.server.messages.MessageMapper;
import app.bladenight.wampv2.server.payload.GsonPayload;
import jdk.nashorn.internal.codegen.CompilerConstants;

/***
 * Represents a single RPC call instance with its input, and the output or result after execution.
 *
 */
public class RpcCall {
	public RpcCall(String sessionId, CallMessage msg) {
		callMessage = msg;
		this.sessionId = sessionId;
	}

	public CallMessage getCallMessage() {
		return  callMessage;
	}

	public <InputType> InputType getInput(Class<InputType> inputType)  {
		return callMessage.getPayload(inputType);
	}

	public void setOutput(JsonElement jsonRoot)  {
		outputJsonElement = jsonRoot;
	}

	public <OutputType> void setOutput(OutputType outputValue, Class<OutputType> outputType)  {
		outputJsonElement = new Gson().toJsonTree(outputValue, outputType);
	}

	public void setError(String errorUri, String errorDesc) {
		hasFailed = true;
		this.errorUri = errorUri;
		this.errorDesc = errorDesc;
	}

	public void setError(String errorUri, String errorDesc, Object errorDetails) {
		setError(errorUri, errorDesc);
		Gson gson = new Gson();
		this.errorDetails = gson.toJsonTree(errorDetails);
	}

	public boolean hasFailed() {
		return hasFailed;
	}

	///Returns sessionId
	public String getOriginatingSession() {
		return sessionId;
	}

	public String getCallResultAsJson() {
		if ( ! hasFailed ) {
			CallResultMessage callResultMessage = new CallResultMessage();
			callResultMessage.callId = callMessage.callId;
			callResultMessage.payload = new GsonPayload(outputJsonElement);
			return MessageMapper.toJson(callResultMessage);
		} else {
			//   [ERROR, CALL, CALL.Request|id, Details|dict, Error|uri]
			CallErrorMessage callErrorMessage = new CallErrorMessage(callMessage.callId, errorUri, errorDesc, errorDetails);
			return MessageMapper.toJson(callErrorMessage);
		}

	}

	protected CallMessage callMessage;

	protected String inputJsonText;
	protected JsonElement outputJsonElement;

	protected boolean hasFailed = false;

	protected String errorUri;
	protected String errorDesc;
	protected JsonElement errorDetails;
	protected String sessionId;

}
