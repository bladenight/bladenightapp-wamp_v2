/*
 * Copyright (C) 2016 Neo Visionaries Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

//https://github.com/TakahikoKawasaki/nv-websocket-client/blob/master/src/main/java/com/neovisionaries/ws/client/HandshakeReader.java

package app.bladenight.wampv2.server.common;

import app.bladenight.wampv2.server.exceptions.HandShakeException;
import app.bladenight.wampv2.server.utilities.GeneralUtilities;
import org.eclipse.jetty.websocket.api.WebSocketException;

import java.security.MessageDigest;
import java.util.*;


public class WampInitialisation {
    private static final String MAGIC_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public String GetWebSecKeyResult(String handShakeKey) {

        String input = handShakeKey + MAGIC_KEY;
        //Test-->  String input = "dGhlIHNhbXBsZSBub25jZQ==" + MAGIC_KEY;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            byte[] digest = md.digest(GeneralUtilities.getBytesUTF8(input));

            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            return "";
        }
    }

    public Map<String, List<String>> validateAccept(Map<String, List<String>> headers) throws WebSocketException {
        Map<String, List<String>> resultHeader = new HashMap<>();
        List<String> values = headers.get("Sec-WebSocket-Key");

        if (values == null) {
            // The opening handshake response does not contain 'Sec-WebSocket-Accept' header.
            throw new HandShakeException( //WS_ERR_NO_HANDSHAKE
                    "The opening handshake response does not contain 'Sec-WebSocket-Accept' header.");
        }

        // The actual value of Sec-WebSocket-Accept.
        String actual = "Iv8io/9s+lYFgZWcXczP8Q==";//values.get(0);

        String input = values + MAGIC_KEY;

        // Expected value of Sec-WebSocket-Accept
        String expected;

        try {
            // Message digest for SHA-1.
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // Compute the digest value.
            byte[] digest = md.digest(GeneralUtilities.getBytesUTF8(input));

            // Base64.
            expected = Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            // This never happens.
            return headers;
        }

        /*
        GET /chat
Host: javascript.info
Origin: https://javascript.info
Connection: Upgrade
Upgrade: websocket
Sec-WebSocket-Key: Iv8io/9s+lYFgZWcXczP8Q==
Sec-WebSocket-Version: 13
         */

        /*
        101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: hsBlbuDTkk24srzEOTBUlZAlC2g=*/



        if (expected.equals(actual) == false) {
            // The value of 'Sec-WebSocket-Accept' header is different from the expected one.
            throw new HandShakeException(//UNEXPECTED_SEC_WEBSOCKET_ACCEPT_HEADER,
                    "The value of 'Sec-WebSocket-Accept' header is different from the expected one."
            );
        }
return null;
        // OK. The value of Sec-WebSocket-Accept is the same as the expected one.
    }
}
