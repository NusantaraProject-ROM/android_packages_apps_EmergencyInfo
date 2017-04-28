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
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.emergency.EmergencyTabActivity;
import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.overlay.FeatureFactory;
import com.android.emergency.view.ViewInfoActivity;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.ArrayList;

/**
 * Activity for editing emergency information.
 */
public class EditInfoActivity extends EmergencyTabActivity {
    static final String TAG_CLEAR_ALL_DIALOG = "clear_all_dialog";
    private static final String ACTION_EDIT_EMERGENCY_CONTACTS =
            "android.emergency.EDIT_EMERGENCY_CONTACTS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Protect against b/28401242 by enabling ViewInfoActivity.
        // We used to have code that disabled/enabled it and it could have been left in disabled
        // state.
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, ViewInfoActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

        setContentView(R.layout.edit_activity_layout);
        if (ACTION_EDIT_EMERGENCY_CONTACTS.equals(getIntent().getAction())) {
            // Select emergency contacts tab
            selectTab(1);
        }

        getWindow().addFlags(FLAG_DISMISS_KEYGUARD);
        MetricsLogger.visible(this, MetricsEvent.ACTION_EDIT_EMERGENCY_INFO);
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
    protected ArrayList<Pair<String, Fragment>> setUpFragments() {
        FeatureFactory featureFactory = FeatureFactory.getFactory(this);

        // Always return the two fragments in edit mode.
        ArrayList<Pair<String, Fragment>> fragments = new ArrayList<>(2);
        fragments.add(Pair.create(getResources().getString(R.string.tab_title_info),
                EditEmergencyInfoFragment.newInstance()));
        fragments.add(Pair.create(getResources().getString(R.string.tab_title_contacts),
                featureFactory.getEmergencyContactsFeatureProvider().createEditContactsFragment()));
        return fragments;
    }

    private void showClearAllDialog() {
        final ClearAllDialogFragment previousFragment =
                (ClearAllDialogFragment) getFragmentManager()
                        .findFragmentByTag(EditInfoActivity.TAG_CLEAR_ALL_DIALOG);
        if (previousFragment == null) {
            DialogFragment newFragment = ClearAllDialogFragment.newInstance();
            newFragment.show(getFragmentManager(), TAG_CLEAR_ALL_DIALOG);
        }
    }

    private void onClearAllPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        for (String key : PreferenceKeys.KEYS_EDIT_EMERGENCY_INFO) {
            sharedPreferences.edit().remove(key).commit();
        }
        sharedPreferences.edit().remove(PreferenceKeys.KEY_EMERGENCY_CONTACTS).commit();

        // Refresh the UI.
        ViewPagerAdapter adapter = getTabsAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
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
