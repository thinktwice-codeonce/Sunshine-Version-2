package com.example.android.sunshine.app.app.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.sunshine.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Phong on 6/30/2016.
 */
public class WeatherForecastFragment extends Fragment {

    ArrayAdapter<String> mWeatherAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_forecast, container, false);

        mWeatherAdapter = new ArrayAdapter<String>(
                getActivity().getApplicationContext(),R.layout.item_weather_forecast,
                new ArrayList<String>());

        ListView listviewWeather = (ListView) view.findViewById(R.id.list_view_weather);
        listviewWeather.setAdapter(mWeatherAdapter);

        new FetchDataTask().execute("http://api.openweathermap.org/data/2.5/forecast/daily" +
                "?q=Ho%20Chi%20Minh&mode=json&units=metric&cnt=7" +
                "&appid=b0ec8202182f4018bc70501439ee06bc");

        return view;
    }

    class FetchDataTask extends AsyncTask<String, Void, List<String>> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected List<String> doInBackground(String... params) {

            try {
                return fetchData(params[0]);
            } catch (IOException e) {
                Log.d("weather", "Network error !", e);
                return null;
            } catch (JSONException e) {
                Log.d("weather", "Wrong JSON format", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);

            if (result!= null && result.size() > 0) {
                mWeatherAdapter.addAll(result);
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        private List<String> fetchData(String urlPath) throws IOException, JSONException {

            StringBuilder rawData = new StringBuilder();
            List<String> weatherItems =  new ArrayList<>();

            BufferedReader bufferedReader = null;
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(urlPath);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    rawData.append(line);
                }

                JSONObject weatherObject = new JSONObject(rawData.toString());
                JSONArray weatherListArr  = weatherObject.getJSONArray("list");
                for (int i=0; i<weatherListArr.length(); i++) {
                    StringBuilder data =  new StringBuilder();
                    JSONObject listItemObject = weatherListArr.getJSONObject(i);
                    data.append(parseDateTime(listItemObject.get("dt").toString())).append(" - ");

                    JSONArray weatherArr = listItemObject.getJSONArray("weather");
                    JSONObject weatherItemObject = weatherArr.getJSONObject(0);
                    data.append(weatherItemObject.get("main")).append(" - ");

                    JSONObject tempObject = listItemObject.getJSONObject("temp");
                    int maxTemp = roundTemp(tempObject.get("max").toString());
                    int minTemp = roundTemp(tempObject.get("min").toString());
                    data.append(maxTemp).append("/").append(minTemp);

                    weatherItems.add(data.toString());
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return weatherItems;
        }

        private String parseDateTime(String seconds) {
            long s = Long.parseLong(seconds);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("EE, MMM d");

            return dateFormatter.format(new Date(s*1000L));
        }

        private int roundTemp(String tempStr) {
            float tempDou = Float.parseFloat(tempStr);
            return Math.round(tempDou);
        }

    }


}
