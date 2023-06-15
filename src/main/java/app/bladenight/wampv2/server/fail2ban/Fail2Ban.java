package app.bladenight.wampv2.server.fail2ban;

import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Fail2Ban implements Runnable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Fail2Ban.class);

    @Override
    public void run() {
        boolean cont = true;
        while (cont) {
            updateBanned();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                cont = false;
            }
        }
    }

    protected List<Banned> bannedList = new ArrayList<>();

    ///Checks if is in banned list
    ///if in banned increase failureCounter
    public boolean isBanned(Banned banned) {
        for (Banned item : bannedList
        ) {
            if (item.remoteAddress.equals(banned.remoteAddress) || item.deviceId.equals(banned.deviceId)) {
                addOrUpdateBanned(banned);
                return true;
            }
        }
        return false;
    }

    public void addOrUpdateBanned(Banned banned) {
        List<Banned> foundBanned = new ArrayList<>();
        for (Banned item : bannedList
        ) {
            if (item.remoteAddress.equals(banned.remoteAddress) || item.deviceId.equals(banned.deviceId)) {
                foundBanned.add(item);
            }
        }
        if (foundBanned.size() == 0) {
            bannedList.add(banned);
            logger.warn("Add new to " + banned);
        }
        for (Banned item : foundBanned
        ) {
            Banned bannedCopy = new Banned(item);
            bannedCopy.failures += 1;
            bannedList.remove(item);
            bannedList.add(bannedCopy);
            logger.warn("Updated banned:" + banned);
        }
    }

    public void addOrUpdateBanned(ServletUpgradeRequest req) {
        final String remoteAddress = req.getRemoteAddress();
        Banned banned = new Banned(remoteAddress.replace("/",""), "");
        addOrUpdateBanned(banned);
     }

    public void addOrUpdateBanned(Session session,String request) {
        Banned banned = new Banned();
        banned.remoteAddress = session.getRemoteAddress().getAddress().toString();
        banned.deviceId=request;
        addOrUpdateBanned(banned);
    }

    private void updateBanned() {
        if (bannedList.size() == 0) return;
        List<Banned> foundOutdatedBanned = new ArrayList<>();
        for (Banned item : bannedList
        ) {
            Duration dur = Duration.between(item.dateTime, LocalDateTime.now());
            if (dur.toMillis() > item.failures * 60000L) {
                foundOutdatedBanned.add(item);
            }
        }
        if (foundOutdatedBanned.size() == 0) return;
        for (Banned item : foundOutdatedBanned
        ) {
            bannedList.remove(item);
            logger.warn("Remove from banned:" + item.toString());
        }
    }
}
