package com.merono.g;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

public class PagePickerDialogFragment extends DialogFragment {
	private NumberPicker mPagePicker;

	public static PagePickerDialogFragment newInstance(int currentPage) {
		PagePickerDialogFragment frag = new PagePickerDialogFragment();
		Bundle args = new Bundle();
		args.putInt("page", currentPage);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int pageNum;
		if (savedInstanceState == null) {
			pageNum = getArguments().getInt("page");
		} else {
			pageNum = savedInstanceState.getInt("saved_page");
		}

		mPagePicker = new NumberPicker(getActivity());
		mPagePicker.setMinValue(0);
		mPagePicker.setMaxValue(10);
		mPagePicker.setValue(pageNum);

		return new AlertDialog.Builder(getActivity())
				.setTitle("Choose Page")
				.setView(mPagePicker)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface d, int button) {
								((GActivity) getActivity()).refresh(mPagePicker
										.getValue());
							}
						}).create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("saved_page", mPagePicker.getValue());
		super.onSaveInstanceState(outState);
	}
}
