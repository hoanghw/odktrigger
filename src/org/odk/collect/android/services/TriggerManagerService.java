package org.odk.collect.android.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import org.odk.collect.android.services.ITMService;
import org.odk.collect.android.services.ITMServiceCallback;

/**
 * Created with IntelliJ IDEA.
 * Author: Sid Feygin
 * Date: 4/23/13
 * Time: 9:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class TriggerManagerService extends Service {

    private static final String TAG = "TriggerManagerService";
    SharedPreferences mSharedPreferences;
    Editor editor;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreated");
        mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        editor = mSharedPreferences.edit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the interface
        return new ITMService.Stub() {

            public void registerCallback(ITMServiceCallback cb){
                if (cb!=null) mCallbacks.register(cb);
            }
            public void unregisterCallback(ITMServiceCallback cb){
                if (cb!=null) mCallbacks.unregister(cb);
            }
            public void setTrigger(String qid) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(REPORT_MSG,"true"),10*1000);
                editor.putInt(qid,1);
                editor.commit();
            }
        };
    }

    final RemoteCallbackList<ITMServiceCallback> mCallbacks
            = new RemoteCallbackList<ITMServiceCallback>();


    private static final int REPORT_MSG = 1;
    /**
     * Our Handler used to execute operations on the main thread.  This is used
     * to schedule increments of our value.
     */
    private final Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case REPORT_MSG: {
                    // Broadcast to all clients
                    final int N = mCallbacks.beginBroadcast();
                    for (int i=0; i<N; i++) {
                        try {
                            mCallbacks.getBroadcastItem(i).updateAnswer(msg.obj.toString());
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast();

                } break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroyed");

        // Unregister all callbacks.
        mCallbacks.kill();

        // Remove the next pending message to increment the counter, stopping
        // the increment loop.
        mHandler.removeMessages(REPORT_MSG);
    }

}
