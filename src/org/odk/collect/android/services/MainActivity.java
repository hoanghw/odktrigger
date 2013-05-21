package org.odk.collect.android.services;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

public class MainActivity extends Activity{
    private static final String TAG = "MainActivity";
    ITMService service;
    TriggerManagerServiceConnection connection;
    String mAnswer;
    String qid = "q1";
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_menu);
        TextView mainMenuMessageLabel = (TextView) findViewById(R.id.main_menu_header);
        mainMenuMessageLabel.setText("Please wait till there's a notification");

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (mSharedPreferences.getInt(qid, 0)){
            case 0:
                Log.d(TAG, "case:0 qid:"+qid);
                bindTMService(this);
                break;
            case 1:
                Log.d(TAG, "case:1 qid:"+qid);
                break;
            case 2:
                Log.d(TAG, "case:2 qid:"+qid);
                mAnswer="Case 2";
                returnClearance();
                break;
            default:
        }
    }

    public class TriggerManagerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = ITMService.Stub.asInterface((IBinder) boundService);
            Log.d(TAG, "onServiceCOnnected() connected");
            Toast.makeText(MainActivity.this, "Service connected",
                    Toast.LENGTH_LONG).show();

            try {
                service.registerCallback(mCallback);
                service.setTrigger(qid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            try {
                service.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            service = null;
            Log.d(TAG, "onServiceDisconnected() disconnected");
            Toast.makeText(MainActivity.this,
                    "Service disconnected",
                    Toast.LENGTH_LONG).show();
        }
    }

    private ITMServiceCallback mCallback = new ITMServiceCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
        public void updateAnswer(String value) {
            mHandler.sendMessage(mHandler.obtainMessage(REPORT_MSG, value));
        }
    };

    private static final int REPORT_MSG = 1;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case REPORT_MSG:
                    mAnswer="Received from service: " + msg.obj;
                    returnClearance();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    private void releaseService() {
        if (connection!= null) {
            unbindService(connection);
            connection = null;
        }
        Log.d(TAG, "releaseService() unbound");
    }
    public void bindTMService(Context context){
        connection = new TriggerManagerServiceConnection();
        Intent i = new Intent("org.odk.collect.android.services.TriggerManagerService");
        context.startService(i);
        boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Binding success? " + ret);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        releaseService();
    }
    private void returnClearance() {
        Intent intent = new Intent();
        intent.putExtra("value", mAnswer);
        setResult(RESULT_OK, intent);
        finish();
    }
}
