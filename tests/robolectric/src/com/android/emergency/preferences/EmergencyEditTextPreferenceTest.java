/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.emergency.preferences;

import static com.google.common.truth.Truth.assertThat;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.AttributeSet;
import android.util.Xml;

import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.TestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParser;

/** Unit tests for {@link EmergencyEditTextPreference}. */
@SmallTest
@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class EmergencyEditTextPreferenceTest {
    private EmergencyEditTextPreference mPreference;

    @Before
    public void setUp() {
        XmlPullParser parser = RuntimeEnvironment.application.getResources().getXml(
                R.xml.edit_medical_info);
        mPreference = new EmergencyEditTextPreference(
                RuntimeEnvironment.application, Xml.asAttributeSet(parser));
    }

    @Test
    public void testDefaultProperties() {
        assertThat(mPreference.isEnabled()).isTrue();
        assertThat(mPreference.isPersistent()).isTrue();
        assertThat(mPreference.isSelectable()).isTrue();
        assertThat(mPreference.isNotSet()).isTrue();
    }

    @Test
    public void testSetText() throws Throwable {
        final String medicalConditions = "Asthma";
        mPreference.setText(medicalConditions);

        assertThat(mPreference.getText()).isEqualTo(medicalConditions);
        assertThat(mPreference.getSummary()).isEqualTo(medicalConditions);
    }
}
