package com.example.auth.app.fragments;
/**
 * Developed for Aalto University course CS-E4300 Network Security.
 * Copyright (C) 2018 Aalto University
 */

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.auth.app.main.FileManager;
import com.example.auth.app.main.MyActivity;
import com.example.auth.app.ulctools.Reader;
import com.example.auth.R;

public class AuthKeyPopup extends DialogFragment {

    private TextView title;
    private TextView bottom_hint;
    private EditText auth_key;
    private Button btn_dialog_proceed;
    private Button btn_dialog_cancel;
    private ImageButton info_popup;

    private String newKeyInfo = MyActivity.outer.getString(R.string.new_key_info);
    private String writeKeyInfo = MyActivity.outer.getString(R.string.write_key_info);

    private Spinner select_key;
    private CheckBox checkBox_auth;
    private TextView current_key;

    private boolean auth = false;

    private int mode;
    private View.OnClickListener btn_dialog_proceed_listener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mode == 2) {
                saveKey(auth_key.getText().toString());
            }
            dismiss();
        }
    };

    private View.OnClickListener btn_dialog_cancel_listener = new View.OnClickListener() {
        public void onClick(View v) {
            dismiss();
        }
    };

    public AuthKeyPopup() {
        // Empty constructor required for DialogFragment
    }

    public static AuthKeyPopup newInstance(int arg) {
        AuthKeyPopup w = new AuthKeyPopup();
        w.setMode(arg);
        return w;
    }

    private void setMode(int arg) {
        this.mode = arg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (mode == 0) {
            view = inflater.inflate(R.layout.key_write_popup, container);
            select_key = view.findViewById(R.id.select_key_spinner);


            String[] values = new String[FileManager.keys.size()];

            for (int i = 0; i < values.length; i++) {
                values[i] = FileManager.keys.get(i).split(",")[1];
            }
            select_key.setAdapter(new ArrayAdapter<String>(MyActivity.outer,
                    R.layout.simple_spinner_item, android.R.id.text1, values));

            current_key = view.findViewById(R.id.current_auth_key);

            checkBox_auth = view.findViewById(R.id.check_box_auth);
            checkBox_auth.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        auth = true;
                        current_key.setText(getString(R.string.key_in_use) + Reader.authKey);
                        current_key.setAlpha(255);
                    } else {
                        auth = false;
                        current_key.setAlpha(0);
                    }

                }
            });

            btn_dialog_proceed = view.findViewById(R.id.btn_dialog_proceed);
            btn_dialog_cancel = view.findViewById(R.id.btn_dialog_cancel);

            btn_dialog_proceed.setOnClickListener(btn_dialog_proceed_listener);
            btn_dialog_cancel.setOnClickListener(btn_dialog_cancel_listener);

            btn_dialog_proceed.setEnabled(true);

        } else {
            view = inflater.inflate(R.layout.key_popup, container);

            bottom_hint = view.findViewById(R.id.bottom_hint);
            auth_key = view.findViewById(R.id.auth_key);


            btn_dialog_proceed = view.findViewById(R.id.btn_dialog_proceed);
            btn_dialog_cancel = view.findViewById(R.id.btn_dialog_cancel);

            btn_dialog_proceed.setOnClickListener(btn_dialog_proceed_listener);
            btn_dialog_cancel.setOnClickListener(btn_dialog_cancel_listener);

            btn_dialog_proceed.setEnabled(false);


            title = view.findViewById(R.id.dialog_title);

            title.setText(getString(R.string.new_key));
            btn_dialog_proceed.setText(getString(R.string.save_key));
            bottom_hint.setText(getString(R.string.key_save_hint));

            auth_key.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable arg0) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (auth_key.getText().toString().length() == 16 && !auth_key.getText().toString().regionMatches(0, "0x", 0, 2)) {
                        btn_dialog_proceed.setEnabled(true);
                    } else if (auth_key.getText().toString().regionMatches(0, "0x", 0, 2) && auth_key.getText().toString().length() == 18) {
                        btn_dialog_proceed.setEnabled(true);
                    } else btn_dialog_proceed.setEnabled(false);
                }
            });
        }
        info_popup = view.findViewById(R.id.info_popup);
        info_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info;
                if (mode == 2) {
                    info = newKeyInfo;
                } else info = writeKeyInfo;
                InfoPopup infoPopup = new InfoPopup();
                infoPopup.setInfoText(info);
                infoPopup.show(MyActivity.fm, "info_popup");
            }
        });

        return view;
    }

    private void saveKey(String key) {
        FileManager.addKey(MyActivity.outer, key);

    }
}
