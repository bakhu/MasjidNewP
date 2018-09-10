package com.example.bakhu.masjidnewp;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import helper.CheckNetworkStatus;
import helper.HttpJsonParser;

public class AddMovieActivity extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_HADITH_ENG = "hadith_eng";
    private static final String KEY_HADITH_AR = "hadith_ar";
    private static final String KEY_EVENT_ENG = "event_eng";
    private static final String KEY_RATING = "rating";
    private static final String BASE_URL = "http://10.0.2.2/masjid/";
    private static String STRING_EMPTY = "";
    private EditText hadith_engEditText;
    private EditText hadith_arEditText;
    private EditText event_engEditText;
    private EditText ratingEditText;
    private String hadith_eng;
    private String hadith_ar;
    private String event_eng;
    private String rating;
    private Button addButton;
    private int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);
        hadith_engEditText = (EditText) findViewById(R.id.hadith_eng);
        hadith_arEditText = (EditText) findViewById(R.id.txtHadithArAdd);
        event_engEditText = (EditText) findViewById(R.id.txtEventEngAdd);
        ratingEditText = (EditText) findViewById(R.id.txtRatingAdd);
        addButton = (Button) findViewById(R.id.btnAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    addMovie();
                } else {
                    Toast.makeText(AddMovieActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    /**
     * Checks whether all files are filled. If so then calls AddMovieAsyncTask.
     * Otherwise displays Toast message informing one or more fields left empty
     */
    private void addMovie() {
        if (!STRING_EMPTY.equals(hadith_engEditText.getText().toString()) &&
                !STRING_EMPTY.equals(hadith_arEditText.getText().toString()) &&
                !STRING_EMPTY.equals(event_engEditText.getText().toString()) &&
                !STRING_EMPTY.equals(ratingEditText.getText().toString())) {

            hadith_eng = hadith_engEditText.getText().toString();
            hadith_ar = hadith_arEditText.getText().toString();
            event_eng = event_engEditText.getText().toString();
            rating = ratingEditText.getText().toString();
            new AddMovieAsyncTask().execute();
        } else {
            Toast.makeText(AddMovieActivity.this,
                    "One or more fields left empty!",
                    Toast.LENGTH_LONG).show();

        }


    }

    /**
     * AsyncTask for adding a movie
     */
    private class AddMovieAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display proggress bar
            pDialog = new ProgressDialog(AddMovieActivity.this);
            pDialog.setMessage("Adding Movie. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Populating request parameters
            httpParams.put(KEY_HADITH_ENG, hadith_eng);
            httpParams.put(KEY_HADITH_AR, hadith_ar);
            httpParams.put(KEY_EVENT_ENG, event_eng);
            httpParams.put(KEY_RATING, rating);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_movie.php", "POST", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(AddMovieActivity.this,
                                "Movie Added", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie update
                        setResult(20, i);
                        //Finish ths activity and go back to listing activity
                        finish();

                    } else {
                        Toast.makeText(AddMovieActivity.this,
                                "Some error occurred while adding movie",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}
