package com.grum.raphael.projectmanagerclient.com.grum.raphael.projectmanagerclient.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.*;
import android.widget.TextView;


import com.grum.raphael.projectmanagerclient.MainActivity;
import com.grum.raphael.projectmanagerclient.R;
import com.grum.raphael.projectmanagerclient.tasks.GetTeamsTask;
import com.grum.raphael.projectmanagerclient.tasks.TeamTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SearchAllTeamsFragment extends Fragment {

    private ListView list;
    private EditText search;
    private String[] teamNames;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_search_all_teams, container, false);
        list = (ListView) rootView.findViewById(R.id.list_teams);
        search = (EditText) rootView.findViewById(R.id.filter_teams);
        GetTeamsTask getTeams = new GetTeamsTask(getActivity());
        try {
            JSONObject fetchedData = getTeams.execute(new String[]{MainActivity.URL + "teams",
                    MainActivity.userData.getToken()}).get();
            String success = fetchedData.getString("success");
            if (success.equals("true")) {
                String fetchedTeams = fetchedData.getString("teams");
                fetchedTeams = fetchedTeams.substring(1, fetchedTeams.length() - 1);
                if (!fetchedTeams.equals("")) {
                    teamNames = fetchedTeams.split(",");
                    int length = teamNames.length;
                    if (teamNames.length != 0) {
                        arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item,
                                R.id.list_element, teamNames);
                        list.setAdapter(arrayAdapter);
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String teamName = (String) list.getAdapter().getItem(position);
                                String url = MainActivity.URL + "team";
                                String token = MainActivity.userData.getToken();
                                String[] params = new String[]{token, url, teamName};
                                TeamTask teamTask = new TeamTask();
                                try {
                                    JSONObject fetchedTeamTask = teamTask.execute(params).get();
                                    dealWithResponse(fetchedTeamTask, rootView);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        search.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                arrayAdapter.getFilter().filter(s);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    } else {
                        // TODO
                    }
                } else {
                    // TODO
                }
            } else {
                // TODO
            }
        } catch (InterruptedException e) {
            // TODO
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO
            e.printStackTrace();
        }
        return rootView;
    }

    private void dealWithResponse(JSONObject data, View anchorView) {
        try {
            String success = data.getString("success");
            if (success.equals("true")) {
                JSONObject team = new JSONObject(data.getString("team").toString());
                HashMap<String, String> teamData = getTeamData(team);
                String teamName = teamData.get("name");
                String description = teamData.get("description");
                String admin = teamData.get("admin");
                View view
                        = this.getLayoutInflater(null).inflate(R.layout.fragment_popup_team, null);
                setValuesToPopupWindow(teamData, view);
                final PopupWindow popupWindow = new PopupWindow(view,
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int width = anchorView.getWidth();
                int height = anchorView.getHeight();
                popupWindow.setWidth((int) anchorView.getWidth());
                popupWindow.setHeight((int) anchorView.getHeight());
                popupWindow.setBackgroundDrawable(new BitmapDrawable(getContext().getResources(),
                        (Bitmap) null));
                Button closeBtn = (Button) view.findViewById(R.id.close_btn_team);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                // Show popup
                int[] location = new int[2];
                anchorView.getLocationOnScreen(location);
                popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
            } else {
                // TODO
            }
        } catch (JSONException e) {
            // TODO
            e.printStackTrace();
        }
    }

    private void setValuesToPopupWindow(HashMap<String, String> teamData, View view) {
        // Fetch all items within the PopupWindow
        TextView teamName = (TextView) view.findViewById(R.id.popup_team_name);
        TextView description = (TextView) view.findViewById(R.id.popup_team_description);
        TextView admin = (TextView) view.findViewById(R.id.popup_team_admin);

        // Get the values from HashMap
        String name = teamData.get("name");
        String teamDescription = teamData.get("description");
        String teamAdmin = teamData.get("admin");

        // Set values to items
        teamName.setText(name);
        description.setText(teamDescription);
        admin.setText(teamAdmin);

    }

    private HashMap<String, String> getTeamData(JSONObject teamData) {
        HashMap<String, String> result = new HashMap<>();
        try {
            result.put("name", teamData.getString("name"));
            result.put("description", teamData.getString("description"));
            result.put("admin", teamData.getString("admin"));
        } catch (JSONException e) {
            // TODO
            e.printStackTrace();
        }
        return result;
    }


}