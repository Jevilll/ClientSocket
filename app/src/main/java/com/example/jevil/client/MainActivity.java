package com.example.jevil.client;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String host = "192.168.1.73";
    int port = 23;

    // формирую JSon
    JSONObject jsonExample;

    TextView tvId, tvSecurity, tvFire;
    LinearLayout ll;
    EditText etIp, etPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.btnSend);
        btn.setOnClickListener(this);

        tvId = (TextView) findViewById(R.id.tvId);
        tvSecurity = (TextView) findViewById(R.id.tvSecurity);
        tvFire =(TextView) findViewById(R.id.tvFire);
        etIp = (EditText) findViewById(R.id.etIp);
        etPort = (EditText) findViewById(R.id.etPort);
        ll = (LinearLayout) findViewById(R.id.ll);

        try {
            // формирую JSon
            jsonExample = new JSONObject();
            jsonExample.put("token", "81fcd0cf0b76d6d2c796715caec1a935");
            jsonExample.put("operation", "check");
            jsonExample.put("device", "security");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {

        // поток для получения сообщений с сервера
        GetDataFromServer getter = new GetDataFromServer();
        getter.execute();

    }

    class GetDataFromServer extends AsyncTask<Void, Void, Model> {
        char[] mData;

        @Override
        protected Model doInBackground(Void... voids) {
            Model nestedObjects;
            Model commonModel = null;
            mData = new char[1024];
            try {
                // ip адрес сервера
                InetAddress ipAddress = InetAddress.getByName(etIp.getText().toString());
                // Создаем сокет
                Socket socket = new Socket(ipAddress, Integer.valueOf(etPort.getText().toString()));

                // Получаем потоки ввод/вывода
                OutputStream outputStream = socket.getOutputStream();

                DataOutputStream out = new DataOutputStream(outputStream);

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    while (!isCancelled()) {
                        out.write(jsonExample.toString().getBytes());
                    if (reader.read(mData) > 0) {

                        String prop = String.valueOf(mData);
                        prop = prop.replace("\u0000", ""); // removes NUL chars


                        // получение вложенных объектов с использованием TypeAdapterFactory
                        GsonBuilder builder = new GsonBuilder().setLenient();
                        builder.registerTypeAdapterFactory(new ItemTypeAdapterFactory());
                        Gson gson = builder.create();

                        nestedObjects = gson.fromJson(prop, Model.class);
                        ArrayList arrSecurity = (ArrayList) nestedObjects.security;
                        ArrayList arrFire = (ArrayList) nestedObjects.fire;

                        // общий вид модели
                        GsonBuilder builderCommon = new GsonBuilder().setLenient();
                        Gson gsonCommon = builderCommon.create();

                        commonModel = gsonCommon.fromJson(prop, Model.class);
                        commonModel.securitySize = arrSecurity.size();
                        commonModel.fireSize = arrFire.size();
                        onPostExecute(commonModel);
                        reader.close();
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return commonModel;
        }

        protected void onPostExecute(Model result)
        {
            super.onPostExecute(result);
            if (result != null) {
                String str = "Значение sceneid: " + result.sceneid;
                tvId.setText(str);
                str = "Количество символов в массиве security: " + result.securitySize;
                tvSecurity.setText(str);
                str = "Количество символов в массиве fire: " + result.fireSize;
                tvFire.setText(str);
            }
        }
    }





}
