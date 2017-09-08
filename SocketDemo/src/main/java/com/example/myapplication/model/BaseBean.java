package com.example.myapplication.model;

/**
 * Created by Howard on 2017/9/8.
 */
public class BaseBean {
    public String id;
    public String index = "0";
    public String type;
    public UserInfo userInfo;
    class UserInfo{
        public String userId;
        public String nickName;
        public String userAvatar;
        public String sex;
    }
}
