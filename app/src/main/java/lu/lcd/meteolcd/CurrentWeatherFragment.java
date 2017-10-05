/*
 * This file is part of meteoLCD-Android.
 *
 * Copyright (c) 2017 Benoît FRISCH
 *
 * meteoLCD-Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * meteoLCD-Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with meteoLCD-Android If not, see <http://www.gnu.org/licenses/>.
 */

package lu.lcd.meteolcd;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;


import cz.msebera.android.httpclient.Header;


public class CurrentWeatherFragment extends Fragment {
    private CurrentWeather currentWeather;
    private ImageView backgroundImage;
    private ImageView currentImage;
    private TextView temperatureLabel;
    private TextView pressionLabel;
    private TextView lastUpdateLabel;
    private TextView backgroundBox;
    private Button refreshButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.current_weather_fragment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        backgroundImage = (ImageView) getView().findViewById(R.id.background);
        currentImage = (ImageView) getView().findViewById(R.id.current);
        temperatureLabel = (TextView) getView().findViewById(R.id.temperature);
        pressionLabel = (TextView) getView().findViewById(R.id.pression);
        lastUpdateLabel = (TextView) getView().findViewById(R.id.lastUpdate);
        refreshButton = (Button) getView().findViewById(R.id.refresh);
        backgroundBox = (TextView) getView().findViewById(R.id.backgroundBox);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentWeather();
            }
        });
        getCurrentWeather();
    }

    private void getCurrentWeather() {
        if (isOnline()) {
            String query = "http://www.lcd.lu/meteo/current_json.php";
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(query, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    currentWeather = new CurrentWeather(response.optString("temperature"), response.optString("icon"), response.optString("pression"), response.optString("lastupdate"));
                    int backID = getResources().getIdentifier("@drawable/" + currentWeather.getIcon() + "_back", "drawable", getContext().getPackageName());
                    backgroundImage.setImageResource(backID);
                    int currID = getResources().getIdentifier("@drawable/" + currentWeather.getIcon(), "drawable", getContext().getPackageName());
                    currentImage.setImageResource(currID);

                    backgroundBox.setBackgroundColor(currentWeather.getColor());

                    temperatureLabel.setText(currentWeather.getTemperature());
                    pressionLabel.setText(currentWeather.getPression());
                    lastUpdateLabel.setText("Dernière mise à jour,\n" + currentWeather.getLastUpdate());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                }
            });
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public boolean isBadNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = cm.getActiveNetworkInfo();

        if (!isOnline()) {
            return true;
        } else {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return false;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_GSM) {
                    return true;
                } else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS) {
                    // Bandwidth between 100 kbps and below
                    return true;
                } else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE) {
                    // Bandwidth between 50-100 kbps
                    return true;
                } else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_UMTS) {
                    // Bandwidth between 50-100 kbps
                    return true;
                } else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_HSDPA) {
                    // Bandwidth between 400-1000 kbps
                    return false;
                } else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE) {
                    return false;
                } else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_HSPAP) {
                    return false;
                } else if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_HSUPA) {
                    return false;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }
}