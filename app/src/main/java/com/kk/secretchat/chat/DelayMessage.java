package com.kk.secretchat.chat;

import java.util.Date;

public class DelayMessage {
    private String content;
    private Date stamp;
//            "stamp": "2022-02-14T18:10:35.580+00:00"

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

//    public String getStamp() {
//        return stamp;
//    }
//
//    public void setStamp(String stamp) {
//        this.stamp = stamp;
//    }


    public Date getStamp() {
        return stamp;
    }

    public void setStamp(Date stamp) {
        this.stamp = stamp;
    }
}
