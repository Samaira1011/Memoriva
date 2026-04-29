package com.example.memoriva.network;

public class FriendRequestBody {
    private int targetUserId;

    public FriendRequestBody() {}

    public FriendRequestBody(int targetUserId) {
        this.targetUserId = targetUserId;
    }

    public int getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(int targetUserId) {
        this.targetUserId = targetUserId;
    }
}
