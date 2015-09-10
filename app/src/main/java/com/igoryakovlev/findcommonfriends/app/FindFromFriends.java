package com.igoryakovlev.findcommonfriends.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;


public class FindFromFriends extends Activity implements View.OnClickListener{

    ProgressBar progressBarMyFriendsList;
    Button buttonFindFromMyFriends;
    TextView tvYourFriends;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_from_friends);

        progressBarMyFriendsList = (ProgressBar)findViewById(R.id.progressBarMyFriendsList);
        buttonFindFromMyFriends = (Button)findViewById(R.id.buttonFindCommonFromMyFriends);
        tvYourFriends = (TextView)findViewById(R.id.tvYourFriends);

        if (savedInstanceState==null) {
            tvYourFriends.setVisibility(View.INVISIBLE);
            buttonFindFromMyFriends.setVisibility(View.INVISIBLE);
            FindFromMyFriendsAsyncTask asyncTask = new FindFromMyFriendsAsyncTask();
            asyncTask.execute();
        } else
        {
            //todo: add checking state changed
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_from_friends, menu);
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

    }

    private class FindFromMyFriendsAsyncTask extends AsyncTask<Void,Void,ArrayList<User>>
    {
        @Override
        protected ArrayList<User> doInBackground(Void... voids) {

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<User> params)
        {}

    }

}
