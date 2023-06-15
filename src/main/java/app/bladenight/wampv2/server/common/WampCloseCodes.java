// Copyright (c) 2010 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses.
// ========================================================================
package app.bladenight.wampv2.server.common;

public class WampCloseCodes {
    public static final  int CLOSE_NORMAL = 1000;
    public static final  int CLOSE_SHUTDOWN = 1001;
    public static final  int CLOSE_PROTOCOL = 1002;
    public static final  int CLOSE_BAD_DATA = 1003;
    public static final  int CLOSE_UNDEFINED = 1004;
    public static final  int CLOSE_NO_CODE = 1005;
    public static final  int CLOSE_NO_CLOSE = 1006;
    public static final  int CLOSE_BAD_PAYLOAD = 1007;
    public static final  int CLOSE_POLICY_VIOLATION = 1008;
    public static final  int CLOSE_MESSAGE_TOO_LARGE = 1009;
    public static final  int CLOSE_REQUIRED_EXTENSION = 1010;
    public static final  int CLOSE_FAILING_WAMP_OVER_WEBSOCKET =1011;

}
