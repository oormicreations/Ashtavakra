package in.oormi.ashtavakragita;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextToSpeech tts;
    String alerttone;
    boolean alertenable;
    boolean vibeenable;
    boolean ttsenable;
    float speechrate;
    int freq;
    boolean nonightrem;
    boolean automode;
    int randenable = 0;

    PendingIntent pi;
    BroadcastReceiver br;
    AlarmManager am;

    boolean animrunning = false;
    private float x1;
    static final int MIN_DISTANCE = 150;
    int Verse = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        Setup();
        setupTimer();

        Verse = 0;
        ShowVerse(false);
        //Toast.makeText(MainActivity.this, R.string.touchtostart, Toast.LENGTH_LONG).show();
        final TextView tvs = (TextView)findViewById(R.id.textstatus);

        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Setup();
                    Verse++;
                    ShowVerse(true);
                    animrunning = false;
                    startTimer();
                    if (automode) {
                        tvs.setText(R.string.status_auto);
                    } else {
                        tvs.setText(R.string.status_sch);
                    }
                } else {
                    stopTimer(true);
                    toggle.clearAnimation();
                    tvs.setText(R.string.touchtostart);
                }
            }
        });

        ImageButton mbuttonSetting = (ImageButton) findViewById(R.id.imageButtonSettings);
        mbuttonSetting.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        stopTimer(true);
                        if(toggle.isChecked()) {toggle.toggle();}
                        //Toast.makeText(MainActivity.this, R.string.rempaused, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    }
                });

        ImageButton mbuttonRes = (ImageButton) findViewById(R.id.imageButtonInfo);
        mbuttonRes.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick (View view){
                        Intent intent = new Intent(MainActivity.this, ResourceShow.class);
                        startActivity(intent);
                    }
                });

    }

    public void ShowVerse(boolean sounds){
        String mLine = getString(R.string.cover);
        TextView tv1 = (TextView)findViewById(R.id.textwhosaid);
        TextView tv2 = (TextView)findViewById(R.id.textversenum);
        TextView tv3 = (TextView)findViewById(R.id.textverse);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int c = Color.parseColor("#ffdd00");
            tv3.setShadowLayer(60.0f, 0.0f, 0.0f, c);
        }

        if(Verse < 1){
            tv1.setText("");
            tv2.setText("");
            tv3.setText(mLine);
            tv3.setGravity(Gravity.CENTER);
            tv3.setTextSize(40.0f);
            return;
        }
        tv3.setGravity(Gravity.CENTER_VERTICAL);
        tv3.setTextSize(22.0f);

        if((Verse < 0) || (randenable > 0)){
            Random rand = new Random();
            Verse = 1 + rand.nextInt(297);
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("verses.txt")));
            int v = 1;
            while (((mLine = reader.readLine()) != null) && (v < Verse)) {
                v++;
            }
        } catch (IOException e) {
            Log.e("File Error", "Could not open verse file.");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("File Error", "Could not close verse file.");
                }
            }
        }

        if (mLine == null){
            mLine = getString(R.string.texterror);
        }

        String[] versecontent;
        versecontent = mLine.split("@");
        if(versecontent.length == 3) {
            versecontent[2] = versecontent[2].replace("~", "\n\n");
            tv1.setText(versecontent[0]);
            tv2.setText(versecontent[1]);
            tv3.setText(versecontent[2]);
            if (sounds) {
                versecontent[2] = versecontent[2].replace("-", ",");
                SoundAlert(versecontent[2]);
            }
        }
        else{
            tv3.setText(R.string.errorbadformat);
        }

    }

    private void Setup(){
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_headers, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        alerttone = prefs.getString("notifications_new_message_ringtone",
                "content://settings/system/notification_sound");
        alertenable = prefs.getBoolean("notifications_new_message", true);
        vibeenable = prefs.getBoolean("notifications_new_message_vibrate", false);
        ttsenable = prefs.getBoolean("notifications_new_message_speak", true);

        speechrate = 0.9f;//prefs.getBoolean("notifications_new_message_tts", false);
        String remfreqstr = prefs.getString("freq_list", "60");
        if (remfreqstr.length()<1) remfreqstr = "60";
        freq = 60 * Integer.parseInt(remfreqstr);
        //freq = 10; //testing

        nonightrem = prefs.getBoolean("night_switch", true);
        automode = prefs.getBoolean("auto_switch", false);
        if (automode) {
            nonightrem = false;//autoread in night too
            freq = 20;
        }
        String srandenable = prefs.getString("order_list", "0");
        randenable = Integer.parseInt(srandenable);

        //setupTimer();
/*
        alert = getAlarms.getString("notifications_new_message_ringtone", "notset");
        if(alert.equals("notset")){
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
            dlgAlert.setMessage("Please set a notification tone.");
            dlgAlert.setTitle("Set a sound");
            dlgAlert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Intent intent = new Intent(MainActivity.this, SettingsActivity2.class);
                            //startActivity(intent);
                        }
                    });
            dlgAlert.create().show();

        }
*/

    }

    private void SoundAlert(final String speakme){
        if (alertenable) {
            Uri uri = Uri.parse(alerttone);
            PlayAlert(this, uri);

            if (vibeenable) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }
            if (ttsenable) {
                tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            int result = tts.setLanguage(Locale.UK);
                            if (result == TextToSpeech.LANG_MISSING_DATA ||
                                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS Error", "This language is not supported");
                            } else {
                                tts.setSpeechRate(speechrate);
                                tts.speak(speakme, TextToSpeech.QUEUE_FLUSH, null,
                                        TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                            }
                        } else
                            Log.e("TTS Error", "Initialization Failed!");
                    }
                });
            }
        }
    }

    private void PlayAlert(Context context, Uri alert) {
        MediaPlayer mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            Log.e("Media Error", "Failed to play alert.");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public void startTimer() {
        int delay = 1000 * freq;

        if(delay>60000) {
            String toastinfo = String.valueOf(delay / 60000) + getString(R.string.nxtremmintoast);
            //Toast.makeText(this, getString(R.string.nxtremtoast) + toastinfo, Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + delay, pi );
        }
        else{
            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
                    + delay, pi );
        }

        if (!animrunning) {
            final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
            final Animation animation = AnimationUtils.loadAnimation(MainActivity.this,
                    R.anim.rotate_around_center_point);
            AlphaAnimation fade_in = new AlphaAnimation(0.2f, 1.0f);
            fade_in.setDuration(1000);
            fade_in.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation arg0) {
                }

                public void onAnimationRepeat(Animation arg0) {
                }

                public void onAnimationEnd(Animation arg0) {
                    toggle.setVisibility(View.VISIBLE);
                    toggle.startAnimation(animation);
                }
            });
            toggle.startAnimation(fade_in);
            animrunning = true;
        }
    }

    public void stopTimer(boolean stopanim) {
        if(am!=null){
            if(pi!=null){
                am.cancel(pi);
            }
        }
        if(stopanim) {
            final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButtonStartStop);
            toggle.clearAnimation();
        }
    }

    private void setupTimer() {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {

                boolean enabled = false;
                if (nonightrem) {
                    //get the current timeStamp
                    Calendar calendar = Calendar.getInstance();
                    //calendar.set(Calendar.HOUR_OF_DAY, 17);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    boolean day = (hour<22) && (hour>5);
                    if (day){enabled = true;}
                }
                else {enabled = true;}

                if (enabled) {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP, "AshtavakraAppTag");
                    wl.acquire();

                    Intent startIntent = c.getPackageManager()
                            .getLaunchIntentForPackage(c.getPackageName());
                    startIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //bring to foreground
                    c.startActivity(startIntent);

                    // Show next verse
                    Verse++;
                    ShowVerse(true);
                    stopTimer(false);
                    startTimer();//for next verse
                    wl.release();
                }
                else {//keep looping anyway
                    stopTimer(false);
                    startTimer();
                }

            }
        };
        registerReceiver(br, new IntentFilter("in.oormi.agita") );
        pi = PendingIntent.getBroadcast( this, 0, new Intent("in.oormi.agita"), 0 );
        am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
    }

    @Override
    protected void onDestroy() {
        if(am!=null){
            if(pi!=null){
                am.cancel(pi);
            }
        }
        if(br!=null){
            unregisterReceiver(br);
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                moveTaskToBack(true);
                return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x2;
        TextView tvs = (TextView)findViewById(R.id.textstatus);

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_DISTANCE)
                {
                    // Left to Right swipe action
                    if (x2 > x1)
                    {
                        Verse++;
                    }

                    // Right to left swipe action
                    else
                    {
                        Verse--;
                        if(Verse < 0){Verse = 0;}
                    }

                    stopTimer(true);
                    ShowVerse(false);
                    tvs.setText(R.string.status_user);
                }
                else
                {
                    // consider as something else - a screen tap for example
                }
                break;
        }
        return super.onTouchEvent(event);
    }


}
