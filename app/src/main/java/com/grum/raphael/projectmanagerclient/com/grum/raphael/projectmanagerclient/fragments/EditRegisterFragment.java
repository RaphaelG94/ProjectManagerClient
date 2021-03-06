package com.grum.raphael.projectmanagerclient.com.grum.raphael.projectmanagerclient.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.grum.raphael.projectmanagerclient.MainActivity;
import com.grum.raphael.projectmanagerclient.R;
import com.grum.raphael.projectmanagerclient.tasks.CheckInternet;
import com.grum.raphael.projectmanagerclient.tasks.DeleteRegisterTask;
import com.grum.raphael.projectmanagerclient.tasks.EditRegisterTask;
import com.grum.raphael.projectmanagerclient.tasks.GetRegisterTask;
import com.grum.raphael.projectmanagerclient.tasks.TeamTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import yuku.ambilwarna.AmbilWarnaDialog;


public class EditRegisterFragment extends Fragment {

    private int defaultColor;
    private String registerName;
    private TextView registerNameView;
    private ImageView editColor;
    private Button editRegister;
    private Button deleteRegister;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_edit_register, container, false);
        Bundle bundle = getArguments();
        registerName = bundle.getString("registerName");
        JSONObject register = getRegister(registerName);
        registerNameView = (TextView) rootView.findViewById(R.id.label_register_name);
        registerNameView.setText(registerName);
        editColor = (ImageView) rootView.findViewById(R.id.edit_color_for_register);
        getColorOfRegister(register);
        editColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPickerDialog(false);
            }
        });
        editRegister = (Button) rootView.findViewById(R.id.btn_edit_register);
        editRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editRegister(registerName, defaultColor);
            }
        });
        deleteRegister = (Button) rootView.findViewById(R.id.btn_delete_register);
        deleteRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRegister();
            }
        });

        return rootView;
    }

    private void deleteRegister() {
        if (CheckInternet.isNetworkAvailable(getContext())) {
            if (MainActivity.userData.getUserRole().equals(MainActivity.ADMIN)) {
                String[] params = new String[]{MainActivity.URL + "delete/register",
                        MainActivity.userData.getToken(), MainActivity.userData.getUsername(),
                        registerName, MainActivity.userData.getTeamName()};
                DeleteRegisterTask deleteRegisterTask = new DeleteRegisterTask();
                try {
                    JSONObject result = deleteRegisterTask.execute(params).get();
                    String success = result.getString("success");
                    if (success.equals("true")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.success)
                                .setMessage("Die Gruppe " + registerName + " wurde erfolgreich gelöscht")
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Fragment newFragment = new RegisterFragment();
                                        FragmentTransaction transaction = getFragmentManager()
                                                .beginTransaction();
                                        transaction.replace(R.id.containerFrame, newFragment);
                                        transaction.commit();
                                    }
                                }).create();
                        alertDialog.show();

                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.error)
                                .setMessage("Die Gruppe konnte nicht gelöscht werden!")
                                .setNegativeButton("OK", null)
                                .create();
                        alertDialog.show();
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
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.error)
                        .setMessage(getResources().getString(R.string.no_rights))
                        .setNegativeButton("OK", null)
                        .create();
                alertDialog.show();
            }
        } else {
            AlertDialog alertDialog = CheckInternet.internetNotAvailable(getActivity());
            alertDialog.show();
        }
    }



    private void getColorOfRegister(JSONObject register) {
        if (register == null) {
            defaultColor = R.color.white;
        } else {
            try {
                defaultColor = register.getInt("color");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String color = "#" + Integer.toHexString(defaultColor);
        editColor.setBackgroundColor(Color.parseColor(color));
    }

    private JSONObject getRegister(String registerName) {
        JSONObject result = new JSONObject();
        if (CheckInternet.isNetworkAvailable(getContext())) {
            String[] params = new String[]{MainActivity.URL + "register",
                    MainActivity.userData.getToken(), registerName, MainActivity.userData.getTeamName()};
            GetRegisterTask getRegisterTask = new GetRegisterTask();
            try {
                JSONObject response = getRegisterTask.execute(params).get();
                String success = response.getString("success");
                if (success.equals("true")) {
                    result = response.getJSONObject("register");
                } else {
                    // TODO
                    result = null;
                }
            } catch (InterruptedException e) {
                // TODO
                result = null;
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO
                result = null;
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO
                result = null;
                e.printStackTrace();
            }
        } else {
            AlertDialog alertDialog = CheckInternet.internetNotAvailable(getActivity());
            alertDialog.show();
        }
        return result;
    }

    private void editRegister(String registerName, int color) {
        if (CheckInternet.isNetworkAvailable(getContext())) {
            if (!registerName.equals(getResources().getString(R.string.blank))) {
                if (MainActivity.userData.getUserRole().equals(MainActivity.ADMIN)) {
                    String[] params = new String[]{MainActivity.URL + "edit/register",
                            MainActivity.userData.getToken(), registerName, "" + color,
                            MainActivity.userData.getTeamName()};
                    if (validateInput(params)) {
                        EditRegisterTask editRegisterTask = new EditRegisterTask();
                        try {
                            JSONObject result = editRegisterTask.execute(params).get();
                            String success = result.getString("success");
                            if (success.equals("true")) {
                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.success)
                                        .setMessage("Die Gruppe " + registerName + " wurde erfolgreich " +
                                                "aktualisiert!")
                                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String[] paramsTeam = new String[]
                                                        {MainActivity.userData.getToken(), MainActivity.URL + "team",
                                                                MainActivity.userData.getTeamName()};
                                                TeamTask teamTask = new TeamTask();
                                                Bundle bundle = new Bundle();
                                                try {
                                                    JSONObject fetchedTeamData = teamTask.execute(paramsTeam).get();
                                                    bundle.putString("teamData", fetchedTeamData.toString());
                                                } catch (InterruptedException e) {
                                                    // TODO
                                                    e.printStackTrace();
                                                } catch (ExecutionException e) {
                                                    // TODO
                                                    e.printStackTrace();
                                                }
                                                Fragment newFragment = new TeamProfileFragment();
                                                newFragment.setArguments(bundle);
                                                FragmentTransaction transaction
                                                        = getFragmentManager().beginTransaction();
                                                transaction.replace(R.id.containerFrame, newFragment);
                                                transaction.commit();
                                            }
                                        })
                                        .create();
                                alertDialog.show();
                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.error)
                                        .setMessage("Die Gruppe konnte nicht erfolgreich editiert werden!")
                                        .setNegativeButton("OK", null)
                                        .create();
                                alertDialog.show();
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
                    } else {
                        Toast.makeText(getContext(), R.string.error_create_team_input,
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage("Sie haben keine Berechtigung für diese Aktion!")
                            .setNegativeButton("OK", null)
                            .create();
                    alertDialog.show();
                }
            } else {
                Toast.makeText(getContext(), R.string.impossible_register_editing,
                        Toast.LENGTH_LONG).show();
            }
        } else {
            AlertDialog alertDialog = CheckInternet.internetNotAvailable(getActivity());
            alertDialog.show();
        }
    }

    private boolean validateInput(String[] params) {
        boolean result = true;
        for (int i = 0; i < params.length; i++) {
            String temp = params[i];
            if (temp == null || temp.equals("")) {
                result = false;
                break;
            }
        }
        return result;
    }

    private void openColorPickerDialog(boolean AlphaSupport) {

        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(getActivity(),
                defaultColor, AlphaSupport, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog ambilWarnaDialog, int color) {
                defaultColor = color;
                editColor.setBackgroundColor(defaultColor);
            }

            @Override
            public void onCancel(AmbilWarnaDialog ambilWarnaDialog) {
                Toast.makeText(getActivity(), "Color Picker Closed", Toast.LENGTH_SHORT).show();
            }
        });
        ambilWarnaDialog.show();

    }
}
