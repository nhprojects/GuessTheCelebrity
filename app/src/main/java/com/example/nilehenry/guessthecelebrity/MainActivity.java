package com.example.nilehenry.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String result;
    ArrayList<URL> celebrityPhotoURL;
    ArrayList<String> celebrityNames;

    String currentName;
    ImageView celebrityImageView;
    TextView welcomeTextView;
    Button[] answerButtons;
    Random random;
    HashSet<Integer> validURLIndices;
    HashSet<Integer> celebritiesLeft;

    public class ImageContentDownloader extends  AsyncTask<String,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url=new URL(strings[0]);
                HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream= connection.getInputStream();
                Bitmap bitmapPhoto= BitmapFactory.decodeStream(inputStream);
                return bitmapPhoto;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    public class URLContentDownloader extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            result="";
            URL url;
            HttpURLConnection urlConnection=null;
            try{
                url= new URL(strings[0]);
                urlConnection= (HttpURLConnection) url.openConnection();
                InputStream inputStream= urlConnection.getInputStream();
                InputStreamReader reader=new InputStreamReader(inputStream);
                BufferedReader bufferedReader= new BufferedReader(reader);
                String data= bufferedReader.readLine();

                while (data!=null   ){
                    result=result+data;
                    data=bufferedReader.readLine();
                }
                return result;
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

    }

    public void initialize(){
        celebrityPhotoURL=new ArrayList<URL>();
        celebrityNames= new ArrayList<String>();
        validURLIndices=new HashSet<Integer>();
        celebritiesLeft= new HashSet<Integer>();

        random= new Random();

        answerButtons= new Button[4] ;
        answerButtons[0]=(Button) findViewById(R.id.button6);
        answerButtons[1]=(Button) findViewById(R.id.button7);
        answerButtons[2]=(Button) findViewById(R.id.button8);
        answerButtons[3]=(Button) findViewById(R.id.button9);
        for (Button button:answerButtons){
            button.setVisibility(View.INVISIBLE);
        }

        welcomeTextView= (TextView) findViewById(R.id.welcomeTextView);

        celebrityImageView= (ImageView) findViewById(R.id.imageView);
        //

        URLContentDownloader urlContentDownloader= new URLContentDownloader();

        try {
            result= urlContentDownloader.execute("http://www.posh24.se/kandisar").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        String[] splitResult= result.split("class=\"sidebarContainer");

        Pattern p= Pattern.compile("<img src=\"(.*?)\"");
        Matcher m= p.matcher(splitResult[0]);


        int i=0;
        while (m.find()){
            try {
                URL currentURL= new URL(m.group(1));
                celebrityPhotoURL.add(currentURL);
                validURLIndices.add(i);
                celebritiesLeft.add(i);
            } catch (MalformedURLException e) {}
            i=i+1;
        };

        Log.i("info",Integer.toString(i));

        p= Pattern.compile("alt=\"(.*?)\"");
        m= p.matcher(splitResult[0]);

        i=0;
        while (m.find()){
            try{
                String currentName=m.group(1);
                if (validURLIndices.contains(i));{
                    celebrityNames.add(currentName);
                }
            }
            catch (Exception e){}
            i=i+1;
        }
    }

    public void generateBoard(){
        int[] leftIndices= new int[celebritiesLeft.size()];
        int i=0;
        for (Integer celebIndex: celebritiesLeft){
            leftIndices[i]=celebIndex;
            i=i+1;
        }
        int[] validIndices= new int[validURLIndices.size()];
        i=0;
        for (Integer validIndex: validURLIndices) {
            validIndices[i]=validIndex;
            i = i + 1;
        }
        int buttonChosen= random.nextInt(answerButtons.length); //choose button to put correct answer
        int randIndex= leftIndices[random.nextInt(leftIndices.length)]; //choose a celebrity
        String celebName= celebrityNames.get(randIndex);
        celebritiesLeft.remove(randIndex);
        ImageContentDownloader imgDownloader= new ImageContentDownloader();
        Bitmap celebBitmap=null;
        try {
             celebBitmap = imgDownloader.execute(celebrityPhotoURL.get(randIndex).toString()).get();
        }
        catch(Exception e){
        }
        celebrityImageView.setImageBitmap(celebBitmap);
        answerButtons[buttonChosen].setText(celebName);
        HashSet<String> namesChosen=new HashSet<String>();
        namesChosen.add(celebName);
        for (int z=0;z<answerButtons.length;z=z+1){
            if (z!=buttonChosen){
                int rand= random.nextInt(validURLIndices.size());
                String nameChosen= celebrityNames.get(rand);
                while (namesChosen.contains(nameChosen)){
                    rand=random.nextInt(validURLIndices.size());
                    nameChosen=celebrityNames.get(rand);
                }
                answerButtons[z].setText(nameChosen);
            }
        }
        currentName=celebName;

    }

    public Bitmap getBitmapofURL(URL url){
        ImageContentDownloader task= new ImageContentDownloader();
        try {
            Bitmap imageFromWebsite= task.execute(url.toString()).get();
            return imageFromWebsite;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void goOnClick(View view){
        generateBoard();
        view.setVisibility(View.INVISIBLE);
        welcomeTextView.setVisibility(View.INVISIBLE);
        for (Button button:answerButtons){
            button.setVisibility(View.VISIBLE);
        }

    }

    public void makeGuess(View view){
        Button button= (Button) view;
        if (button.getText().toString().equals(currentName)){
            Toast.makeText(MainActivity.this,"Correct",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this,"correct answer is: " + currentName, Toast.LENGTH_SHORT).show();
        }
        generateBoard();
    }
}
