package com.mlkcca.android.sample.milkchat;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;

import android.os.Handler;
import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.mlkcca.client.DataElement;
import com.mlkcca.client.DataElementValue;
import com.mlkcca.client.DataStore;
import com.mlkcca.client.Streaming;
import com.mlkcca.client.StreamingListener;
import com.mlkcca.client.MilkCocoa;
import com.mlkcca.client.DataStoreEventListener;


public class MainActivity extends Activity implements DataStoreEventListener {

    private EditText editText;
    private ArrayAdapter<String> adapter;
    private MilkCocoa milkcocoa;
    private Handler handler = new Handler();
    private DataStore messagesDataStore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_main);



        // ListViewの設定
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ListView listView = (ListView)findViewById(R.id.listView1);
        listView.setAdapter(adapter);

        editText = (EditText)findViewById(R.id.editText1);

        connect();
    }

    private void connect() {
        this.milkcocoa = new MilkCocoa("{your-app-id}.mlkcca.com");
        this.messagesDataStore = this.milkcocoa.dataStore("message");
        Streaming stream = this.messagesDataStore.streaming();
        stream.size(25);
        stream.sort("desc");
        stream.addStreamingListener(new StreamingListener() {

                @Override
                public void onData(ArrayList<DataElement> arg0) {
                    final ArrayList<DataElement> messages = arg0;

                    new Thread(new Runnable() {
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    for (int i = 0; i < messages.size(); i++) {
                                        adapter.insert(messages.get(i).getValue("content"), i);
                                    }
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        stream.next();

        this.messagesDataStore.addDataStoreEventListener(this);
        this.messagesDataStore.on("push");
    }

    public void sendEvent(View view){
        if (editText.getText().toString().length() == 0) {
            return;
        }

        DataElementValue params = new DataElementValue();
        params.put("content", editText.getText().toString());
        Date date = new Date();
        params.put("date", date.getTime());
        this.messagesDataStore.push(params);
        editText.setText("");
    }

    @Override
    public void onPushed(DataElement dataElement) {
        final DataElement pushed = dataElement;
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        String content = pushed.getValue("content");
                        adapter.insert(content, 0);
                    }
                });
            }
        }).start();

    }

    @Override
    public void onSetted(DataElement dataElement) {

    }

    @Override
    public void onSended(DataElement dataElement) {

    }

    @Override
    public void onRemoved(DataElement dataElement) {

    }
}

