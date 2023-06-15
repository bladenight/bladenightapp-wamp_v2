package app.bladenight.wampv2.server;

import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.MessageMapper;
import app.bladenight.wampv2.server.messages.WelcomeMessage;
import app.bladenight.wampv2.testutils.ErrorChannel;
import app.bladenight.wampv2.testutils.ProtocollingChannel;
import app.bladenight.wampv2.testutils.ServerClient;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.*;

public class ServerTest {

	@Test
	public void serverMustWelcomeClient() throws IOException {
		String serverIdent = UUID.randomUUID().toString();

		WampBnServerImpl server = new WampBnServerImpl(serverIdent);
		ProtocollingChannel channel = new ProtocollingChannel(); 
		assertEquals(0, channel.handledMessages.size());
		Session session = server.registerSession(new ServerClient(server).getTestSession(),channel);
		assertEquals(1, channel.handledMessages.size());
		WelcomeMessage welcomeMessage = (WelcomeMessage) MessageMapper.fromJson(channel.handledMessages.get(0));

		assertTrue(session.isOpen());

		assertTrue("Welcome message from the server must be valid", welcomeMessage.isValid());
		assertEquals(serverIdent, welcomeMessage.serverIdent);
		assertTrue("Session id must be set", welcomeMessage.sessionId>=0);
		}

	@Test(expected=BadArgumentException.class)
	public void invalidInputMessage() throws IOException, BadArgumentException {
		String serverIdent = UUID.randomUUID().toString();
		WampBnServerImpl server = new WampBnServerImpl(serverIdent);
		ProtocollingChannel channel = new ProtocollingChannel(); 
		Session session = server.registerSession(new ServerClient(server).getTestSession(),channel);
		server.handleIncomingString(session, "[]");
	}

	@Test(expected=BadArgumentException.class)
	public void invalidSession() throws IOException, BadArgumentException {
		String serverIdent = UUID.randomUUID().toString();
		WampBnServerImpl server = new WampBnServerImpl(serverIdent);
		ProtocollingChannel channel = new ProtocollingChannel(); 
		Session session = server.registerSession(new ServerClient(server).getTestSession(),channel);
		server.handleIncomingString(session, "[7, \"http://example.com/simple\", \"Hello, world!\"]");
	}

	@Test
	public void connectionInterrupted()  {
		String serverIdent = UUID.randomUUID().toString();
		WampBnServerImpl server = new WampBnServerImpl(serverIdent);
		ErrorChannel channel = new ErrorChannel(); 
		Session session = server.registerSession(new ServerClient(server).getTestSession(),channel);
		assertFalse(session.isOpen());
	}
}
