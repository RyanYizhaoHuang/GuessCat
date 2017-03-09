package ca.ryanhuang.guesscats;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    List<String> catURLs = new ArrayList<>();
    List<String> catNames= new ArrayList<>();
    int chosenCat = 0;
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[3];

    ImageView catImageView;
    Button button0;
    Button button1;
    Button button2;
    Button button_cheat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        catImageView = (ImageView)findViewById(R.id.imgView_cat);
        button0 = (Button)findViewById(R.id.btn_catChosen);
        button1 = (Button)findViewById(R.id.btn_catChosen1) ;
        button2 = (Button)findViewById(R.id.btn_catChosen2) ;
        button_cheat = (Button)findViewById(R.id.btn_cheat) ;

        DownloadTask task = new DownloadTask();
        String result = null;

        try {
            result = task.execute("http://simplycatbreeds.org/Cat-Breeds.html").get();

            //split the unless html
            String[] splitResultOne = result.split("<table border=\"0\"></table>");


            //split the unless html again
            String[] splitResult = splitResultOne[0].split("<p align=\"left\"><strong><span style=\"FONT-SIZE: 12pt\">");
            //System.out.println(splitResult[1]);

            //Rex pattern, get the alt tag
            Pattern p = Pattern.compile("alt=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[1]);

            while(m.find())
            {
                //add names to array
                 catNames.add(m.group(1));
                //System.out.println(m.group(1));
                //Log.i("cat name",m.group(1));
            }

            //Rex pattern, get the src tag
            p = Pattern.compile("src=\"(.*?)\"");
            m = p.matcher(splitResult[1]);

            String webAddress = "http://simplycatbreeds.org/";
            while(m.find())
            {
                //add url to array
                catURLs.add(webAddress + m.group(1));
                //System.out.println(m.group(1));
                //Log.i("can src",m.group(1));
            }




        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //generate new cat
        createNewCat();

    }

    //download image
    public class DownloadTask extends AsyncTask<String,Void, String>
    {

        @Override
        protected String doInBackground(String... urls) {

            String result ="";
            URL url;
            HttpURLConnection urlConnection = null;

            try
            {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection)url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1)
                {
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                }

                return result;

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

    //ImageDownloader
    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>
    {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                connection.connect();

                InputStream inputStream = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //generate new question
    public void createNewCat()
    {
        Random random = new Random();
        chosenCat = random.nextInt(catURLs.size());

        //Download image
        ImageDownloader imageTask = new ImageDownloader();

        Bitmap catImg;

        try {
            catImg = imageTask.execute(catURLs.get(chosenCat)).get();


        catImageView.setImageBitmap(catImg);

        //set up location of correct ans
        locationOfCorrectAnswer = random.nextInt(3);

        int incorrectAnswerLocation;
        for (int i = 0; i < 3; ++i)
        {
            if(i == locationOfCorrectAnswer)
            {
                answers[i] = catNames.get(chosenCat);
            }
            else
            {
                incorrectAnswerLocation = random.nextInt(catURLs.size());

                while (incorrectAnswerLocation == chosenCat)
                {
                    incorrectAnswerLocation = random.nextInt(catURLs.size());
                }
                answers[i] = catNames.get(incorrectAnswerLocation);
            }
        }

            //populate button
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    public void cheatAnswer(View v)
    {
        Toast.makeText(this,catNames.get(chosenCat),Toast.LENGTH_SHORT).show();
    }

    public void catChosen(View view)
    {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer)))
        {
            Toast.makeText(this,"Correct!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this,"Wrong! It was " + catNames.get(chosenCat),Toast.LENGTH_SHORT).show();

        }

        //generate new cat
        createNewCat();
    }
}
