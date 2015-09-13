package com.igoryakovlev.findcommonfriends.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;


public class FindFromFriends extends Activity implements View.OnClickListener{

    private static String ITEMS = "items";
    private static String RESPONSE = "response";
    private static String ID = "id";
    private static String LAST_NAME = "last_name";
    private static String FIRST_NAME = "first_name";
    private static String PHOTO_50 = "photo_50";
    private static String CHECKED = "checked";
    private static String CURRENT_DATA = "current data";
    private static String COMMON_OR_NOT = "common or not"; //common - true


    String[] from = {FIRST_NAME,LAST_NAME,ID,CHECKED};
    int[] to = {R.id.tvName, R.id.tvSurname, R.id.tvID, R.id.checkBoxOfFriend};

    ArrayList<ArrayList<User>> dataOfIds;

    ProgressBar progressBarMyFriendsList;
    Button buttonFindFromMyFriends;
    TextView tvYourFriends;
    ListView lvMyFriends;

    SimpleAdapter simpleAdapter;
    ArrayList<HashMap<String,Object>> data;
    ArrayList<Integer> dataForRequest;
    ArrayList<User> currentDataWithInfo;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_from_friends);

        progressBarMyFriendsList = (ProgressBar)findViewById(R.id.progressBarMyFriendsList);
        buttonFindFromMyFriends = (Button)findViewById(R.id.buttonFindCommonFromMyFriends);
        tvYourFriends = (TextView)findViewById(R.id.tvYourFriends);
        lvMyFriends = (ListView)findViewById(R.id.lvMyFriendsList);


        buttonFindFromMyFriends.setOnClickListener(this);
        lvMyFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                boolean flag = ((Boolean)data.get((int)id).get(CHECKED)).booleanValue();
                if (flag)
                {
                    HashMap<String,Object> m = data.get(position);
                    String userIdString = (String)m.get(ID);
                    int idUser = Integer.valueOf(userIdString).intValue();
                    dataForRequest.remove((Object)idUser);
                } else
                {
                    HashMap<String,Object> m = data.get(position);
                    String userIdString = (String)m.get(ID);
                    int idUser = Integer.valueOf(userIdString).intValue();
                    dataForRequest.add(idUser);
                }
                data.get((int)id).put(CHECKED,!flag);
                simpleAdapter.notifyDataSetChanged();
            }
        });

        data = new ArrayList<HashMap<String, Object>>();
        dataForRequest = new ArrayList<Integer>();



        if (savedInstanceState==null) {
            tvYourFriends.setVisibility(View.INVISIBLE);
            buttonFindFromMyFriends.setVisibility(View.INVISIBLE);
            FindFromMyFriendsAsyncTask asyncTask = new FindFromMyFriendsAsyncTask();
            asyncTask.execute();
        } else
        {
            ContainerArrayList containerArrayList = (ContainerArrayList)savedInstanceState.getSerializable(CURRENT_DATA);
            currentDataWithInfo = containerArrayList.getData();
            data = new ArrayList<HashMap<String,Object>>();
            for (int i = 0; i<currentDataWithInfo.size(); i++)
            {
                HashMap<String, Object> m = new HashMap<String, Object>();
                m.put(ID,currentDataWithInfo.get(i).getId());
                m.put(LAST_NAME, currentDataWithInfo.get(i).getSurname());
                m.put(FIRST_NAME,currentDataWithInfo.get(i).getName());
                m.put(CHECKED,false);
                data.add(m);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("uiThread","run");
                    simpleAdapter = new SimpleAdapter(getApplicationContext(),data,R.layout.item_for_list,from,to);
                    simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    lvMyFriends.setAdapter(simpleAdapter);
                    int count = data.size();
                    if (savedInstanceState.getBoolean(COMMON_OR_NOT))
                    {
                        String str = count+" ";
                        if ((count%100)==1)
                        {
                            str = str + getResources().getString(R.string.commonFriend);
                        } else if (((count%100)<5) && ((count%100)>1))
                        {
                            str = str + getResources().getString(R.string.commonFriendsLessThan10);
                        } else
                        {
                            str = str + getResources().getString(R.string.commonFriends);
                        }
                        tvYourFriends.setText(str);
                    } else
                    {
                        tvYourFriends.setText(getResources().getString(R.string.yourFriends));
                    }
                    progressBarMyFriendsList.setVisibility(View.INVISIBLE);
                }
            });
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

        Log.d("dataForRequest.size",dataForRequest.size()+"");
        dataOfIds = new ArrayList<ArrayList<User>>();
        if (dataForRequest.size()>1)
        {
            final ArrayList<Integer> commonFriendsId = new ArrayList<Integer>();
            for (int i = 0; i<dataForRequest.size(); i++)
            {

                final int tmp = (Integer)dataForRequest.get(i).intValue();
                VKRequest request = new VKRequest("friends.get",VKParameters.from("user_id",tmp, VKApiConst.FIELDS,"id,first_name,last_name"));
                final int finalI = i;
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Log.d("json", response.json.toString());
                        try {
                            JSONObject jsonObject = response.json.getJSONObject(RESPONSE);
                            JSONArray jsonArray = jsonObject.getJSONArray(ITEMS);
                            ArrayList<User> tmpDataids = new ArrayList<User>();
                            for (int i = 0; i<jsonArray.length(); i++)
                            {
                                JSONObject object = jsonArray.getJSONObject(i);
                                String name = object.getString(FIRST_NAME);
                                String surname = object.getString(LAST_NAME);
                                String id = object.getString(ID);
                                User user = new User(name,surname,id);
                                tmpDataids.add(user);
                            }
                            dataOfIds.add(tmpDataids);
                            if (dataOfIds.size()==dataForRequest.size())
                            {
                                getCommonFriends(dataOfIds);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }


        }

    }

    private void getCommonFriends (ArrayList<ArrayList<User>> dataToFind)
    {
        ArrayList<User> dataToPrint = dataToFind.get(0);
        Log.d("dataToPrint size",dataToPrint.size()+"");

        for (int i = 1; i<dataToFind.size(); i++) //in list of lists of ids
        {
            ArrayList<User> currentListOfIds = dataToFind.get(i);
            Log.d("currentListOfIds.size", currentListOfIds.size() + "");
            for (int j = 0; j<dataToPrint.size(); j++)
            {
                Integer currentId = Integer.valueOf(dataToPrint.get(j).getId());
                boolean trigger = false;
                for (int k = 0; k<currentListOfIds.size(); k++)
                {
                    Integer tmpCurrentId = Integer.valueOf(currentListOfIds.get(k).getId());
                    if (currentId.equals(tmpCurrentId))
                    {
                        trigger=true;
                        break;
                    }
                }
                if (!trigger)
                {
                    dataToPrint.remove(j);
                    j--;
                }
            }
        }

        Log.d("size",dataToPrint.size()+"");
        currentDataWithInfo = dataToPrint;
        data = new ArrayList<HashMap<String,Object>>();
        for (int i = 0; i<dataToPrint.size(); i++)
        {
            Log.d("listOfCommonIds", dataToPrint.get(i).toString());
            HashMap<String, Object> m = new HashMap<String, Object>();
            m.put(ID,dataToPrint.get(i).getId());
            m.put(LAST_NAME, dataToPrint.get(i).getSurname());
            m.put(FIRST_NAME,dataToPrint.get(i).getName());
            m.put(CHECKED,false);
            data.add(m);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("uiThread","run");
                simpleAdapter = new SimpleAdapter(getApplicationContext(),data,R.layout.item_for_list,from,to);
                simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                lvMyFriends.setAdapter(simpleAdapter);
                int count = data.size();
                String str = count+" ";
                if ((count%100)==1)
                {
                    str = str + getResources().getString(R.string.commonFriend);
                } else if (((count%100)<5) && ((count%100)>1))
                {
                    str = str + getResources().getString(R.string.commonFriendsLessThan10);
                } else
                {
                    str = str + getResources().getString(R.string.commonFriends);
                }
                tvYourFriends.setText(str);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putSerializable(CURRENT_DATA, new ContainerArrayList(currentDataWithInfo));
        if (tvYourFriends.getText().toString().equals(getResources().getString(R.string.yourFriends)))
        {
            savedInstanceState.putBoolean(COMMON_OR_NOT, false);
        } else
        {
            savedInstanceState.putBoolean(COMMON_OR_NOT,true);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    private class FindFromMyFriendsAsyncTask extends AsyncTask<Void,Void,ArrayList<User>>
    {
        @Override
        protected ArrayList<User> doInBackground(Void... voids) {
            final ArrayList<User> dataFromJson = new ArrayList<User>();
            final boolean[] trigger = {false};
            VKRequest request = new VKRequest("friends.get", VKParameters.from(VKApiConst.FIELDS,"id,first_name,last_name,photo_50"));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    JSONObject jsonObject = response.json;
                    try {
                        JSONObject jsonObject1 = jsonObject.getJSONObject(RESPONSE);
                        JSONArray jsonArray = jsonObject1.getJSONArray(ITEMS);
                        Log.d("array",jsonArray.toString());
                        for (int i = 0; i<jsonArray.length(); i++)
                        {
                            JSONObject tmp = jsonArray.getJSONObject(i);
                            String name = tmp.getString(FIRST_NAME);
                            String surname = tmp.getString(LAST_NAME);
                            String id = tmp.getString(ID);
                            /*String photo_50StringURL = tmp.getString(PHOTO_50); todo: rewrite photo

                            URL url = new URL(photo_50StringURL);
                            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();
                            InputStream inputStream = connection.getInputStream();
                            Bitmap photo = BitmapFactory.decodeStream(inputStream);*/

                            User user = new User(name,surname,id);
                            dataFromJson.add(user);
                        }
                        trigger[0] = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            while (!trigger[0]){}
            return dataFromJson;
        }

        @Override
        protected void onPostExecute(ArrayList<User> inData)
        {
            data = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i<inData.size(); i++)
            {
                HashMap<String,Object> tmp = new HashMap<String,Object>();
                tmp.put(FIRST_NAME,inData.get(i).getName());
                tmp.put(LAST_NAME,inData.get(i).getSurname());
                tmp.put(ID,inData.get(i).getId());
                tmp.put(CHECKED,false);
                data.add(tmp);
            }
            if (simpleAdapter==null)
            {
                simpleAdapter = new SimpleAdapter(getApplicationContext(),data,R.layout.item_for_list,from,to);
                simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                lvMyFriends.setAdapter(simpleAdapter);
            } else
            {
                simpleAdapter.notifyDataSetChanged();
            }
            progressBarMyFriendsList.setVisibility(View.INVISIBLE);
            tvYourFriends.setVisibility(View.VISIBLE);
            buttonFindFromMyFriends.setVisibility(View.VISIBLE);
        }

    }

}
