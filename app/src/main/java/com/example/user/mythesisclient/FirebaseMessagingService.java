package com.example.user.mythesisclient;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by User on 2/20/2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    Intent i;
    private static final String mtag = FirebaseMessagingService.class.getSimpleName();

    private static UserSession userSession;
    NotificationManager manager;

    @Override
    public void onCreate() {
        super.onCreate();
        userSession = new UserSession(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //Set Accepted
        userSession.setIsAccepted(true);

        showNotification(remoteMessage.getData().get("message"));
        try {
            startActivity(i);
            manager.cancel(0);

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showNotification(String message){

        i = new Intent(this, AcceptedDriverInfoActivity.class);
        i.putExtra("message",message);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Log.w(mtag,message);

        PendingIntent pendingIntent = PendingIntent.getActivities(this,0, new Intent[]{i},PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("ATBSys")
                .setContentText(message)
                .setSmallIcon(R.drawable.taxi)
                //Vibration
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                //Sound
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());



    }


}
