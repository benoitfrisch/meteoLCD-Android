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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.analytics.FirebaseAnalytics;


public class LightningFragment extends Fragment {
    private ImageView backgroundImage;
    private Button refreshButton;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_fragment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        backgroundImage = (ImageView) getView().findViewById(R.id.background);
        refreshButton = (Button) getView().findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentImage();
            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        getCurrentImage();
        getActivity().setTitle("Lightning Radar");
    }

    private void getCurrentImage() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "LightningRadar");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "LightningRadar");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "lightning");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        if (isOnline()) {
            ImageDownloader downloadTask = new ImageDownloader(backgroundImage);
            downloadTask.execute("http://meteo.lcd.lu/lightning/public_html/lcdflash.jpg");
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