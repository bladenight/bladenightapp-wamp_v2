package app.bladenight.wampv2.messages;

import app.bladenight.wampv2.server.messages.CallErrorMessage;
import app.bladenight.wampv2.server.messages.MessageMapper;
import app.bladenight.wampv2.server.messages.MessageType;
import app.bladenight.wampv2.server.messages.SubscribeMessage;
import org.junit.Test;

import static app.bladenight.wampv2.testutils.Utils.q;
import static app.bladenight.wampv2.testutils.Utils.rndStr;
import static org.junit.Assert.*;

public class SubscribeMessageTest implements TestsNotToForget {
	@Test
	public void constructMessage() {
		String topicUri = rndStr(); 
		SubscribeMessage msg = new SubscribeMessage(topicUri);
		assertEquals(topicUri, msg.topicUri);
	}

	public SubscribeMessage randomMessage() {
		return new SubscribeMessage(rndStr());
	}

	@Test
	@Override
	public void testEquals() {
		SubscribeMessage msg1 = new SubscribeMessage("1"); 
		SubscribeMessage msg1bis = new SubscribeMessage("1"); 
		SubscribeMessage msg2 = new SubscribeMessage("2"); 
		assertTrue(msg1.equals(msg1bis));
		assertFalse(msg1.equals(msg2));
	}
	
	@Test
	public void validUnsubscribe() {
		String topicUrl = rndStr(); 
		SubscribeMessage message = new SubscribeMessage(topicUrl);
		String json = MessageMapper.toJson(message);
		assertEquals("[5," + q(topicUrl) + "]", json);

		SubscribeMessage clone = (SubscribeMessage) MessageMapper.fromJson(json);
		assertEquals(MessageType.SUBSCRIBE, clone.getType());
		assertEquals(topicUrl, clone.topicUri);
	}

	@Test
	@Override
	public void unserializeInvalidMessages() {
		MessagesTest.verifyFailingParsing(CallErrorMessage.class, new String[] {
				"[5]",
		});
	}
}
