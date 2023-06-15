package app.bladenight.wampv2.server;

import app.bladenight.wampv2.testutils.SessionFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SessionTest {

	@Test
	public void subsequentIdsShallBeDifferent() {
		WampBnServerImpl server = new WampBnServerImpl("SessionTestserver");

		SessionFactory f1 = new SessionFactory();

		Session s1 = f1.getNew(server,null);
		Session s2 = f1.getNew(server,null);

		assertFalse(server.getExternalId(s1).equals(server.getExternalId(s2)));
	}

	@Test
	public void idsFromDifferentFactoryShallBeDifferent() {
		///different servers
		WampBnServerImpl server = new WampBnServerImpl("SessionTestserver");
		WampBnServerImpl server2 = new WampBnServerImpl("SessionTestserver");

		SessionFactory f1 = new SessionFactory();
		SessionFactory f2 = new SessionFactory();

		Session s1 = f1.getNew(server, null);
		Session s2 = f2.getNew(server2, null);

		assertFalse(server.getExternalId(s1).equals(server.getExternalId(s2)));
	}
	}
