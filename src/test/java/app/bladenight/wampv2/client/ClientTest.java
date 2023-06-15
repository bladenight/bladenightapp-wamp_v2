package app.bladenight.wampv2.client;

import app.bladenight.wampv2.server.common.Channel;
import app.bladenight.wampv2.server.messages.*;
import app.bladenight.wampv2.testutils.ProtocollingChannel;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientTest {

	private final class ProtocollingRpcResultReceiver extends RpcResultReceiver {
		public boolean isSuccess = false;
		public boolean isError = false;

		@Override
		public void onSuccess() {
			isSuccess = true;
		}

		@Override
		public void onError() {
			isError = true;
		}
	}

	private final class ProtocollingEventReceiver extends EventReceiver {
		public int eventCount = 0;
		public String lastPayload;
		
		@Override
		public void onReceive() {
			eventCount++;
			lastPayload = getPayload(String.class);
		}
	}


	static class MyWelcomeListener implements WelcomeListener {
		public int count = 0;
		@Override
		public void onWelcome() {
			count++;
		}
	};

	@Test
	public void handleWelcomeMessage() {
		ProtocollingChannel channel = new ProtocollingChannel();
		WampClient client = new WampClient(channel);
		String sessionId = UUID.randomUUID().toString();
		String serverIndent = UUID.randomUUID().toString();
		assertEquals(false, client.hasBeenWelcomed());
		
		MyWelcomeListener welcomeListener = new MyWelcomeListener();
		client.setWelcomeListener(welcomeListener);
		client.handleIncomingMessage("[0, \"" + sessionId + "\" , 1, \"" + serverIndent + "\"]");
		assertEquals(serverIndent, client.getServerIdent());
		assertEquals(serverIndent, client.getServerIdent());
		assertEquals(sessionId, client.getSessionId());
		assertEquals(1, welcomeListener.count);
	}

	@Test(expected=IllegalStateException.class)
	public void notWelcomed() {
		ProtocollingChannel channel = new ProtocollingChannel();
		WampClient client = new WampClient(channel);
		assertEquals(false, client.hasBeenWelcomed());
		client.handleIncomingMessage("[3, \"Test\" , null]");
	}

	@Test
	public void handleUnexpectedCallResultMessage() {
		String procedureId = "http://host/handleUnexpectedCallResultMessage";
		ProtocollingChannel channel = new ProtocollingChannel();
		WampClient client = newClient(channel);
		client.handleIncomingMessage("[3, \"" + procedureId + "\" , null]");
		// TODO check appropriate logging
	}

	@Test
	public void handleRpcCycle() throws IOException {
		String procedureId = "http://host/handleRpcCycle";
		ProtocollingChannel channel = new ProtocollingChannel();
		WampClient client = newClient(channel);
		ProtocollingRpcResultReceiver rpcResultReciever = new ProtocollingRpcResultReceiver();
		assertEquals(0, channel.handledMessages.size());
		client.call(procedureId, rpcResultReciever);
		assertEquals(1, channel.handledMessages.size());
		assertTrue(channel.last().matches("^\\[2,\"[^\"]+\",\"http://host/handleRpcCycle\"\\]$"));
		// [2,"1180c948-c071-4435-9acc-6c0832866f33","http://host/handleRpcCycle"]
		// The callId is generated by the Client class and sent to the remote server.
		// For testing purpose, we have to get it back from the output channel.
		String callId = getCallId(channel.handledMessages.get(0));
		client.handleIncomingMessage("[3, \"" + callId + "\" , null]");
		assertEquals("RPC call shall be successful", true, rpcResultReciever.isSuccess);
		assertEquals("RPC error handler shall not have been called", false, rpcResultReciever.isError);
		// TODO check appropriate logging
	}

	@Test
	public void handleRpcError() throws IOException {
		String procedureId = "http://host/handleRpcError";
		ProtocollingChannel channel = new ProtocollingChannel();
		WampClient client = newClient(channel);
		ProtocollingRpcResultReceiver rpcResultReciever = new ProtocollingRpcResultReceiver();
		client.call(procedureId, rpcResultReciever);
		String callId = getCallId(channel.handledMessages.get(0));
		client.handleIncomingMessage("[4, \"" + callId + "\" , \"http://error\", \"Some error occured\"]");
		assertEquals("RPC call shall be successful", false, rpcResultReciever.isSuccess);
		assertEquals("RPC error handler shall have been called", true, rpcResultReciever.isError);
		// TODO check appropriate logging
	}

	@Test
	public void handleRpcCycleWithPayloads() throws IOException {
		String procedureId = "http://host/handleRpcCycle";
		ProtocollingChannel channel = new ProtocollingChannel();
		WampClient client = newClient(channel);
		ProtocollingRpcResultReceiver rpcResultReciever = new ProtocollingRpcResultReceiver();
		String payload = UUID.randomUUID().toString();
		client.call(procedureId, rpcResultReciever, payload);
		assertEquals(1, channel.handledMessages.size());
		assertTrue(channel.last().matches("^\\[2,\"[^\"]+\",\"http://host/handleRpcCycle\",\"" + payload + "\"\\]$"));
		String callId = getCallId(channel.handledMessages.get(0));
		client.handleIncomingMessage("[3, \"" + callId + "\" , \""+payload+"\"]");
		assertEquals("RPC call shall be successful", true, rpcResultReciever.isSuccess);
		assertEquals("RPC call shall be successful", false, rpcResultReciever.isError);
		assertEquals("RPC resulting payload shall be set", payload, rpcResultReciever.getPayload(String.class));
	}

	@Test
	public void handleSubscribe() throws IOException {
		String topicId = "http://host/handleSubscribe";
		ProtocollingChannel channel = new ProtocollingChannel();
		WampClient client = newClient(channel);
		ProtocollingEventReceiver eventReceiver = new ProtocollingEventReceiver();
		client.subscribe(topicId, eventReceiver);
		assertEquals(0, eventReceiver.eventCount);

		EventMessage eventMessage = new EventMessage(topicId);

		client.handleIncomingEventMessage(eventMessage);
		assertEquals(1, eventReceiver.eventCount);
		client.handleIncomingEventMessage(eventMessage);
		assertEquals(2, eventReceiver.eventCount);

		client.unsubscribe(topicId);

		client.handleIncomingEventMessage(eventMessage);
		assertEquals(2, eventReceiver.eventCount);
}

	@Test
	public void handleEvents() throws IOException {
		String topicId = "http://host/handleEvents";
		ProtocollingChannel channel = new ProtocollingChannel();
		WampClient client = newClient(channel);
		String payload = UUID.randomUUID().toString();
		ProtocollingEventReceiver eventReceiver = new ProtocollingEventReceiver();

		String jsonEventMessage = "[8,\""+topicId+"\",\"" + payload + "\"]";
		String jsonEventMessageOtherTopic = "[8, \"Not "+topicId+"\", null]";
		
		// We didn't subscribe yet, so incoming events shall not trigger the receiver:
		assertEquals(0, eventReceiver.eventCount);
		client.handleIncomingMessage(jsonEventMessage);
		assertEquals(0, eventReceiver.eventCount);

		assertEquals(0, channel.handledMessages.size());
		client.subscribe(topicId, eventReceiver);
		assertEquals(1, channel.handledMessages.size());
		
		// From this point on we have subscribed to topicId
		SubscribeMessage subscribeMessage = (SubscribeMessage) MessageMapper.fromJson(channel.handledMessages.get(0));
		assertEquals(topicId, subscribeMessage.topicUri);

		assertEquals(0, eventReceiver.eventCount);
		client.handleIncomingMessage(jsonEventMessage);
		assertEquals(1, eventReceiver.eventCount);
		assertEquals(payload, eventReceiver.lastPayload);
		client.handleIncomingMessage(jsonEventMessage);
		assertEquals(2, eventReceiver.eventCount);
		client.handleIncomingMessage(jsonEventMessageOtherTopic);
		assertEquals(2, eventReceiver.eventCount);

		// From this point on we are not subscribed to anything anymore
		client.unsubscribe(topicId);
		assertEquals(2, channel.handledMessages.size());
		UnsubscribeMessage unsubscribeMessage = (UnsubscribeMessage)MessageMapper.fromJson(channel.handledMessages.get(1));
		assertEquals(topicId, unsubscribeMessage.topicUri);

		assertEquals(2, eventReceiver.eventCount);
		client.handleIncomingMessage(jsonEventMessage);
		assertEquals(2, eventReceiver.eventCount);
	}
	
	@Test
	public void handlePublish() throws IOException {
		String topicId = "http://host/handleEvents";
		ProtocollingChannel channel = new ProtocollingChannel();
		WampClient client = newClient(channel);
		String payload = UUID.randomUUID().toString();

		assertEquals(0, channel.handledMessages.size());

		String expectedOutgoingMessage = "[7,\""+topicId+"\",\"" + payload + "\"]";
		client.publish(topicId, payload);
		
		System.out.println(expectedOutgoingMessage);
		System.out.println(channel.handledMessages.get(0));
		assertEquals(1, channel.handledMessages.size());
		assertEquals(expectedOutgoingMessage, channel.handledMessages.get(0));

	}

	public WampClient newClient(Channel channel) {
		WampClient client = new WampClient(channel);
		String sessionId = UUID.randomUUID().toString();
		String serverIndent = UUID.randomUUID().toString();
		assertEquals(false, client.hasBeenWelcomed());
		client.handleIncomingMessage("[0, \"" + sessionId + "\" , 1, \"" + serverIndent + "\"]");
		return client;
	}

	public String getCallId(String jsonCallMessage) {
		CallMessage callMessage = (CallMessage)MessageMapper.fromJson(jsonCallMessage);
		return callMessage.callId;
	}
}
