package com.example.myapplication;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.model.ImageBean;
import com.example.myapplication.model.MessageBean;
import com.example.myapplication.model.OccupyBean;
import com.example.myapplication.model.RedPackageBean;
import com.example.myapplication.model.RewardBean;
import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Response;


public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    private ImageView mQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mQRCode = (ImageView) findViewById(R.id.qr);
        mQRCode.setImageBitmap(QRCodeUtil.createQRCode(qrcode));
        getRoomCode();
    }

    private void getQRCode() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(qrcode).build();
        Response response = null;
        String result;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                result =  response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void getRoomCode() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("auid", "59b122bfe5dcd37c33f30c15");
        builder.add("uid", "58bd1730a4258b599fb22188");
        builder.add("oid", "59b12303e5dcd37c33f30c17");
        builder.add("type", "1");
        Request request = new Request.Builder()
                .url("http://101.200.176.51:10086/screen/v1/getRoomCode")
                .post(builder.build())
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String data = response.body().string();
                LogUtils.hLog().d("onResponse: " + data);
                try {
                    JSONObject json = new JSONObject(data);
                    String roomCode = json.getString("roomCode");
                    String ip = json.getString("ip");
                    String port = json.getString("port");
                    SocketBean bean = new SocketBean(ip, port, roomCode);
                    configSocket(bean);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void configSocket(SocketBean bean) {
        String url = "http://" + bean.ip + ":" + bean.port + "?reconnect=0/1&roomCode=" + bean.roomCode;
        App app = (App) getApplication();
        mSocket = app.getSocket(url);
        mSocket.on("disconnect", onDisconnect);
        mSocket.on("createRoomOver", onCreateRoomOver);
        mSocket.on("userJoin", onUserJoin);
        mSocket.on("userLeave", onUserLeave);
        mSocket.on("dataFromUser", onDataFromUser);
        mSocket.on("err", onError);
        connectSocket(bean.roomCode);
    }

    private void connectSocket(final String roomCode) {
        mSocket.on("connected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtils.hLog().d("onConnected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                createRoom(roomCode);
            }
        });
        mSocket.connect();
    }


    private void createRoom(String roomCode) {
        JSONObject json = new JSONObject();
        try {
            json.put("roomCode", roomCode);
            json.put("maxUserCount", 1000);
            json.put("codeTime", 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.hLog().d(json.toString());
        mSocket.emit("createRoom", json);
    }

    private Emitter.Listener onCreateRoomOver = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            LogUtils.hLog().d("onCreateRoomOver ");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });

        }
    };

    private String qrcode = "http://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=gQGi8DwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyMGd4RWNRY3BjZDAxVEQ2MjFwMWoAAgSDI7FZAwRkIxEA";

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            LogUtils.hLog().d("onDisconnect " + Thread.currentThread().getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "onDisconnect", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onUserJoin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            String uid, nickName, sex, userIcon;
            try {
                uid = data.getString("uid");
                nickName = data.getString("nickName");
                userIcon = data.getString("userIcon");
                sex = data.getString("sex");
            } catch (JSONException e) {
                LogUtils.hLog().e(e.getMessage());
                return;
            }
            if (uid != null && nickName != null && sex != null && userIcon != null) {
                LogUtils.hLog().i("new User Joined " + data.toString());
            }
        }
    };

    private Emitter.Listener onUserLeave = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            LogUtils.hLog().d("onUserLeave " + Thread.currentThread().getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "onUserLeave", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onDataFromUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject json = (JSONObject) args[0];
            String type;
            try {
                json = json.getJSONObject("data");
                type = json.getString("type");
            } catch (JSONException e) {
                LogUtils.hLog().e(e.getMessage());
                return;
            }
            LogUtils.hLog().d("data from user type = " + json);

            final Gson gson = new Gson();
            switch (type){
                case "1":
                    gson.fromJson(json.toString(), MessageBean.class);
                    break;
                case "2":
                    gson.fromJson(json.toString(), ImageBean.class);
                    break;
                case "3":
                    gson.fromJson(json.toString(), OccupyBean.class);
                    break;
                case "4":
                    gson.fromJson(json.toString(), RewardBean.class);
                    break;
                case "5":
                    gson.fromJson(json.toString(), RedPackageBean.class);
                    break;
                default:
                    break;
            }
            mSocket.emit("dataToUser", (JSONObject) args[0]);
            LogUtils.hLog().d(json.toString());
            LogUtils.hLog().w(((JSONObject) args[0]).toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    LogUtils.hLog().d(json.toString());
                }
            });

        }
    };

    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            LogUtils.hLog().d("onError " + Thread.currentThread().getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "onError", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
}
