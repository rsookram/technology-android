package com.merono.g;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class ChooseBoardDialogFragment extends DialogFragment {

	EditText mEditText;

	public static ChooseBoardDialogFragment newInstance(String title) {
		ChooseBoardDialogFragment frag = new ChooseBoardDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString("title");

		mEditText = new EditText(getActivity());
		mEditText.setInputType(InputType.TYPE_CLASS_TEXT); // force one-line

		final DialogFragment frag = this;
		mEditText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							((GActivity) getActivity()).switchBoard(mEditText
									.getText().toString());
							frag.dismiss();
						}
						return false;
					}
				});

		return new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setView(mEditText)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface d, int button) {
								((GActivity) getActivity())
										.switchBoard(mEditText.getText()
												.toString());
							}
						}).create();
	}

	@Override
	public void onStart() {
		super.onStart();

		// make the keyboard appear when dialog appears (only in portrait)
		final Window window = this.getDialog().getWindow();
		mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});
	}
}
