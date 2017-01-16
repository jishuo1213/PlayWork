package com.inspur.playwork.view.common.progressbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.playwork.R;

/**
 * Created by Fan on 16-2-18.
 */
public class TipDialog extends DialogFragment implements View.OnClickListener {


    private TipDialogEvents eventListener;

    public interface TipDialogEvents {
        void onConfrimClick();

        void onCancelClick();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.normal_dialog);
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.layout_resend_message, null);
        dialog.setContentView(v);
        TextView confrimView = (TextView) v.findViewById(R.id.tv_confrim);
        TextView cancelView = (TextView) v.findViewById(R.id.tv_cancel);
        confrimView.setOnClickListener(this);
        cancelView.setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confrim:
                eventListener.onConfrimClick();
                break;
            case R.id.tv_cancel:
                eventListener.onCancelClick();
                break;
        }
    }
}
