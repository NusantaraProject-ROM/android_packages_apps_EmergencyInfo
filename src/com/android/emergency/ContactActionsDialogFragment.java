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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * A {@link DialogFragment} that displays a list of actions for an emergency contact. Call
 * {@link #setTitle(CharSequence)} and {@link #setDialogActionCallback(DialogActionCallback)}
 * before showing.
 */
public class ContactActionsDialogFragment extends DialogFragment {
    private CharSequence mTitle = null;
    private DialogActionCallback mDialogActionCallback = null;

    /**
     * Sets the title of the dialog.
     */
    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    /**
     * Sets the callbacks to be triggered when dialog options are selected.
     */
    public void setDialogActionCallback(DialogActionCallback dialogActionCallback) {
        mDialogActionCallback = dialogActionCallback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mTitle == null) {
            throw new IllegalArgumentException(
                    "setTitle must be called before showing dialog");
        }
        if (mDialogActionCallback == null) {
            throw new IllegalArgumentException(
                    "setDialogActionCallback must be called before showing dialog");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] choices =
                getContext().getResources().getStringArray(R.array.contact_action_choices);

        builder.setTitle(mTitle).setItems(choices, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    mDialogActionCallback.onContactDelete();
                } else if (which == 1) {
                    mDialogActionCallback.onContactDisplay();
                }
            }
        });
        return builder.create();
    }

    /**
     * Callbacks for actions on a contact. Triggered when options in the dialog are selected.
     */
    public interface DialogActionCallback {
        /**
         * Callback to delete a contact.
         */
        void onContactDelete();

        /**
         * Callback to display a contact.
         */
        void onContactDisplay();
    }
}
