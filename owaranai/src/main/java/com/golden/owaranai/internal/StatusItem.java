package com.golden.owaranai.internal;

import android.graphics.Bitmap;
import twitter4j.Status;

/**
 * Created by soomin on 3/26/2014.
 */
public class StatusItem {
    private String id;
    private Status status;
    private Bitmap profilePic;

    public StatusItem(Status status) {
        this.id = String.valueOf(status.getId());
        this.status = status;
        this.profilePic = null;
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public Bitmap getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
    }
}
