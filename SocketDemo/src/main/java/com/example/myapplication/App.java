package com.example.myapplication;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by Howard on 2017/9/7.
 */
public class App extends Application {

    private Socket mSocket;
    @Override
    public void onCreate() {
        super.onCreate();

    }

    public Socket getSocket(String url){
        try {
            mSocket = IO.socket(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return mSocket;
    }
}
