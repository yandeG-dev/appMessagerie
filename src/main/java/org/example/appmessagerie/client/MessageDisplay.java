package org.example.appmessagerie.client;

public class MessageDisplay {
    private final String sender;
    private final String content;
    private final boolean isMine;

    public MessageDisplay(String sender, String content, boolean isMine) {
        this.sender = sender;
        this.content = content;
        this.isMine = isMine;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }
    public boolean isMine() { return isMine; }
}
