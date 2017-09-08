package com.example.myapplication.model;

/**
 * Created by Howard on 2017/9/8.
 */
public class RewardBean extends BaseBean{
    class Data{
        public boolean rewardToOther;
        public UserInfo rewardOtherInfo;
        class RewardToDefault{
            public String type;
            public String font;
        }
        public RewardToDefault rewardToDefault;
        class RewardGiftType{
            public String type;
            public String font;
        }
        public RewardGiftType rewardGiftType;
        public String rewardMsg;
    }
}
