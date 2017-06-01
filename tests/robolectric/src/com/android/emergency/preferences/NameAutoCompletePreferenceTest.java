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
import static org.mockito.Mockito.when;

import android.util.Xml;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.AutoCompleteTextView;

import com.android.emergency.PreferenceKeys;
import com.android.emergency.R;
import com.android.emergency.TestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParser;

/** Unit tests for {@link NameAutoCompletePreference}. */
@SmallTest
@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class NameAutoCompletePreferenceTest {
    @Mock private NameAutoCompletePreference.SuggestionProvider mAutoCompleteSuggestionProvider;
    private NameAutoCompletePreference mPreference;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        XmlPullParser parser = RuntimeEnvironment.application.getResources().getXml(
                R.xml.edit_medical_info);
        mPreference = new NameAutoCompletePreference(RuntimeEnvironment.application,
                Xml.asAttributeSet(parser), mAutoCompleteSuggestionProvider);
    }

    @Test
    public void testProperties() {
        assertThat(mPreference).isNotNull();
        assertThat(mPreference.isEnabled()).isTrue();
        assertThat(mPreference.isPersistent()).isTrue();
        assertThat(mPreference.isSelectable()).isTrue();
        assertThat(mPreference.isNotSet()).isTrue();
    }

    @Test
    public void testSetText() throws Throwable {
        final String name = "John";
        mPreference.setText(name);
        assertThat(mPreference.getText()).isEqualTo(name);
        assertThat(mPreference.getSummary()).isEqualTo(name);
    }

    @Test
    public void testGetAutoCompleteTextView() {
        AutoCompleteTextView autoCompleteTextView = mPreference.getAutoCompleteTextView();
        assertThat(autoCompleteTextView).isNotNull();
    }

    @Test
    public void testCreateAutocompleteSuggestions_noNameToSuggest() {
        when(mAutoCompleteSuggestionProvider.hasNameToSuggest()).thenReturn(false);
        assertThat(mPreference.createAutocompleteSuggestions()).isEqualTo(new String[] {});
    }

    @Test
    public void testCreateAutocompleteSuggestions_nameToSuggest() {
        final String name = "Jane";
        when(mAutoCompleteSuggestionProvider.hasNameToSuggest()).thenReturn(true);
        when(mAutoCompleteSuggestionProvider.getNameSuggestion()).thenReturn(name);
        assertThat(mPreference.createAutocompleteSuggestions()).isEqualTo(new String[] {name});
    }
}
