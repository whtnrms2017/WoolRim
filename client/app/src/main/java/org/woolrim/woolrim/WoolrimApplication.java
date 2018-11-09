package org.woolrim.woolrim;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.tsengvn.typekit.Typekit;

import org.woolrim.woolrim.Utils.DBManagerHelper;

public class WoolrimApplication extends Application {
    public final static int REQUSET_MAIN_ACTIVITY = 100;
    public final static int REQUSET_MAIN_FRAGMENT = 101;
    public final static int REQUSET_POEM_LIST_FRAGMENT = 102;
    public final static int REQUSET_RECORD_LIST_FRAGMENT = 103;
    public final static int REQUSET_LOGIN_FRAGMENT = 104;

    public final static int REQUSET_HOME = 110;
    public final static int REQUSET_MY_MENU = 111;
    public final static int REQUSET_FAVORITE = 112;
    public final static int REQUSET_RECORD_LOGOUT = 113;

    public final static String BASE_URL = "http://52.79.33.194:3000/graphql";


    public static RequestQueue requestQueue;
    public static DBManagerHelper dbManagerHelper;

    public static boolean isLogin = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Typekit.getInstance()
                .addBold(Typekit.createFromAsset(this, "font/nanumsquare_eb.ttf"))
                .addNormal(Typekit.createFromAsset(this, "font/nanumsquare_b.ttf"));

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        dbManagerHelper = new DBManagerHelper(WoolrimApplication.this);
        dbManagerHelper.openDatabase();
    }



    @Override
    public void onTerminate() {

        requestQueue.stop();
        requestQueue = null;
        dbManagerHelper.closeDatabase();
        super.onTerminate();
    }


}
