package com.example.hearing_aid;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.resample.RateTransposer;

public class ModulateActivity extends AppCompatActivity {

    public static Listen listening;
    AudioDispatcher dispatcher;
    AudioProcessor pitchProcessor;
    AndroidAudioPlayer player;
    Button on_off;
    RateTransposer rateTranspose;

    public static boolean flag=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulate);


        on_off=(Button)findViewById(R.id.btn_onoff);


        listening=new Listen();
        listening.execute();

        on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag){
                    flag=false;
                    on_off.setText("Stop");

                    listening=new Listen();
                    listening.execute();

                }else{
                    flag=true;
                    on_off.setText("Start");
                }
            }
        });
    }

    class Listen extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //final int bufferSize= AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT);
            //dispatcher= AudioDispatcherFactory.fromDefaultMicrophone(44100,bufferSize,bufferSize/2);
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
            player=new AndroidAudioPlayer(dispatcher.getFormat());
            double factor=centToFactor(0);

            PitchDetectionHandler pitch=new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                    final float pitch=pitchDetectionResult.getPitch();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!flag){
                              if(pitch>=MainActivity.codes[9][0]&& pitch<MainActivity.codes[9][1])
                                  modulate(-100);
                              else
                                  modulate(0);
                            }
                            else {
                                Listen.super.cancel(true);
                                dispatcher.stop();
                            }
                        }
                    });
                }
            };

            //pitchProcessor= new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,44100,bufferSize,pitch);
            pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitch);
            rateTranspose=new RateTransposer(factor);
            dispatcher.addAudioProcessor(pitchProcessor);
            dispatcher.addAudioProcessor(rateTranspose);
            dispatcher.addAudioProcessor(player);
            dispatcher.run();
            return null;
        }
    }

    private void modulate(int cents){
        double factor=centToFactor(cents);
        rateTranspose.setFactor(factor);
    }

    public static double centToFactor(double cents){
        return 1/Math.pow(Math.E,cents*Math.log(2)/1200/ Math.log(Math.E));
    }
}