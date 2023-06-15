package app.bladenight.wampv2.testutils;

import app.bladenight.wampv2.server.common.Channel;

import java.io.IOException;

public class ErrorChannel implements Channel {
	@Override
	public void handle(String message) throws IOException {
		throw new IOException("Forced exception from ErrorChannel");
	}
	
}