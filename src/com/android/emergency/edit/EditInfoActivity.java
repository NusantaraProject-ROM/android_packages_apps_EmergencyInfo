/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.emergency.edit;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.emergency.EmergencyTabActivity;
import com.android.emergency.R;
import com.android.emergency.view.ViewEmergencyContactsFragment;
import com.android.emergency.view.ViewInfoActivity;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.util.ArrayList;

/**
 * Activity for editing emergency information.
 */
public class EditInfoActivity extends EmergencyTabActivity {
    private static final String TAG_WARNING_DIALOG = "warning_dialog";
    private static final String TAG_CLEAR_ALL_DIALOG = "clear_all_dialog";
    private static final String KEY_LAST_CONSENT_TIME_MS = "last_consent_time_ms";
    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
    private static final String ACTION_EDIT_EMERGENCY_CONTACTS =
            "android.emergency.EDIT_EMERGENCY_CONTACTS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity_layout);

        // savedInstanceState is null on first start and non-null on restart.
        // We want to show the dialog on first start, even if there is a screen rotation but avoid
        // reshowing it if a rotation occurs (which causes onCreate to be called again, but this
        // time with savedInstanceState!=null).
        if (savedInstanceState == null) {
            long lastConsentTimeMs = PreferenceManager.getDefaultSharedPreferences(this)
                    .getLong(KEY_LAST_CONSENT_TIME_MS, Long.MAX_VALUE);
            long nowMs = System.currentTimeMillis();
            // Check if at least one day has gone by since the user last gave his constant or if
            // the last consent was in the future (e.g. if the user changed the date).
            if (nowMs - lastConsentTimeMs > ONE_DAY_MS || lastConsentTimeMs > nowMs) {
                showWarningDialog();
            }
        }

        if (ACTION_EDIT_EMERGENCY_CONTACTS.equals(getIntent().getAction())) {
            // Select emergency contacts tab
            selectTab(1);
        }

        getWindow().addFlags(FLAG_DISMISS_KEYGUARD);
        MetricsLogger.visible(this, MetricsEvent.ACTION_EDIT_EMERGENCY_INFO);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Enable ViewInfoActivity if the user input some info. Otherwise, disable it.
        PackageManager pm = getPackageManager();
        if (ViewEmergencyContactsFragment.hasAtLeastOneEmergencyContact(this)
                || EditEmergencyInfoFragment.hasAtLeastOnePreferenceSet(this)) {
            pm.setComponentEnabledSetting(new ComponentName(this, ViewInfoActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(new ComponentName(this, ViewInfoActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_all:
                showClearAllDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isInViewMode() {
        return false;
    }

    @Override
    protected ArrayList<Pair<String, Fragment>> setUpFragments() {
        // Always return the two fragments in edit mode.
        ArrayList<Pair<String, Fragment>> fragments = new ArrayList<>(2);
        fragments.add(Pair.create(getResources().getString(R.string.tab_title_info),
                EditEmergencyInfoFragment.newInstance()));
        fragments.add(Pair.create(getResources().getString(R.string.tab_title_contacts),
                EditEmergencyContactsFragment.newInstance()));
        return fragments;
    }

    private void showWarningDialog() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment previousDialog = getFragmentManager().findFragmentByTag(TAG_WARNING_DIALOG);
        if (previousDialog != null) {
            ft.remove(previousDialog);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = WarningDialogFragment.newInstance();
        newFragment.setCancelable(false);
        newFragment.show(ft, TAG_WARNING_DIALOG);
    }

    private void showClearAllDialog() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment previousDialog = getFragmentManager().findFragmentByTag(TAG_CLEAR_ALL_DIALOG);
        if (previousDialog != null) {
            ft.remove(previousDialog);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = ClearAllDialogFragment.newInstance();
        newFragment.show(ft, TAG_CLEAR_ALL_DIALOG);
    }
    
    private void onClearAllPreferences() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();

        ArrayList<Pair<String, Fragment>> fragments = getFragments();
        EditEmergencyInfoFragment editEmergencyInfoFragment =
                (EditEmergencyInfoFragment) fragments.get(0).second;
        editEmergencyInfoFragment.reloadFromPreference();
        EditEmergencyContactsFragment editEmergencyContactsFragment =
                (EditEmergencyContactsFragment) fragments.get(1).second;
        editEmergencyContactsFragment.reloadFromPreference();
    }

    /**
     * Warning dialog shown to the user each time they go in to the edit info view. Using a {@link
     * DialogFragment} takes care of screen rotation issues.
     */
    public static class WarningDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.user_emergency_info_title)
                    .setMessage(R.string.user_emergency_info_consent)
                    .setPositiveButton(R.string.emergency_info_continue,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PreferenceManager.getDefaultSharedPreferences(
                                            getActivity()).edit()
                                            .putLong(KEY_LAST_CONSENT_TIME_MS,
                                                    System.currentTimeMillis()).apply();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                }
                            })
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

        public static DialogFragment newInstance() {
            return new WarningDialogFragment();
        }
    }

    /**
     * Dialog shown to the user when they tap on the CLEAR ALL menu item. Using a {@link
     * DialogFragment} takes care of screen rotation issues.
     */
    public static class ClearAllDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.clear_all_message)
                    .setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((EditInfoActivity) getActivity()).onClearAllPreferences();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            return dialog;
        }

        public static DialogFragment newInstance() {
            return new ClearAllDialogFragment();
        }
    }
}
