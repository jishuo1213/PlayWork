package com.inspur.playwork.view.profile.my;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.CommonUtils;

public class AboutFragment extends Fragment implements View.OnClickListener{

    private TextView textView;
    private ImageButton backImageView;
    private TextView titleTextView;
    private TextView textView3 ;
    private View rootView ;

    public static final String TAG = "AboutFragment" ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView ==null){
            rootView = inflater.inflate(R.layout.activity_about,container,false);
        }
        initView(rootView);
        return rootView;
    }

    private void initView(View view){
        backImageView = (ImageButton) view.findViewById(R.id.iv_left);
        titleTextView = (TextView) view.findViewById(R.id.tv_title);
        textView = (TextView) view.findViewById(R.id.textView);
        textView3 = (TextView) view.findViewById(R.id.textView3);

        String version = null;
        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ;
        textView.setText("V "+version);
        titleTextView.setText("关于");
        backImageView.setVisibility(View.VISIBLE);
        backImageView.setOnClickListener(this);
        textView3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                CommonUtils.back();
                break;
            case R.id.textView3:
                String tel = textView3.getText().toString().substring(5);
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel));
                getActivity().startActivity(intent);
                break;

        }
    }
}
