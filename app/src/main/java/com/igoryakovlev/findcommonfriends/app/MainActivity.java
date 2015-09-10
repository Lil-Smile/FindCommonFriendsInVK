package com.igoryakovlev.findcommonfriends.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;


public class MainActivity extends Activity implements View.OnClickListener {

    Button buttonAuthorize;

    static String FRIENDS = "friends";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonAuthorize = (Button)findViewById(R.id.buttonAuthorize);
        buttonAuthorize.setOnClickListener(this);

        //autorize+start
        if (VKSdk.wakeUpSession(this))
        {
            Intent intent = new Intent(MainActivity.this,FindFromFriends.class);
            startActivity(intent);
        } else
        {
            //todo: cannot wake up
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.buttonAuthorize:
            {
                if (isNetworkOnline(this))
                {
                    String[] scope = {FRIENDS};
                    VKSdk.login(this, scope);
                } else
                {
                    Toast.makeText(this,getString(R.string.noConnection),Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Intent intent = new Intent(MainActivity.this,FindFromFriends.class);
                startActivity(intent);
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(),getString(R.string.smthIsWrond),Toast.LENGTH_LONG).show();
            }
        }))
        {
            super.onActivityResult(requestCode,resultCode,data);
        }
    }


    public boolean isNetworkOnline(Context context) {
        boolean status = false;
        //Log.d(TAG, "start");
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0); //mobile
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
                //Log.d(TAG,"try mobile"+netInfo);
            } else {
                netInfo = cm.getNetworkInfo(1); //wi-fi
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    status = true;
                    //Log.d(TAG,"try wi-fi"+netInfo);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;

    }

}