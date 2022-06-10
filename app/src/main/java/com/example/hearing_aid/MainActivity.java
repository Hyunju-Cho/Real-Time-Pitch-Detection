package com.example.hearing_aid;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    public static Listen listening;
    AudioDispatcher dispatcher;
    AudioProcessor pitchProcessor;
    TextView pitchText,noteText;
    AndroidAudioPlayer player;
    Button on_off;
    LinearLayout status;

    public static boolean flag=true;

    public static double[][]codes=new double[12][2];

    public void processPitch(float pitch){
        pitchText.setText(""+pitch);

        status.setBackgroundColor(Color.GREEN);

        if(pitch>=codes[0][0]&&pitch<codes[0][1]){
            noteText.setText("G#");
        }
        else if(pitch>=codes[1][0]&&pitch<codes[1][1]){
            noteText.setText("A");
        }
        else if(pitch>=codes[2][0]&&pitch<codes[2][1]){
            noteText.setText("A#");
        }
        else if(pitch>=codes[3][0]&&pitch<codes[3][1]){
            noteText.setText("B");
        }
        else if(pitch>=codes[4][0]&&pitch<codes[4][1]){
            noteText.setText("C");
        }
        else if(pitch>=codes[5][0]&&pitch<codes[5][1]){
            noteText.setText("C#");
        }
        else if(pitch>=codes[6][0]&&pitch<codes[6][1]){
            noteText.setText("D");
        }
        else if(pitch>=codes[7][0]&&pitch<codes[7][1]){
            noteText.setText("D#");
        }
        else if(pitch>=codes[8][0]&&pitch<codes[8][1]){
            noteText.setText("E");
        }
        else if(pitch>=codes[9][0]&&pitch<codes[9][1]){
            noteText.setText("F");
            status.setBackgroundColor(Color.RED);
        }
        else if(pitch>=codes[10][0]&&pitch<codes[10][1]){
            noteText.setText("F#");
        }
        else if(pitch>=codes[11][0]&&pitch<codes[11][1]){
            noteText.setText("G");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codes[0][0]=103.82;//G#
        codes[0][1]=109.99;
        codes[1][0]=110.00;//A
        codes[1][1]=116.53;
        codes[2][0]=116.54;//A#
        codes[2][1]=123.46;
        codes[3][0]=123.47;//B
        codes[3][1]=130.80;
        codes[4][0]=130.81;//C
        codes[4][1]=138.58;
        codes[5][0]=138.59;//C#
        codes[5][1]=146.81;
        codes[6][0]=146.82;//D
        codes[6][1]=155.55;
        codes[7][0]=155.56;//D#
        codes[7][1]=164.80;
        codes[8][0]=164.81;//E
        codes[8][1]=174.60;
        codes[9][0]=174.61;//F
        codes[9][1]=184.96;
        codes[10][0]=184.99;//F#
        codes[10][1]=195.98;
        codes[11][0]=195.99;//G
        codes[11][1]=207.65;

        noteText=(TextView) findViewById(R.id.tv_notetext);
        pitchText=(TextView) findViewById(R.id.tv_pitchtext);
        on_off=(Button)findViewById(R.id.btn_onoff);
        status=(LinearLayout)findViewById(R.id.status);

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
                    noteText.setText("dmarP");
                    pitchText.setText("Hertz");
                    status.setBackgroundColor(Color.WHITE);
                }
            }
        });
    }

    class Listen extends AsyncTask<Void, double[], Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            //final int bufferSize= AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT);
            //dispatcher= AudioDispatcherFactory.fromDefaultMicrophone(44100,bufferSize,bufferSize/2);
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

            PitchDetectionHandler pitch=new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                    final float pitch=pitchDetectionResult.getPitch();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!flag)
                                processPitch(pitch);
                            else
                                Listen.super.cancel(true);
                        }
                    });
                }
            };

            //pitchProcessor= new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,44100,bufferSize,pitch);
            pitchProcessor= new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,22050,1024,pitch);
            dispatcher.addAudioProcessor(pitchProcessor);
            dispatcher.run();
            return null;
        }
    }
}