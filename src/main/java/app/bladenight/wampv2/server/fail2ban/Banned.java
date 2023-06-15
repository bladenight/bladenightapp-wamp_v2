package app.bladenight.wampv2.server.fail2ban;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.UUID;

public class Banned {
    UUID id=UUID.randomUUID();
    String deviceId="";
    String remoteAddress="";
    java.time.LocalDateTime dateTime = LocalDateTime.now();
    int failures=1;

    public Banned(){
        reset();
    }

    public Banned(Banned item) {
        this.deviceId = item.deviceId;
        this.id = item.id;
        this.failures = item.failures;
        this.remoteAddress=item.remoteAddress;
        this.dateTime = item.dateTime;
    }

    public Banned(String remoteAddress,String deviceId) {
        this.deviceId = deviceId;
        this.remoteAddress=remoteAddress;
        this.id = UUID.randomUUID();
        reset();
    }


    void reset(){
        failures=1;
        dateTime = LocalDateTime.now();
    }

    Logger getLog(){
        return LogManager.getLogger(Banned.class);
    }

    @Override
    public String toString() {
        return "Banned{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", localDate=" + dateTime +
                ", failures=" + failures +
                '}';
    }
}
