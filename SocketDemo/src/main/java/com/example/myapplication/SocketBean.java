package com.example.myapplication;

/**
 * Created by Howard on 2017/9/7.
 */
public class SocketBean {
    String ip;
    String port;
    String roomCode;

    public SocketBean(String ip, String port, String roomCode) {
        this.ip = ip;
        this.port = port;
        this.roomCode = roomCode;
    }
}
