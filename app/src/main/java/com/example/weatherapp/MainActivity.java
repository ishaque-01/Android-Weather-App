package com.example.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=";
    private static final String END_URL = "&units=metric&appid=";
    private static final String API_KEY = BuildConfig.WEATHER_API_KEY;
    EditText cityName;
    TextView city, temp, condition, feel, humidityText, speedText;
    ImageView imageView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("demo", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        cityName = findViewById(R.id.cityName);
        city = findViewById(R.id.city);
        temp = findViewById(R.id.temperature);
        condition = findViewById(R.id.condition);
        feel = findViewById(R.id.feel);
        humidityText = findViewById(R.id.humidity);
        speedText = findViewById(R.id.speed);
        imageView = findViewById(R.id.search);

        imageView.setOnClickListener((v -> {
            showWeather();
        }));

        try {
            String myCity = sharedPreferences.getString("str", "");
            cityName.setText(myCity);
            showWeather();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showWeather() {
        if (!cityName.getText().toString().isEmpty()) {
            editor.putString("str", cityName.getText().toString());
            editor.apply();

            String finalURL = BASE_URL + cityName.getText().toString() + END_URL + API_KEY;
            RequestQueue requestQueue;
            requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, finalURL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String cityName = response.getString("name");
                        JSONObject main = response.getJSONObject("main");
                        JSONObject wind = response.getJSONObject("wind");

                        JSONArray weather = response.getJSONArray("weather");
                        JSONObject weatherObject = weather.getJSONObject(0);
                        String condition = weatherObject.getString("main");

                        double temperature = main.getDouble("temp");
                        double feelsLike = main.getDouble("feels_like");
                        double speed = wind.getDouble("speed");
                        int humidity = main.getInt("humidity");

                        // Update UI with weather data
                        updateInApp(cityName, temperature, feelsLike, humidity, speed, condition);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
        } else {
            Toast.makeText(MainActivity.this, "Enter City Name!", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateInApp(String cityName, double temperature, double feelsLike, int humidity, double speed, String cond) {
        city.setText(cityName);
        String value = String.valueOf(temperature) + "°C";
        temp.setText(value);
        value = "Feels like " + String.valueOf(feelsLike) + "°C";
        feel.setText(value);
        condition.setText(cond);
        value = "Humidity\n" + String.valueOf(humidity) + "%";
        humidityText.setText(value);
        value = "Wind Speed " + String.valueOf(speed) + "km/h";
        speedText.setText(value);
    }
}

class SharedPrefManager {
    private static final String PREF_NAME = "WeatherAppPrefs";
    private static final String KEY_LAST_CITY = "last_city";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SharedPrefManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveLastCity(String city) {
        editor.putString(KEY_LAST_CITY, city);
        editor.apply();
    }

    public String getLastCity() {
        return sharedPreferences.getString(KEY_LAST_CITY, null);
    }
}