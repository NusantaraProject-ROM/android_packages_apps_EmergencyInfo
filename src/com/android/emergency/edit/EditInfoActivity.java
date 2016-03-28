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
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;

import com.android.emergency.EmergencyTabActivity;
import com.android.emergency.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.util.ArrayList;

/**
 * Activity for editing emergency information.
 */
public class EditInfoActivity extends EmergencyTabActivity {
    private static final String TAG_WARNING_DIALOG = "warning_dialog";
    private static final String KEY_LAST_CONSENT_TIME_MS = "last_consent_time_ms";
    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;

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

        getWindow().addFlags(FLAG_DISMISS_KEYGUARD);
        MetricsLogger.visible(this, MetricsEvent.ACTION_EDIT_EMERGENCY_INFO);
    }

    @Override
    public boolean isInViewMode() {
        return false;
    }

    @Override
    public String getActivityTitle() {
        return getString(R.string.edit_emergency_info_label);
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
}
