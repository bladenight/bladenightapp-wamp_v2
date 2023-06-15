package app.bladenight.wampv2.server.utilities;


import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

/**
 * Created by mgallina.
 */
public final class GeneralUtilities {
    public final static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    // Should be set to UTC from command line
    public final static Date getCurrentDate() {
        return Date.from(getCurrentDateTime().atZone(ZoneId.systemDefault()).toInstant());
    }

    public final static Date toDate(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    public final static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public final static byte[] getBytesUTF8(String string)
    {
        if (string == null)
        {
            return null;
        }
        try
        {
            return string.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }

        private long sessionId;

        public long getNextSessionID() {
            sessionId += 1;
            if (sessionId > 9007100254740990L) {
                sessionId = 1;
            }
            return sessionId;
    }
}
