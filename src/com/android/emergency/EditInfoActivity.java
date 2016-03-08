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
package com.android.emergency;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
/**
 * Activity for editing emergency information.
 */
public class EditInfoActivity extends EmergencyTabPreferenceActivity {
    private static final String FRAGMENT_TAG = "edit_info_fragment";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(FLAG_DISMISS_KEYGUARD);
        MetricsLogger.visible(this, MetricsEvent.ACTION_EDIT_EMERGENCY_INFO);
    }

    @Override
    public boolean isInViewMode() {
        return false;
    }

    @Override
    public void onBackPressed() {
        // If returning to the ViewInfoActivity, then the currently selected tab will be shown.
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_TAB, getSelectedTabPosition());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
