package app.bladenight.wampv2.server;

import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.CallMessage;
import app.bladenight.wampv2.server.messages.MessageMapper;
import app.bladenight.wampv2.testutils.ProtocollingChannel;
import app.bladenight.wampv2.testutils.ServerClient;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TextFrameEavesdropperTest {
	
	public static class Frame {
		Frame(String sessionId, String frame) {
			this.sessionId = sessionId;
			this.frame = frame;
		}
		public String sessionId;
		public String frame;
	}
	
	@Test
	public void serverMustWelcomeClient() throws IOException, BadArgumentException {
		WampBnServerImpl server = new WampBnServerImpl(UUID.randomUUID().toString());
		ProtocollingChannel channel = new ProtocollingChannel(); 
		Session session = server.registerSession(new ServerClient(server).getTestSession(),channel);
		final String sessionId = String.valueOf(server.getExternalId(session));
		
		final List<Frame> frames = new ArrayList<>();
		
		TextFrameEavesdropper incomingEavesdropper = (id, frame) -> frames.add(new Frame(id, frame));

		server.addIncomingFramesEavesdropper(incomingEavesdropper);

		CallMessage callMessage = new CallMessage("ProcedureId", "Payload");
		server.handleIncomingMessage(session, callMessage);

		assertEquals(1, frames.size());
		assertEquals(sessionId, frames.get(0).sessionId);
		assertEquals(MessageMapper.toJson(callMessage), frames.get(0).frame);
	}


}
