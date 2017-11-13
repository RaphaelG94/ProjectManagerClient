package com.grum.raphael.projectmanagerclient.tasks;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;

/**
 * Created by Raphael on 13.11.2017.
 */

public class DeleteAppointmentTask extends AsyncTask<String, Void, JSONObject> {

    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject result;
        String url = params[0];
        String token = params[1];
        String projectName = params[2];
        String teamName = params[3];
        String username = params[4];
        String appointmentId = params[5];
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost deleteAppointmentRequest = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(createRequestData(token, projectName, teamName,
                username, appointmentId), "UTF-8");
        stringEntity.setContentType("application/json");
        deleteAppointmentRequest.setEntity(stringEntity);
        try {
            HttpResponse response = client.execute(deleteAppointmentRequest);
            InputStream input = response.getEntity().getContent();
            if (input != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder stringBuilder = new StringBuilder();
                String temp;
                while ((temp = reader.readLine()) != null) {
                    stringBuilder.append(temp);
                }
                result = new JSONObject(stringBuilder.toString());
            } else {
                // TODO
                result = null;
            }
        } catch (IOException e) {
            // TODO
            result = null;
        } catch (JSONException e) {
            // TODO
            result = null;
            e.printStackTrace();
        }
        return result;
    }

    private String createRequestData(String token, String projectName, String teamName,
                                     String username, String appointmentId) {
        return "{\"token\": \"" + token + "\", \"projectName\": \"" + projectName + "\", " +
                "\"teamName\": \"" + teamName + "\", \"id\": \"" + appointmentId + "\", " +
                "\"username\": \"" + username + "\"}";
    }

}
