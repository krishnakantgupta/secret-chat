package com.kk.secretchat.chat;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity
public class ModelChat implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int _id;

    private String chatID;
    private String fromId;
    private String toId;
    private String message;
    private String name;
    private Long timestamp;
    private boolean isDeliver;
    private boolean isRead;
    private boolean isSend;
    private boolean isIncomming;
    private String repliedID;

    public ModelChat() {
    }

    public ModelChat(String message, String name, Long timestamp) {
        this.message = message;
        this.name = name;
        this.timestamp = timestamp;
    }
    public ModelChat(String from, String to, String msg){
        fromId = from;
        toId =to;
        message = msg;
        timestamp = new Date().getTime();
    }

    public ModelChat(IncommingMessageModal message){
        fromId = message.getFrom().substring(0,message.getFrom().indexOf("/"));
        toId = message.getTo().substring(0,message.getTo().indexOf("/"));
        chatID = message.getId();
        this.message = message.getBody();
        this.isIncomming = true;
        this.isRead = true;
        if(message.getDelay()!=null){
            timestamp = message.getDelay().getStamp().getTime();
        }else {
            timestamp = new Date().getTime();
        }
        name = fromId.substring(0,fromId.indexOf("@"));
        this.repliedID = message.getReplyId();
    }

    public ModelChat(OutGoingMessageModel message){
        fromId = message.getFrom();
        toId = message.getTo();
        chatID = message.getId();
        this.message = message.getBody();
        this.isIncomming = false;
        timestamp = new Date().getTime();
        name = toId.substring(0,toId.indexOf("@"));
        this.repliedID = message.getReplyId();
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }


    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public boolean isDeliver() {
        return isDeliver;
    }

    public void setDeliver(boolean deliver) {
        isDeliver = deliver;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public boolean isIncomming() {
        return isIncomming;
    }

    public void setIncomming(boolean incomming) {
        isIncomming = incomming;
    }

    public String getRepliedID() {
        return repliedID;
    }

    public void setRepliedID(String repliedID) {
        this.repliedID = repliedID;
    }
}
