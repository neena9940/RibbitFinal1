package com.teamtreehouse.ribbitfinal1;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by NY on 3/10/2015.
 */
public class RibbitApplication extends Application {
    @Override
    public void onCreate(){
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "hFgz1NDOgrN2g0HtJOl1B31j6ycgK9rZnC8LjySQ", "0fOC2h5QSRPFlEi84ypwddeoHiaLXBKca87wCXke");



    }
}
