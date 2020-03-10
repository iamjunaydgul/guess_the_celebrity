package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs= new ArrayList<>();
    ArrayList<String> celebNames= new ArrayList<>();
    int celebChosen=0,locationOfCorrectAnswer;
    ImageView celebrityImage;
    Bitmap celebImage;
    Button buttonOne,buttonTwo,buttonThree,buttonFour;


    //for storing answer at random place
    String[] ansArray= new String[4];

    public void generateRandomQuestion(){

        //random number for url
        Random random=new Random();
        celebChosen =random.nextInt(celebURLs.size());

        celebrityImage=findViewById(R.id.celebrityImageView);
        ImageDownloader ImageDownloader=new ImageDownloader();

        try {
            celebImage=ImageDownloader.execute(celebURLs.get(celebChosen)).get();
            celebrityImage.setImageBitmap(celebImage);

            //for setting the celebrity name according to url selected(randomly generated)
            //buttonOne.setText(celebNames.get(celebChosen));

            // for setting the answer at random button 1 to 4
            locationOfCorrectAnswer=random.nextInt(3);
            int incorrectAnswer;
            for(int i=0;i<4;i++){
                if (i==locationOfCorrectAnswer){
                    ansArray[i]=celebNames.get(celebChosen);
                }
                else{
                    //this will generate random number at non-sum postion ,but there is chance that answer and random matches where user
                    //see 2 answers to avoid that situation we introduced int incorrectanswer
                    incorrectAnswer=random.nextInt(celebURLs.size());

                    while(incorrectAnswer==celebChosen){
                        incorrectAnswer=random.nextInt(celebURLs.size());
                    }
                    ansArray[i]=celebNames.get(incorrectAnswer);
                }

            }
            buttonOne.setText(ansArray[0]);
            buttonTwo.setText(ansArray[1]);
            buttonThree.setText(ansArray[2]);
            buttonFour.setText(ansArray[3]);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void celebrityChosen(View view){
        //for getting the tag  we assigned to each button
        String x= view.getTag().toString();

        // cast buttonRandom as a string
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Sorry! it was " + celebNames.get(celebChosen), Toast.LENGTH_SHORT).show();
        }
        generateRandomQuestion();
    }

    public class DownloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result="";
            URL url;
            HttpURLConnection httpURLConnection;

            try {

                url=new URL(urls[0]);
                httpURLConnection= (HttpURLConnection) url.openConnection();
                InputStream inputStream=httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
                int data=inputStreamReader.read();

                while (data!=-1){

                    result+=(char) data;
                    data=inputStreamReader.read();

                }
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class ImageDownloader extends AsyncTask<String,Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                //proper url format
                URL url=new URL(urls[0]);

                //open url connection ,browser
                HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();

                //for holding the content
                InputStream inputStream= httpURLConnection.getInputStream();

                //converting the content directly into image
                Bitmap myBitmap= BitmapFactory.decodeStream(inputStream);
                return myBitmap;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }*/

        ConstraintLayout constraintLayout =  findViewById(R.id.constraintLayoutTwo);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();



        buttonOne=findViewById(R.id.button0);
        buttonTwo=findViewById(R.id.button1);
        buttonThree=findViewById(R.id.button2);
        buttonFour=findViewById(R.id.button3);

        String result;
        DownloadTask task=new DownloadTask();

        try {
            Pattern pattern;
            Matcher matcher;
            result=task.execute("http://www.posh24.se/kandisar").get();
            String[] splitResult= result.split("<div class=\"sidebarContainer\">");
            pattern=Pattern.compile("<img src=\"(.*?)\"");
            matcher=pattern.matcher(splitResult[0]);

            while(matcher.find()){

                celebURLs.add(matcher.group(1));
                // group(1) here 1 is to make group of pattern we need, if you dont put 1 there
                // than it consider whole pattern and print whole pattern
                //System.out.println(matcher.group(1));
            }
            pattern=Pattern.compile("alt=\"(.*?)\"/");
            matcher=pattern.matcher(splitResult[0]);

            while(matcher.find()){

                celebNames.add(matcher.group(1));
                // System.out.println(matcher.group(1));
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        generateRandomQuestion();

    }
}
