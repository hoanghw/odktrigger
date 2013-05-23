package org.odk.collect.android.services;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.odk.collect.android.R;


/*
    Keep track of what trigger has been set/completed via SharePreferences
 */
public class TriggerManagerActivity extends Activity{
    private static final String TAG = "TriggerManagerActivity";
    ITMService service;
    TriggerManagerServiceConnection connection;
    String mAnswer;
    String qid;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Get the question ID that launches this activity
        Intent intent = getIntent();
        qid = intent.getStringExtra("qid");

        //Set the view
        setContentView(R.layout.trigger);
        TextView mainMenuMessageLabel = (TextView) findViewById(R.id.main_menu_header);
        mainMenuMessageLabel.setText("Please wait till there's a notification.");

        //Check the trigger status associating with the current question
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (mSharedPreferences.getInt(qid, 0)){
            //Trigger has not been set
            case 0:
                Log.d(TAG, "case:0 qid:"+qid);
                //Bind to the external TMService and set the trigger
                bindTMService(this);
                break;

            //Trigger has been set
            case 1:
                Log.d(TAG, "case:1 qid:"+qid);
                break;

            //Trigger completed
            case 2:
                Log.d(TAG, "case:2 qid:"+qid);
                mAnswer="Case 2";
                returnClearance();
                break;
            default:
        }
    }

    //A connection to bind to the service
    public class TriggerManagerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder boundService) {

            //service is an interface to interact with methods in the external service's binder object
            service = ITMService.Stub.asInterface((IBinder) boundService);
            Log.d(TAG, "onServiceConnected() called");
            Toast.makeText(TriggerManagerActivity.this, "Service connected",
                    Toast.LENGTH_LONG).show();

            //registerCallback and setTrigger are methods of the external service's binder object
            try {
                service.registerCallback(mCallback);
                service.setTrigger(qid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //unregisterCallback is a method of the external service's binder object
            try {
                service.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            service = null;
            Log.d(TAG, "onServiceDisconnected() called");
            Toast.makeText(TriggerManagerActivity.this,
                    "Service disconnected",
                    Toast.LENGTH_LONG).show();
        }
    }

    //This is an example of Callback from Android Documentation
    //The external service will broadcast updateAnswer(value) to all the binded activities
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

    //This is an example of Callback from Android Documentation
    //Handle the message whose request code is REPORT_MSG
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
        Log.d(TAG, "releaseService() called");
    }

    //Change the Intent's name to the external TMService's name
    public void bindTMService(Context context){
        connection = new TriggerManagerServiceConnection();
        Intent i = new Intent("org.odk.collect.android.services.TriggerManagerService");
        context.startService(i);
        boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Binding success? " + ret);

        //The status of the trigger is set to 1
        SharedPreferences mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(TriggerManagerActivity.this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(qid,1);
        editor.commit();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        releaseService();
    }

    //Only return when trigger completed
    private void returnClearance() {
        //The status of the trigger is set to 2
        //It should be reset to 0 after user finishes the form
        SharedPreferences mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(TriggerManagerActivity.this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(qid,2);
        editor.commit();

        Intent intent = new Intent();
        intent.putExtra("value", mAnswer);
        setResult(RESULT_OK, intent);
        finish();
    }

    //Prevent user from going back to the current trigger question
    @Override
    public void onBackPressed(){
        Toast.makeText(TriggerManagerActivity.this, "You can't go back.  Please press HOME and wait for notification.",
                Toast.LENGTH_LONG).show();
    }
}
