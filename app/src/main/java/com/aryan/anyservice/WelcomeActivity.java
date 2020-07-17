package com.aryan.anyservice;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ImageView imageView=findViewById(R.id.logo);
        LinearLayout linearLayout=findViewById(R.id.welcome_layout);
        slideUp(imageView,linearLayout,null);
        Boolean result = getIntent().getBooleanExtra("result",false);
        if(result){
            Button button = findViewById(R.id.ok_button);
            button.setEnabled(true);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(WelcomeActivity.this,HomeActivity.class);
                    finish();
                    startActivity(intent);

                }
            });

        }else {
            String msg = getIntent().getStringExtra("msg");
            TextView tv = findViewById(R.id.pending_textview);
            tv.setText(msg+"\nWhile we are checking the same.....\nPlease check after few minutes.");
        }
    }
    // slide the view from its current position to below itself
    public void slideUp(final View view, final View llDomestic, final View GONEView){
        final ImageView logoImageView = findViewById(R.id.logo);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if(GONEView!=null) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(GONEView, "translationY", metrics.heightPixels / 3 - logoImageView.getHeight() / 3);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(1000);
            animation.start();
            GONEView.getLayoutParams().height = metrics.heightPixels / 3 - logoImageView.getHeight() / 3;
            GONEView.requestLayout();
            GONEView.setVisibility(View.GONE);
        }


//        Toast.makeText(getApplicationContext(),""+view.getTranslationX()+"\n"+view.getHeight()+"\n"+view.getBaseline(),Toast.LENGTH_LONG).show();
        if(view!=null) {
            ObjectAnimator animation2 = ObjectAnimator.ofFloat(view, "translationY", logoImageView.getHeight() / 3 - metrics.heightPixels / 3);
            animation2.setInterpolator(new AccelerateDecelerateInterpolator());
            animation2.setDuration(1000);
            animation2.start();
            view.getLayoutParams().height = logoImageView.getHeight() / 3 - metrics.heightPixels / 3;
            view.requestLayout();
        }
        if(llDomestic!=null) {
            ObjectAnimator animation2 = ObjectAnimator.ofFloat(llDomestic, "translationY", logoImageView.getHeight() / 3 - metrics.heightPixels / 3);
            animation2.setInterpolator(new AccelerateDecelerateInterpolator());
            animation2.setDuration(1000);
            animation2.start();
            llDomestic.getLayoutParams().height = logoImageView.getHeight() / 3 - metrics.heightPixels / 3;
            llDomestic.requestLayout();
            llDomestic.setVisibility(View.VISIBLE);
        }

    }
}