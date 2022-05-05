package com.kk.secretchat.chat;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.time.Instant;

public class IncommingMessageModal {
    private String to;
    private String from;
    private String id;
    @SerializedName("archived")
    private ArchivedModel archived;
    @SerializedName("delay")
    private DelayMessage delay;
    private String subject;
    private String replyId;

    @SerializedName("stanza-id")
    private StanzaModel stanza;

    private String body;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArchivedModel getArchived() {
        return archived;
    }

    public void setArchived(ArchivedModel archived) {
        this.archived = archived;
    }

    public StanzaModel getStanza() {
        return stanza;
    }

    public void setStanza(StanzaModel stanza) {
        this.stanza = stanza;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public DelayMessage getDelay() {
        return delay;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDelay(DelayMessage delay) {
        this.delay = delay;
    }

    public String getReplyId() {
        if (subject != null && subject.startsWith(Constant.REPLY_PREFIX)) {
            replyId = subject.substring(Constant.REPLY_PREFIX.length());
        }
        return replyId;
    }

    public boolean isReadSubject() {
        return subject != null && subject.startsWith(Constant.READ_PREFIX);
    }

    public String[] getReadIds(){
       String readIds =  subject.substring(Constant.READ_PREFIX.length());
       String ids[]  = readIds.split("#");
       return ids;
    }

    public void getDelayTime() {
        if (getDelay() != null & getDelay().getStamp() != null) {
//            String dateString = getDelay().getStamp();

////            2022-02-14T18:10:35.580+00:00"
//            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-ddTHH:mm:");
//            try {
//                Date d = f.parse(string_date);
//                long milliseconds = d.getTime();
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
        }
    }
}
