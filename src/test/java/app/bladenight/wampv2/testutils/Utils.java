package app.bladenight.wampv2.testutils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
	static public String q(String s) {
		return "\"" + s + "\"";
	}
	
	static public String rndStr() {
		return UUID.randomUUID().toString();
	}

	static public Long rndLong() {
		return ThreadLocalRandom.current().nextLong(10000000L);
	}

}
