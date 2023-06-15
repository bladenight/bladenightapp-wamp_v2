package app.bladenight.wampv2.server;

import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.*;
import app.bladenight.wampv2.testutils.ProtocollingChannel;
import app.bladenight.wampv2.testutils.ServerClient;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ServerEventsTest {
	final String serverIdent = "SERVER IDENT";

	@Test
	public void subscribeAndPublish() throws IOException, BadArgumentException {
		String payload = "Publish payload";
		
		WampBnServerImpl server = new WampBnServerImpl(serverIdent);
		String topicId = "http://host/topicId";

		ProtocollingChannel channel1 = new ProtocollingChannel();
		Session session1 = server.registerSession(new ServerClient(server).getTestSession(),channel1);
		SubscribeMessage subscribeMessage = new SubscribeMessage(topicId);
		server.handleIncomingMessage(session1, subscribeMessage);

		ProtocollingChannel channel2 = new ProtocollingChannel();
		Session session2 = server.registerSession(new ServerClient(server).getTestSession(),channel2);
		server.handleIncomingMessage(session2, subscribeMessage);


		PublishMessage publishMessage = new PublishMessage(topicId);
		publishMessage.setPayload(payload);

		server.handleIncomingMessage(session1, publishMessage);

		assertEquals(2, channel1.handledMessages.size());
		assertEquals(2, channel2.handledMessages.size());

		Message message = MessageMapper.fromJson(channel1.last());
		EventMessage eventMessage = (EventMessage) message;
		assertEquals(payload, eventMessage.getPayload(String.class));
		
		publishMessage.excludeMe = true;
		server.handleIncomingMessage(session1, publishMessage);
		assertEquals(2, channel1.handledMessages.size());
		assertEquals(3, channel2.handledMessages.size());
	}

	@Test
	public void unsubscribe() throws IOException, BadArgumentException {
		String payload = "Publish payload";
		
		WampBnServerImpl server = new WampBnServerImpl(serverIdent);
		String topicId = "http://host/topicId";

		ProtocollingChannel channel1 = new ProtocollingChannel();
		Session session1 = server.registerSession(new ServerClient(server).getTestSession(),channel1);
		SubscribeMessage subscribeMessage = new SubscribeMessage(topicId);
		server.handleIncomingMessage(session1, subscribeMessage);

		PublishMessage publishMessage = new PublishMessage(topicId);
		publishMessage.setPayload(payload);

		server.handleIncomingMessage(session1, publishMessage);
		assertEquals(2, channel1.handledMessages.size());
		assertEquals("[8,\"http://host/topicId\",\"Publish payload\"]", channel1.last());

		UnsubscribeMessage unsubscribeMessage = new UnsubscribeMessage(topicId);
		server.handleIncomingMessage(session1, unsubscribeMessage);

		server.handleIncomingMessage(session1, publishMessage);
		assertEquals(2, channel1.handledMessages.size());
	}
}
