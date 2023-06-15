package app.bladenight.wampv2.server.common;

import java.io.IOException;

public interface Channel {
	void handle(String message) throws IOException;
}
