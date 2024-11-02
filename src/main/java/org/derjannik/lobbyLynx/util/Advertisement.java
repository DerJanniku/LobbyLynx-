package org.derjannik.lobbyLynx.util;

import org.derjannik.lobbyLynx.enums.AdType;

import java.util.List;

public class Advertisement {
    private String id;
    private String title;
    private List<String> content;
    private AdType type;
    private int duration;
    private String permission;

    public Advertisement(String id, String title, List<String> content, AdType type, int duration, String permission) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.duration = duration;
        this.permission = permission;
    }

    // Getter und Setter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public List<String> getContent() { return content; }
    public AdType getType() { return type; }
    public int getDuration() { return duration; }
    public String getPermission() { return permission; }
}

