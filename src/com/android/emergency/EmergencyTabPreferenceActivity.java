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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.design.widget.TabLayout.ViewPagerOnTabSelectedListener;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.emergency.edit.EditEmergencyContactsFragment;
import com.android.emergency.edit.EditEmergencyInfoFragment;
import com.android.emergency.view.ViewEmergencyContactsFragment;
import com.android.emergency.view.ViewEmergencyInfoFragment;

/**
 * A {@link PreferenceActivity} which implements and proxies the necessary calls
 * to be used with AppCompat. It uses a tab layout to separate personal and medical information
 * from emergency contacts.
 */
public abstract class EmergencyTabPreferenceActivity extends Activity {

    private AppCompatDelegate mDelegate;

    protected static final String EXTRA_SELECTED_TAB = "selected_tab";

    public static final int INDEX_INFO_TAB = 0;
    public static final int INDEX_CONTACTS_TAB = 1;
    private static final int NUMBER_TABS = 2;

    private ViewPagerAdapter mTabsAdapter;
    private TabLayout mTabLayout;

    private Fragment[] mFragments = new Fragment[NUMBER_TABS];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupTabs() {
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        if (mTabsAdapter == null) {
            // The viewpager that will host the section contents.
            ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
            mTabsAdapter = new ViewPagerAdapter(getFragmentManager());
            viewPager.setAdapter(mTabsAdapter);
            mTabLayout.setTabsFromPagerAdapter(mTabsAdapter);

            // Set a listener via setOnTabSelectedListener(OnTabSelectedListener) to be notified
            // when any tab's selection state has been changed.
            mTabLayout.setOnTabSelectedListener(
                    new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

            // Use a TabLayout.TabLayoutOnPageChangeListener to forward the scroll and selection
            // changes to this layout
            viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(mTabLayout));
        }

        // Select the same tab that was selected in the Edit/View activity.
        final int selectedTabIndex = getIntent().getIntExtra(EXTRA_SELECTED_TAB, INDEX_INFO_TAB);
        selectTab(selectedTabIndex);
    }

    /** Selects the tab at index {@code selectedTabIndex}. */
    public void selectTab(int selectedTabIndex) {
        TabLayout.Tab tab = mTabLayout.getTabAt(selectedTabIndex);
        tab.select();
    }

    /** Returns the index of the currently selected tab. */
    public int getSelectedTabPosition() {
        return mTabLayout.getSelectedTabPosition();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
        setupTabs();
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button.
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    /** Returns whether the activity is in view mode (true) or in edit mode (false). */
    public abstract boolean isInViewMode();

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    /** The adapter used to handle the two fragments. */
    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mFragments[position] = fragment;
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments[position] = null;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = mFragments[position];
            if (fragment != null) {
                return fragment;
            }
            switch (position) {
                case INDEX_INFO_TAB:
                    if (isInViewMode()) {
                        return ViewEmergencyInfoFragment.newInstance();
                    } else {
                        return EditEmergencyInfoFragment.newInstance();
                    }
                case INDEX_CONTACTS_TAB:
                    if (isInViewMode()) {
                        return ViewEmergencyContactsFragment.newInstance();
                    } else {
                        return EditEmergencyContactsFragment.newInstance();
                    }
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUMBER_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case INDEX_INFO_TAB:
                    return getResources().getString(R.string.tab_title_info);
                case INDEX_CONTACTS_TAB:
                    return getResources().getString(R.string.tab_title_contacts);
            }
            return null;
        }
    }
}
