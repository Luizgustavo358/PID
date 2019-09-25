package com.example.pid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {


    Animation hideLayout;
    Animation hideShadow;

    Animation showLayout;
    Animation showShadow;
    private FloatingActionButton fabPhoto;
    private View shadowView;
    private LinearLayout subFab;
    private LinearLayout option1Layout;
    private LinearLayout option2Layout;
    private LinearLayout option3Layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();

        initializeViews();
        subFab.setVisibility(View.GONE);
        setFabAnimations();
        setListeners();

    }


    /**
     * Set the activity listener behaviours.
     */
    private void setListeners() {
        option1Layout.setOnClickListener(view ->
            Toast.makeText(this, "Option 1", Toast.LENGTH_SHORT).show()
        );

        option2Layout.setOnClickListener(view ->
            Toast.makeText(this, "Option 2", Toast.LENGTH_SHORT).show()
        );

        option3Layout.setOnClickListener(view ->
            Toast.makeText(this, "Option 3", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Initializes the views of the activity
     */
    private void initializeViews() {
        subFab = findViewById(R.id.ls_layout);
        shadowView = findViewById(R.id.shadowView);
        option1Layout = findViewById(R.id.option1);
        option2Layout = findViewById(R.id.option2);
        option3Layout = findViewById(R.id.option3);
        fabPhoto = findViewById(R.id.fab_photo);
    }



    /**
     * Set Floating Action button animations.
     */
    private void setFabAnimations() {
        hideLayout = AnimationUtils.loadAnimation(this, R.anim.hide_layout);
        hideFabAnimation(hideLayout);
        hideShadow = AnimationUtils.loadAnimation(this, R.anim.hide_shadow);
        hideShadowAnimation(hideShadow);

        showLayout = AnimationUtils.loadAnimation(this, R.anim.show_layout);
        showShadow = AnimationUtils.loadAnimation(this, R.anim.show_shadow);

        fabPhoto.setOnClickListener(toggleFab());

        shadowView.setOnClickListener(setShadowViewClick());
    }


    /**
     * Starts the animations when clicked on the shadow view.
     *
     * @return lambda with the animations started if shadowView is visible
     */
    private View.OnClickListener setShadowViewClick() {
        return v -> {
            if (shadowView.getVisibility() == View.VISIBLE) {
                shadowView.startAnimation(hideShadow);
                subFab.startAnimation(hideLayout);
            }
        };
    }

    /**
     * Set animation listener of the fab
     *
     * @param hideShadow is the animation used on the shadowView
     */
    private void hideShadowAnimation(Animation hideShadow) {
        hideShadow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                shadowView.clearAnimation();
                shadowView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * Creates an onClickListener that toggles the floating action button between being visible or not.
     *
     * @return a click listener view
     */
    private View.OnClickListener toggleFab() {
        return v -> {
            if (subFab.getVisibility() == View.VISIBLE) {
                shadowView.startAnimation(hideShadow);
                subFab.startAnimation(hideLayout);
            } else {
                subFab.setVisibility(View.VISIBLE);
                shadowView.setVisibility(View.VISIBLE);
                shadowView.startAnimation(showShadow);
                subFab.startAnimation(showLayout);
            }
        };
    }

    /**
     * Set animation listener of the fab
     *
     * @param hideLayout is the animation used on the shadowView
     */
    private void hideFabAnimation(Animation hideLayout) {
        hideLayout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                subFab.clearAnimation();
                subFab.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
