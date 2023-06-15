package app.bladenight.wampv2.server.messages;

//https://github.com/Raynes/WAMP/blob/master/spec/basic.md
public enum MessageType {
    HELLO(1),
    WELCOME(2),
    ABORT(3),
    CHALLANGE(4),
    AUTHENTICATE(5),
    GOOOBYE(6),
    ERROR(8),
    PUBLISH(16),
    PUBLISHED(17),
    SUBSCRIBE(32),
    SUBSCRIBED(33),
    UNSUBSCRIBE(34),
    UNSUBSCRIBED(35),
    EVENT(36),
    CALL(48),
    CANCEL(49),
    RESULT(50),
    REGISTER(64),
    REGISTERED(65),
    UNREGISTER(66),
    UNREGISTERED(67),
    INVOCATION(68),
    INTERRUPT(69),
    YIELD(70);
	/*OLD STUFF not Wamp compliant
	CALL(2),
	CALLRESULT(3),
	CALLERROR(4),
	SUBSCRIBE(5),
	UNSUBSCRIBE(6),
	PUBLISH(7),
	EVENT(8)*/


    private int value;

    private MessageType(int value) {
        this.value = value;
    }

    public int getCode() {
        return value;
    }

    public static MessageType fromInteger(int value) {
        for (MessageType type : values()) {
            if (type.getCode() == value) {
                return type;
            }
        }
        return null;
    }
}
