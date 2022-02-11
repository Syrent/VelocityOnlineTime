package ir.sayandevelopment.sayanplaytime;

import java.util.UUID;

public class PPlayer {

    private String userName;
    private UUID uuid;
    private long time;

    public PPlayer(UUID uuid, String userName, long time) {
        this.uuid = uuid;
        this.userName = userName;
        this.time = time;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUserName() {
        return userName;
    }

    public long getTime() {
        return time;
    }
}
