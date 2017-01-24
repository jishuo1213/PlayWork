package com.inspur.playwork.view.common.chosepicture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.inspur.playwork.R;
import com.inspur.playwork.view.common.BaseActivity;

public class ChoseImageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Glide.with(this).load("").into((ImageView) findViewById(R.id.image_font_size)).
        setContentView(R.layout.layout_chose_image);
        ChosePictureFragment fragment = new ChosePictureFragment();
        getFragmentManager().beginTransaction().add(R.id.chose_image_container, fragment).commit();
    }
}
