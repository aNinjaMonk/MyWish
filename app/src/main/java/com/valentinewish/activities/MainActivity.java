package com.valentinewish.activities;

import com.aspiration.photoviewer.R;
import com.valentinewish.listener.OnSwipeTouchListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;
    int[] slides = {R.drawable.image2,R.drawable.image1,R.drawable.image3,
            R.drawable.image4,R.drawable.image5,R.drawable.image6,
            R.drawable.image7,R.drawable.image8,R.drawable.image9,
            R.drawable.image10,R.drawable.image11,R.drawable.image12,
            R.drawable.image13,R.drawable.image14,R.drawable.image15,
            R.drawable.image16,R.drawable.image17,R.drawable.image18,
            R.drawable.image19,R.drawable.image20};
    ImageSwitcher sw;
    Animation in_left,out_right,out_left,in_right,fade_in,fade_out,zoom_in,zoom_out;
    static final String SLIDE_NO = "slideno";
    int currentSlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        // Hide the status Bar.
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);
        in_left = AnimationUtils.loadAnimation(this,R.anim.slide_in_left);
        out_right = AnimationUtils.loadAnimation(this,R.anim.slide_out_right);
        out_left = AnimationUtils.loadAnimation(this,R.anim.slide_out_left);
        in_right = AnimationUtils.loadAnimation(this,R.anim.slide_in_right);
        fade_in = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(this,R.anim.fade_out);
        zoom_in = AnimationUtils.loadAnimation(this,R.anim.zoom_in);
        zoom_out = AnimationUtils.loadAnimation(this,R.anim.zoom_out);

        setupAds();
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        currentSlide = sharedPreferences.getInt(SLIDE_NO,0);

        //Gallery View
        sw = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        sw.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView myView = new ImageView(getApplicationContext());
                myView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                return myView;
            }
        });
        sw.setImageResource(slides[currentSlide]);

        //Change slides with swipe gestures -- left/right.
        sw.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();

                changeSlide(false);
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();

                changeSlide(true);
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return super.onTouch(v, event);

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SLIDE_NO, currentSlide);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupAds(){
        //Interstitial Ads
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();

        //Banner ads
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void changeSlide(boolean isNext){
        // 3 -- 0 1 2
        if(!isNext){
            if(currentSlide == 0) {
                currentSlide = slides.length - 1;
            }
            else{
                currentSlide--;
            }
            sw.setInAnimation(in_left);
            sw.setOutAnimation(zoom_out);
        }
        else{
            if(currentSlide < slides.length-1){
                currentSlide++;
            }
            else{
                currentSlide = 0;
            }
            sw.setInAnimation(zoom_in);
            sw.setOutAnimation(out_left);
        }
        sw.setImageResource(slides[currentSlide]);

        //Show big ad on every 3rd slide...
        if(currentSlide%3 == 2 ){
            showInterstitial();
        }

        // Show interstitial ads within the photos.
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                goToNextLevel();
            }
        });
        return interstitialAd;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            goToNextLevel();
        }
    }

    private void loadInterstitial() {
        // load the ad.
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void goToNextLevel() {
        // Show the next level and reload the ad to prepare for the level after.
        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
    }

    public void downloadClick(View view){
        Bitmap bm = BitmapFactory.decodeResource( getResources(), slides[currentSlide]);
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);
        File extDirectory = new File(extStorageDirectory);
        if(!extDirectory.exists()){
            extDirectory.mkdir();
        }
        File file = new File(extStorageDirectory, "/"+System.currentTimeMillis()+".PNG");
        try {
            file.createNewFile();
            try{
                FileOutputStream outStream = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();

                //For showing up the newly saved file in gallery.
                MediaScannerConnection.scanFile(this, new String[] {
                                file.getAbsolutePath()},
                        null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri)
                            {}
                        });
            }
            catch (FileNotFoundException e){
                Log.e("Error","file not found");
            }
            catch (IOException e){
                Log.e("Error","I/O error");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(this, "Image Saved To Gallery!", Toast.LENGTH_SHORT).show();
        //Show dialog instead of toast.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.saved_title));
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void shareClick(View view){
        Resources resources = getResources();

        Uri uri = Uri.parse("android.resource://com.aspiration.photoviewer/drawable/"+ slides[currentSlide]);

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
        emailIntent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_message));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, Html.fromHtml(resources.getString(R.string.share_title)));

        emailIntent.setType("image/*");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_message));
        sendIntent.setType("image/*");

        Intent openInChooser = Intent.createChooser(emailIntent, getResources().getText(R.string.share_title));
        PackageManager pm = getPackageManager();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if(packageName.contains("android.email")) {
                emailIntent.setPackage(packageName);
            }
            else if(packageName.contains("whatsapp")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");
                if(packageName.contains("whatsapp")){
                    intent.putExtra(Intent.EXTRA_STREAM,uri);
                    intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_message));
                }
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);
        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(openInChooser);
    }
}
