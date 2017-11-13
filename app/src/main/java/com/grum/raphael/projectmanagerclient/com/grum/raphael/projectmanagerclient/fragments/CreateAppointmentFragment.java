package com.grum.raphael.projectmanagerclient.com.grum.raphael.projectmanagerclient.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.grum.raphael.projectmanagerclient.MainActivity;
import com.grum.raphael.projectmanagerclient.R;
import com.grum.raphael.projectmanagerclient.tasks.CreateAppointmentTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;


public class CreateAppointmentFragment extends Fragment {

    String name;
    String description;
    String deadline;
    String time;
    private EditText appointmentName;
    private EditText appointmentDescription;
    private DatePicker deadlinePicker;
    private TimePicker timePicker;
    private Button createAppointment;
    private TextView info;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_appointment, container, false);
        appointmentName = (EditText) rootView.findViewById(R.id.create_appointment_name);
        appointmentDescription
                = (EditText) rootView.findViewById(R.id.create_appointment_description);
        deadlinePicker = (DatePicker) rootView.findViewById(R.id.create_appointment_deadline);
        timePicker = (TimePicker) rootView.findViewById(R.id.create_appointment_time);
        timePicker.setIs24HourView(true);
        createAppointment = (Button) rootView.findViewById(R.id.btn_create_appointment_rdy);
        info = (TextView) rootView.findViewById(R.id.text_create_appointment_info);
        appointmentName.addTextChangedListener(setUpTextWatcherAppointmentName());
        appointmentDescription.addTextChangedListener(setUpTextWatcherAppointmentDescription());
        Calendar currentCalendar = Calendar.getInstance();
        deadlinePicker.init(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH), setUpOnDateChangedListener());
        timePicker.setOnTimeChangedListener(setTimeChangedListener());
        createAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAppointment();
            }
        });
        return rootView;
    }

    private void createAppointment() {
        info.setText("");
        deadline = concatenateDeadline(deadline, time);
        if (validateInput(name, description)) {
            if (validateDeadline(deadline)) {
                String[] params = new String[] {MainActivity.URL + "create/appointment",
                        MainActivity.userData.getToken(), MainActivity.userData.getAdminOfProject(),
                MainActivity.userData.getTeamName(), name, description, deadline,
                        MainActivity.userData.getUsername()};
                CreateAppointmentTask createAppointmentTask = new CreateAppointmentTask();
                try {
                    JSONObject result = createAppointmentTask.execute(params).get();
                    String success = result.getString("success");
                    if (success.equals("true")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.success)
                                .setMessage("Das Meeting wurde erfolgreich angelegt!")
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        openMeetingsPage();
                                    }
                                })
                                .create();
                        alertDialog.show();
                    } else {
                        String reason = result.getString("reason");
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.error)
                                .setMessage("Das Meeting konnte nicht erstellt werden!\n" + reason)
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
                info.setText(getResources().getString(R.string.error_deadline));
            }
        } else {
        info.setText(getResources().getString(R.string.error_fields_filled_wrong));
        }
    }

    private void openMeetingsPage() {
        Fragment meetingFragment = new AppointmentsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.containerFrame, meetingFragment);
        transaction.commit();
    }

    private boolean validateDeadline(String deadline) {
        boolean result = false;
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        Calendar calendarDeadline = Calendar.getInstance();
        try {
            calendarDeadline.setTime(formatter.parse(deadline));
            if (currentDate.getTime().before(calendarDeadline.getTime())) {
                result = true;
            }
        } catch (ParseException e) {
            // Never reached
        }
        return result;
    }

    private boolean validateInput(String name, String description) {
        boolean result = false;
        if (name != null && !name.equals("") && description != null && !description.equals("")) {
            result = true;
        }
        return result;
    }

    private String concatenateDeadline(String deadline, String time) {
        return deadline + " " + time;
    }

    private TimePicker.OnTimeChangedListener setTimeChangedListener() {
        return new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                String hour;
                String minutes;
                if (minute < 10) {
                    minutes = "0" + minute;
                } else {
                    minutes = "" + minute;
                }
                if (hourOfDay < 10) {
                    hour = "0" + hourOfDay;
                } else {
                    hour = "" + hourOfDay;
                }
                time = hour + ":" + minutes + ":00";
            }
        };
    }

    private DatePicker.OnDateChangedListener setUpOnDateChangedListener() {
        return new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                String mDay;
                String mMonth;
                String mYear;
                if (dayOfMonth < 10) {
                    mDay = "0" + dayOfMonth;
                } else {
                    mDay = "" + dayOfMonth;
                }
                if (monthOfYear < 9) {
                    mMonth = "0" + (monthOfYear + 1);
                } else {
                    mMonth = "" + (monthOfYear + 1);
                }
                mYear = "" + year;
                // The 00:00:00 is for the formatter on server side
                deadline = mDay + "." + mMonth + "." + mYear;
            }
        };
    }

    private TextWatcher setUpTextWatcherAppointmentName() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                name = appointmentName.getText().toString();
            }
        };
    }

    private TextWatcher setUpTextWatcherAppointmentDescription() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                description = appointmentDescription.getText().toString();
            }
        };
    }

}