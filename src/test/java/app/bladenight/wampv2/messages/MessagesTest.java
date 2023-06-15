package app.bladenight.wampv2.messages;

import app.bladenight.wampv2.server.messages.MessageMapper;
import app.bladenight.wampv2.server.messages.WelcomeMessage;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNull;


public class MessagesTest {

	static public <Type> void verifyFailingParsing(Class<Type> type, String[] invalidJsonTexts) {
		for (String invalidJsonText : Arrays.asList(invalidJsonTexts) ) {
			@SuppressWarnings("unchecked")
			Type msg = (Type) MessageMapper.fromJson(invalidJsonText);
			assertNull(msg);
		}
	}


	@Test
	public void unserializeInvalidMessages() {
		verifyFailingParsing(WelcomeMessage.class, new String[] {
			"",
			"1",
			"[-1]",
			"[9]"
		});
	}

}
