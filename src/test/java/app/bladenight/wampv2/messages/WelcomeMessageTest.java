package app.bladenight.wampv2.messages;

import app.bladenight.wampv2.server.messages.MessageMapper;
import app.bladenight.wampv2.server.messages.WelcomeMessage;
import org.junit.Test;

import java.util.Optional;

import static app.bladenight.wampv2.testutils.Utils.*;
import static org.junit.Assert.*;

public class WelcomeMessageTest implements TestsNotToForget {
	@Override
	public void constructMessage() {
		long sessionId = rndLong();
		String serverIdent = rndStr(); 
		WelcomeMessage welcomeMessage = new WelcomeMessage(sessionId, serverIdent);
		assertEquals(sessionId, welcomeMessage.sessionId);
		assertEquals(serverIdent, welcomeMessage.serverIdent);
	}

	@Override
	public void testEquals() {
		WelcomeMessage msg1 = new WelcomeMessage(1,"2");
		WelcomeMessage msg1bis = new WelcomeMessage(1,"2");
		WelcomeMessage msg2 = new WelcomeMessage(1,"3");
		WelcomeMessage msg3 = new WelcomeMessage(1,"2");
		assertTrue(msg1.equals(msg1bis));
		assertFalse(msg1.equals(msg2));
		assertFalse(msg1.equals(msg3));
	}

	@Test
	public void validWelcomeMessage() {
		Long sessionId = rndLong();
		String serverIdent = rndStr(); 

		WelcomeMessage welcomeMessage = new WelcomeMessage(sessionId, serverIdent);
		String json = MessageMapper.toJson(welcomeMessage);
		assertEquals("[0," + sessionId + ",1,"+q(serverIdent)+"]", json);

		WelcomeMessage clone = (WelcomeMessage) MessageMapper.fromJson(json);
		assertTrue("Welcome message from a valid string must be valid", welcomeMessage.isValid());
		assertEquals(13,clone.protocolVersion);
		assertEquals(serverIdent, clone.serverIdent);
		assertEquals(Optional.of(sessionId), clone.sessionId);
	}

	@Test
	public void unserializeInvalidMessages() {
		MessagesTest.verifyFailingParsing(WelcomeMessage.class, new String[] {
			"?",
			"[0]",
			"[0,\"\"]",
			"[0,\"\",1]",
		});
	}

}
