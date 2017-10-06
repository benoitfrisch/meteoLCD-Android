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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_fragment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        progressBar = (ProgressBar) getView().findViewById(R.id.progress);
        webView = (WebView) getView().findViewById(R.id.webView);
        progressBar.setVisibility(View.VISIBLE);

        webView.loadUrl("https://lcd.fresh.lu/getnews.php");
        progressBar.setProgress(webView.getProgress());
        if (webView.getProgress() == 100) {
            progressBar.setVisibility(View.GONE);
        }
    }
}