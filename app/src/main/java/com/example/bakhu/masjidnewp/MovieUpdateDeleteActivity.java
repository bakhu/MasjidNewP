package com.example.bakhu.masjidnewp;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import helper.CheckNetworkStatus;
import helper.HttpJsonParser;

public class MovieUpdateDeleteActivity extends AppCompatActivity {
    private static String STRING_EMPTY = "";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_MOVIE_ID = "movie_id";
    private static final String KEY_HADITH_ENG = "hadith_eng";
    private static final String KEY_HADITH_AR = "hadith_ar";
    private static final String KEY_EVENT_ENG = "event_eng";
    private static final String KEY_RATING = "rating";
    private static final String BASE_URL = "http://10.0.2.2/masjid/";
    private String movieId;
    private EditText hadith_engEditText;
    private EditText hadith_arEditText;
    private EditText event_engEditText;
    private EditText ratingEditText;
    private String hadith_eng;
    private String hadith_ar;
    private String event_eng;
    private String rating;
    private Button deleteButton;
    private Button updateButton;
    private int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_update_delete);
        Intent intent = getIntent();
        hadith_engEditText = (EditText) findViewById(R.id.txtHadithEngUpdate);
        hadith_arEditText = (EditText) findViewById(R.id.txtHadithArUpdate);
        event_engEditText = (EditText) findViewById(R.id.txtEventEngUpdate);
        ratingEditText = (EditText) findViewById(R.id.txtRatingUpdate);

        movieId = intent.getStringExtra(KEY_MOVIE_ID);
        new FetchMovieDetailsAsyncTask().execute();
        deleteButton = (Button) findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete();
            }
        });
        updateButton = (Button) findViewById(R.id.btnUpdate);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    updateMovie();

                } else {
                    Toast.makeText(MovieUpdateDeleteActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();

                }

            }
        });


    }

    /**
     * Fetches single movie details from the server
     */
    private class FetchMovieDetailsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(MovieUpdateDeleteActivity.this);
            pDialog.setMessage("Loading Movie Details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_MOVIE_ID, movieId);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "get_movie_details.php", "GET", httpParams);
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONObject hadith;
                if (success == 1) {
                    //Parse the JSON response
                    hadith = jsonObject.getJSONObject(KEY_DATA);
                    hadith_eng = hadith.getString(KEY_HADITH_ENG);
                    hadith_ar = hadith.getString(KEY_HADITH_AR);
                    event_eng = hadith.getString(KEY_EVENT_ENG);
                    rating = hadith.getString(KEY_RATING);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    //Populate the Edit Texts once the network activity is finished executing
                    hadith_engEditText.setText(hadith_eng);
                    hadith_arEditText.setText(hadith_ar);
                    event_engEditText.setText(event_eng);
                    ratingEditText.setText(rating);

                }
            });
        }


    }

    /**
     * Displays an alert dialogue to confirm the deletion
     */
    private void confirmDelete() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MovieUpdateDeleteActivity.this);
        alertDialogBuilder.setMessage("Are you sure, you want to delete this movie?");
        alertDialogBuilder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                            //If the user confirms deletion, execute DeleteMovieAsyncTask
                            new DeleteMovieAsyncTask().execute();
                        } else {
                            Toast.makeText(MovieUpdateDeleteActivity.this,
                                    "Unable to connect to internet",
                                    Toast.LENGTH_LONG).show();

                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * AsyncTask to delete a movie
     */
    private class DeleteMovieAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(MovieUpdateDeleteActivity.this);
            pDialog.setMessage("Deleting Movie. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Set movie_id parameter in request
            httpParams.put(KEY_MOVIE_ID, movieId);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "delete_movie.php", "POST", httpParams);
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
                        Toast.makeText(MovieUpdateDeleteActivity.this,
                                "Movie Deleted", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie deletion
                        setResult(20, i);
                        finish();

                    } else {
                        Toast.makeText(MovieUpdateDeleteActivity.this,
                                "Some error occurred while deleting movie",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }

    /**
     * Checks whether all files are filled. If so then calls UpdateMovieAsyncTask.
     * Otherwise displays Toast message informing one or more fields left empty
     */
    private void updateMovie() {


        if (!STRING_EMPTY.equals(hadith_engEditText.getText().toString()) &&
                !STRING_EMPTY.equals(hadith_arEditText.getText().toString()) &&
                !STRING_EMPTY.equals(event_engEditText.getText().toString()) &&
                !STRING_EMPTY.equals(ratingEditText.getText().toString())) {

            hadith_eng = hadith_engEditText.getText().toString();
            hadith_ar = hadith_arEditText.getText().toString();
            event_eng = event_engEditText.getText().toString();
            rating = ratingEditText.getText().toString();
            new UpdateMovieAsyncTask().execute();
        } else {
            Toast.makeText(MovieUpdateDeleteActivity.this,
                    "One or more fields left empty!",
                    Toast.LENGTH_LONG).show();

        }


    }
    /**
     * AsyncTask for updating a movie details
     */

    private class UpdateMovieAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(MovieUpdateDeleteActivity.this);
            pDialog.setMessage("Updating Movie. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Populating request parameters
            httpParams.put(KEY_MOVIE_ID, movieId);
            httpParams.put(KEY_HADITH_ENG, hadith_eng);
            httpParams.put(KEY_HADITH_AR, hadith_ar);
            httpParams.put(KEY_EVENT_ENG, event_eng);
            httpParams.put(KEY_RATING, rating);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "update_movie.php", "POST", httpParams);
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
                        Toast.makeText(MovieUpdateDeleteActivity.this,
                                "Movie Updated", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie update
                        setResult(20, i);
                        finish();

                    } else {
                        Toast.makeText(MovieUpdateDeleteActivity.this,
                                "Some error occurred while updating movie",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}