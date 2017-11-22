package com.tyc129.nfcmap;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Window;
import android.widget.ImageView;

/**
 * Created by Code on 2017/10/23 0023.
 *
 * @author 谈永成
 * @version 1.0
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setExitTransition(new Fade());
        startActivity(new Intent(this, MainActivity.class),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        finishAfterTransition();
    }
}
