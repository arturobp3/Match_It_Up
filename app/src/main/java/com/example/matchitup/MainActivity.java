package com.example.matchitup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eftimoff.viewpagertransformers.*;
import com.example.matchitup.game.Game;
import com.example.matchitup.game.GameActivity;
import com.example.matchitup.game.GameFactory;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public static final String START_GAME = "start_game";
    private ViewPager mSlideViewPager;
    private ImageView logo;
    private SliderAdapter sliderAdapter;
    private RelativeLayout slidesLayout, mainLayout;
    private Dialog popUpPlayMenu;
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setAnimation();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocaleManager.setLocale(this,
                this.getSharedPreferences("matchPref", Context.MODE_PRIVATE)
                        .getString("language_key", Locale.getDefault().getLanguage()));
        
        setContentView(R.layout.activity_main);
        popUpPlayMenu = new Dialog(this);
        mainLayout = findViewById(R.id.mainLayout);
        slidesLayout = findViewById(R.id.slidesLayout);
        logo = findViewById(R.id.logo);
        mSlideViewPager = findViewById(R.id.slideViewPager);
        sliderAdapter = new SliderAdapter(this);
        mSlideViewPager.setAdapter(sliderAdapter);
        mSlideViewPager.setPageTransformer(true, new ZoomOutSlideTransformer());
        //Inicia la animación
        handler = new Handler();
        handler.postDelayed(runnable, 2500);

        //Realiza una animación del logo
        YoYo.with(Techniques.Landing).duration(1700).repeat(0).playOn(findViewById(R.id.logo));
    }

    private int convertDpToPx(int dp){
        return Math.round(dp*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }

    private void setAnimation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionManager.beginDelayedTransition(mainLayout);
        }
        logo.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, convertDpToPx(100)));
        slidesLayout.setVisibility(View.VISIBLE);
        logo.setBackgroundResource(R.drawable.gradient_menu);
        logo.setImageResource(R.drawable.logo_lado);
    }

    public void onPlayPressed(){
        popUpPlayMenu.setContentView(R.layout.play_mode_layout);
        popUpPlayMenu.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mSlideViewPager.getCurrentItem();
        popUpPlayMenu.show();
    }

    public void onPlayModePressed(View v){
        Button btnPressed = (Button) v;
        int buttonText = btnPressed.getId();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(START_GAME, buttonText);
        startActivity(intent);
    }

    /*Clase que permite crear el menu con distintas pantallas*/
    private class SliderAdapter extends PagerAdapter {

        public final int[] SLIDE_IMAGES = {
                R.drawable.jugar,
                R.drawable.dict,
                R.drawable.perfil
        };

        public final Class[] SLIDE_CLASSES = {
                GameActivity.class,
                DictionaryActivity.class,
                ProfileActivity.class
        };

        public final String[] SLIDE_TITLES = {
                getString(R.string.play_menu),
                getString(R.string.dictionary_menu),
                getString(R.string.profile_menu)
        };

        public final String[] SLIDE_DESCRIPTIONS = {
                getString(R.string.description_play_menu),
                getString(R.string.description_dictionary_menu),
                getString(R.string.description_profile_menu)
        };


        private Context context;
        private LayoutInflater layoutInflater;


        public SliderAdapter(Context context){
            this.context = context;
        }

        @Override
        public int getCount() {
            return SLIDE_TITLES.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == (LinearLayout)object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position){
            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.slider_card, container, false);

            ImageView slideLogo = (ImageView) view.findViewById(R.id.slide_logo);
            TextView slideTitle = (TextView) view.findViewById(R.id.slide_title);
            TextView slideDescription = (TextView) view.findViewById(R.id.slide_description);

            slideLogo.setImageResource(SLIDE_IMAGES[position]);
            slideTitle.setText(SLIDE_TITLES[position]);
            slideDescription.setText(SLIDE_DESCRIPTIONS[position]);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, (Class<?>) SLIDE_CLASSES[position]);
                    switch(position){
                        // Jugar
                        case 0:
                            onPlayPressed();
                            break;
                        // Diccionario y perfil
                        default:
                            context.startActivity(intent);
                    }
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object){
            container.removeView((LinearLayout) object);
        }

    }


}
