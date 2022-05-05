package com.kk.secretchat.chat;

import com.google.gson.annotations.SerializedName;

public class OutGoingMessageModel {
    private String from;
    @SerializedName("to")
    private String to;
    @SerializedName("id")
    private String id;
    private String body;
    private String subject;
    private String replyId;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReplyId(){
        if(subject!=null && subject.startsWith(Constant.REPLY_PREFIX)){
            replyId  = subject.substring(Constant.REPLY_PREFIX.length());
        }
        return replyId;
    }


    public boolean isReadSubject() {
        return subject != null && subject.startsWith(Constant.READ_PREFIX);
    }

}
