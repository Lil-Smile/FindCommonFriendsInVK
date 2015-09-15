package com.igoryakovlev.findcommonfriends.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.vk.sdk.api.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;


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

    private static String VK_LINK = "https://vk.com/id";


    private static int SIZE_OF_LIST=0;

    String[] from = {FIRST_NAME,LAST_NAME,ID,CHECKED,PHOTO_50};
    int[] to = {R.id.tvName, R.id.tvSurname, R.id.tvID, R.id.checkBoxOfFriend, R.id.ivPhoto};

    Stack<ArrayList<HashMap<String,Object>>> backStack = new Stack<ArrayList<HashMap<String,Object>>>();

    ArrayList<ArrayList<User>> dataOfIds;

    ProgressBar progressBarMyFriendsList;
    Button buttonFindFromMyFriends;
    TextView tvYourFriends;
    ListView lvMyFriends;

    ExtendedSimpleAdapter simpleAdapter;
    ArrayList<HashMap<String,Object>> data;
    ArrayList<Bitmap> dataBitmap;
    ArrayList<Integer> dataForRequest;
    ArrayList<User> currentDataWithInfo;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_from_friends);
        Log.d("logged", "onCreate FFF");

        progressBarMyFriendsList = (ProgressBar)findViewById(R.id.progressBarMyFriendsList);
        buttonFindFromMyFriends = (Button)findViewById(R.id.buttonFindCommonFromMyFriends);
        tvYourFriends = (TextView)findViewById(R.id.tvYourFriends);
        lvMyFriends = (ListView)findViewById(R.id.lvMyFriendsList);


        buttonFindFromMyFriends.setOnClickListener(this);
        lvMyFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                boolean flag = ((Boolean) data.get((int) id).get(CHECKED)).booleanValue();
                if (flag) {
                    HashMap<String, Object> m = data.get(position);
                    String userIdString = (String) m.get(ID);
                    int idUser = Integer.valueOf(userIdString).intValue();
                    dataForRequest.remove((Object) idUser);
                } else {
                    HashMap<String, Object> m = data.get(position);
                    String userIdString = (String) m.get(ID);
                    int idUser = Integer.valueOf(userIdString).intValue();
                    dataForRequest.add(idUser);
                }
                data.get((int) id).put(CHECKED, !flag);
                simpleAdapter.notifyDataSetChanged();
            }
        });

        lvMyFriends.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                String url = VK_LINK+currentDataWithInfo.get((int)l).getId();
                Uri adress = Uri.parse(url);
                Intent openLink = new Intent(Intent.ACTION_VIEW,adress);
                startActivity(openLink);
                return false;
            }
        });

        data = new ArrayList<HashMap<String, Object>>();
        dataBitmap = new ArrayList<Bitmap>();
        dataForRequest = new ArrayList<Integer>();



        if (savedInstanceState==null) {
            tvYourFriends.setVisibility(View.INVISIBLE);
            buttonFindFromMyFriends.setVisibility(View.INVISIBLE);
            FindFromMyFriendsAsyncTask asyncTask = new FindFromMyFriendsAsyncTask();
            asyncTask.execute();
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.trynaGetFriends),Toast.LENGTH_LONG).show();
        } else {
            ContainerArrayList containerArrayList = (ContainerArrayList) savedInstanceState.getSerializable(CURRENT_DATA);
            currentDataWithInfo = containerArrayList.getData();
            if (currentDataWithInfo != null) {
                data = new ArrayList<HashMap<String, Object>>();
                for (int i = 0; i < currentDataWithInfo.size(); i++) {
                    HashMap<String, Object> m = new HashMap<String, Object>();
                    m.put(ID, currentDataWithInfo.get(i).getId());
                    m.put(LAST_NAME, currentDataWithInfo.get(i).getSurname());
                    m.put(FIRST_NAME, currentDataWithInfo.get(i).getName());
                    m.put(CHECKED, false);
                    data.add(m);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        simpleAdapter = new ExtendedSimpleAdapter(getApplicationContext(), data, R.layout.item_for_list, from, to);
                        simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        lvMyFriends.setAdapter(simpleAdapter);
                        int count = data.size();
                        if (savedInstanceState.getBoolean(COMMON_OR_NOT)) {
                            String str = count + " ";
                            if ((count % 100) == 1) {
                                str = str + getResources().getString(R.string.commonFriend);
                            } else if (((count % 100) < 5) && ((count % 100) > 1)) {
                                str = str + getResources().getString(R.string.commonFriendsLessThan10);
                            } else {
                                str = str + getResources().getString(R.string.commonFriends);
                            }
                            tvYourFriends.setText(str);
                        } else {
                            tvYourFriends.setText(getResources().getString(R.string.yourFriends));
                        }
                        progressBarMyFriendsList.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }

    }


    @Override
    public void onBackPressed()
    {
        if (backStack.size()==0)
        {
            startActivity(new Intent(this, MainActivity.class));
        } else
        {
            data = backStack.pop();
            for (int i = 0; i<data.size(); i++)
            {
                data.get(i).put(CHECKED,false);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    simpleAdapter = new ExtendedSimpleAdapter(getApplicationContext(),data,R.layout.item_for_list,from,to);
                    simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    lvMyFriends.setAdapter(simpleAdapter);
                    int count = data.size();
                    if (backStack.size()>=1)
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

        dataOfIds = new ArrayList<ArrayList<User>>();

        if (dataForRequest.size()>1)
        {
            lvMyFriends.setVisibility(View.INVISIBLE);
            progressBarMyFriendsList.setVisibility(View.VISIBLE);
            final ArrayList<Integer> commonFriendsId = new ArrayList<Integer>();
            for (int i = 0; i<dataForRequest.size(); i++)
            {

                final int tmp = (Integer)dataForRequest.get(i).intValue();
                VKRequest request = new VKRequest("friends.get",VKParameters.from("user_id",tmp, VKApiConst.FIELDS,"id,first_name,last_name,photo_50"));
                final int finalI = i;
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
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
                                String photoUrl = object.getString(PHOTO_50);
                                User user = new User(name,surname,photoUrl,id);
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

                    @Override
                    public void onError(VKError error) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.smthIsWrond),Toast.LENGTH_LONG).show();
                        lvMyFriends.setVisibility(View.VISIBLE);
                        progressBarMyFriendsList.setVisibility(View.INVISIBLE);
                        tvYourFriends.setVisibility(View.VISIBLE);
                        buttonFindFromMyFriends.setVisibility(View.VISIBLE);
                    }


                });
            }


        }  else
        {
            Toast.makeText(getApplicationContext(),getString(R.string.notEnoughFriends),Toast.LENGTH_LONG).show();
        }

    }

    private void getCommonFriends (ArrayList<ArrayList<User>> dataToFind)
    {
        ArrayList<User> dataToPrint = dataToFind.get(0);

        for (int i = 1; i<dataToFind.size(); i++) //in list of lists of ids
        {
            ArrayList<User> currentListOfIds = dataToFind.get(i);
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

        currentDataWithInfo = dataToPrint;
        backStack.push(data);
        data = new ArrayList<HashMap<String,Object>>();
        ArrayList<StringUrlWrapper> stringUrlWrapperArrayList = new ArrayList<StringUrlWrapper>();
        for (int i = 0; i<dataToPrint.size(); i++)
        {
            HashMap<String, Object> m = new HashMap<String, Object>();
            m.put(ID,dataToPrint.get(i).getId());
            m.put(LAST_NAME, dataToPrint.get(i).getSurname());
            m.put(FIRST_NAME,dataToPrint.get(i).getName());
            m.put(CHECKED,false);
            data.add(m);
            stringUrlWrapperArrayList.add(new StringUrlWrapper(dataToPrint.get(i).getPhoto(),i));
        }
        WrapperAsyncDownload asyncDownload = new WrapperAsyncDownload();
        asyncDownload.execute(stringUrlWrapperArrayList);

    }

    private class WrapperAsyncDownload extends AsyncTask<ArrayList<StringUrlWrapper>, Void, Void>
    {
        @Override
        protected Void doInBackground(ArrayList<StringUrlWrapper>... arrayLists) {
            DownloadPhotoAsync async = new DownloadPhotoAsync();
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
            {
                async.executeOnExecutor(THREAD_POOL_EXECUTOR,arrayLists[0]);
            } else
            {
                async.execute(arrayLists[0]);
            }
            SIZE_OF_LIST=0;
            while (data.size()>SIZE_OF_LIST){}
            for (int i = 0; i<data.size(); i++)
            {
                data.get(i).put(PHOTO_50,dataBitmap.get(i));
            }
            dataForRequest.clear();
            return null;
        }

        @Override
        protected void onPostExecute(Void params)
        {
            simpleAdapter = new ExtendedSimpleAdapter(getApplicationContext(),data,R.layout.item_for_list,from,to);
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
            lvMyFriends.setVisibility(View.VISIBLE);
            progressBarMyFriendsList.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(CURRENT_DATA, new ContainerArrayList(currentDataWithInfo));
        if (tvYourFriends.getText().toString().equals(getResources().getString(R.string.yourFriends)))
        {
            savedInstanceState.putBoolean(COMMON_OR_NOT, false);
        } else
        {
            savedInstanceState.putBoolean(COMMON_OR_NOT,true);
        }

        super.onSaveInstanceState(savedInstanceState);
    }


    private class FindFromMyFriendsAsyncTask extends AsyncTask<Void,Void,ArrayList<User>>
    {
        @Override
        protected ArrayList<User> doInBackground(Void... voids) {
            SIZE_OF_LIST=0;
            final ArrayList<User> dataFromJson = new ArrayList<User>();
            final boolean[] trigger = {false};
            VKRequest request = new VKRequest("friends.get", VKParameters.from(VKApiConst.FIELDS,"id,first_name,last_name,photo_50"));
            request.secure=false;
            request.attempts=3;
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                    if (attemptNumber<totalAttempts)
                    {
                        Toast.makeText(getApplicationContext(),getString(R.string.smthIsWrongWeTryna),Toast.LENGTH_LONG).show();
                    } else
                    {
                        Toast.makeText(getApplicationContext(),getString(R.string.tryOnceAgain),Toast.LENGTH_LONG).show();
                        trigger[0]=true;
                    }
                }
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    Toast.makeText(getApplicationContext(),"Получено, работаем",Toast.LENGTH_SHORT).show();
                    JSONObject jsonObject = response.json;
                    try {
                        JSONObject jsonObject1 = jsonObject.getJSONObject(RESPONSE);
                        JSONArray jsonArray = jsonObject1.getJSONArray(ITEMS);
                        ArrayList<StringUrlWrapper> stringUrlWrapperArrayList = new ArrayList<StringUrlWrapper>();
                        for (int i = 0; i<jsonArray.length(); i++)
                        {
                            JSONObject tmp = jsonArray.getJSONObject(i);
                            String name = tmp.getString(FIRST_NAME);
                            String surname = tmp.getString(LAST_NAME);
                            String id = tmp.getString(ID);
                            String photo_50 = tmp.getString(PHOTO_50);
                            User user = new User(name,surname,id);
                            stringUrlWrapperArrayList.add(new StringUrlWrapper(photo_50,i));
                            dataFromJson.add(user);
                        }
                        DownloadPhotoAsync async = new DownloadPhotoAsync();
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                        {
                            async.executeOnExecutor(THREAD_POOL_EXECUTOR,stringUrlWrapperArrayList);
                        } else
                        {
                            async.execute(stringUrlWrapperArrayList);
                        }

                        trigger[0] = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            while (!trigger[0]){}
            Log.d("size from json",dataFromJson.size()+"");
            Log.d("size of list",SIZE_OF_LIST+"");
            while (SIZE_OF_LIST<dataFromJson.size()){}
            return dataFromJson;
        }

        @Override
        protected void onPostExecute(ArrayList<User> inData)
        {
            if (inData.size()==0)
            {
                progressBarMyFriendsList.setVisibility(View.INVISIBLE);
                tvYourFriends.setVisibility(View.VISIBLE);
                buttonFindFromMyFriends.setVisibility(View.VISIBLE);
                return;
            }
            currentDataWithInfo = inData;
            data = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i<inData.size(); i++)
            {
                HashMap<String,Object> tmp = new HashMap<String,Object>();
                tmp.put(FIRST_NAME,inData.get(i).getName());
                tmp.put(LAST_NAME,inData.get(i).getSurname());
                tmp.put(ID,inData.get(i).getId());
                tmp.put(CHECKED, false);
                tmp.put(PHOTO_50,dataBitmap.get(i));
                data.add(tmp);
            }
            if (simpleAdapter==null)
            {
                simpleAdapter = new ExtendedSimpleAdapter(getApplicationContext(),data,R.layout.item_for_list,from,to);
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

    private class DownloadPhotoAsync extends AsyncTask<ArrayList<StringUrlWrapper>,Void,Void>
    {

        @Override
        protected Void doInBackground(ArrayList<StringUrlWrapper>... arrayLists) {
            try {
                ArrayList<StringUrlWrapper> list = arrayLists[0];
                SIZE_OF_LIST=0;
                for (int i = 0; i<list.size(); i++) {
                    URL url = new URL(list.get(i).getUrl());
                    int position = list.get(i).getPosition();
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    dataBitmap.add(position, bitmap);
                    SIZE_OF_LIST++;

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}
