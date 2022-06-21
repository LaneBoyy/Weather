package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private final String MyURL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=4b17c96510334805efe1dda000e34953";

    private EditText editTextCity;
    private TextView textCity;
    private TextView textTemp;
    private TextView textWeatherInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCity = findViewById(R.id.editTextCity);
        textCity = findViewById(R.id.textCity);
        textTemp = findViewById(R.id.textTemp);
        textWeatherInfo = findViewById(R.id.textWeatherInfo);

        clickOnShowWeatherButton();
    }

    private void clickOnShowWeatherButton() {
        Button buttonShowWeather = findViewById(R.id.buttonShowWeather);
        buttonShowWeather.setOnClickListener(v -> {
            String city = editTextCity.getText().toString().trim();
            if (!city.isEmpty()) {
                String URL = String.format(MyURL, city);
                    DownloadJSONTask showWeatherTask = new DownloadJSONTask();
                    showWeatherTask.execute(URL);
            } else {
                Toast.makeText(this, getString(R.string.hint_in_editText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class DownloadJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("weather");

                String cityName = jsonObject.getString("name");
                String tempStr = jsonObject.getJSONObject("main").getString("temp");
                String feels_likeStr = jsonObject.getJSONObject("main").getString("feels_like");

                JSONObject weather = jsonArray.getJSONObject(0);
                String main = weather.getString("main");
                String description = weather.getString("description");
                String wind = jsonObject.getJSONObject("wind").getString("speed");
                String pressure = jsonObject.getJSONObject("main").getString("pressure");

                double tempDouble = Double.parseDouble(tempStr) - 273.15;
                double feels_likeDouble = Double.parseDouble(feels_likeStr) - 273.15;
                int temp = (int) tempDouble;
                int feels_like = (int) feels_likeDouble;


                textTemp.setText(String.format(" %s°", temp));
                textCity.setText(cityName);
                textWeatherInfo.setText(String.format("Feels like %s°C\n\n%s (%s)\n\nWind %s m/s\n\nAtmospheric pressure %s mm Hg", feels_like, main, description, wind, pressure));
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, getString(R.string.text_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}