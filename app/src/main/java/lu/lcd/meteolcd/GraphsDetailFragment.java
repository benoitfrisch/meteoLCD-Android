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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cz.msebera.android.httpclient.Header;

public class GraphsDetailFragment extends Fragment {
    private Button backButton;
    private Button refreshButton;
    private ProgressBar progressBar;
    private Graph graph;
    private LineChart chart;
    private List<GraphDetail> graphDetailsList = new ArrayList<>();

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.graph_detail_fragment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        progressBar = (ProgressBar) getView().findViewById(R.id.progress);
        backButton = (Button) getView().findViewById(R.id.backButton);
        refreshButton = (Button) getView().findViewById(R.id.refreshButton);
        chart = (LineChart) getView().findViewById(R.id.chart);
        progressBar.setVisibility(View.VISIBLE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragmentContent, new GraphsFragment()).commit();
            }
        });


        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                getCurrent();
            }
        });

        getCurrent();
    }

    private void getCurrent() {
        String query = "http://www.lcd.lu/meteo/graph_json.php?id=" + graph.getId();
        final File current = new File(getContext().getFilesDir(), "graphs_" + graph.getId() + ".json");
        if (current.exists() && isBadNetwork()) {
            loadFromFile(current);
            progressBar.setVisibility(View.GONE);
        } else {
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(query, new FileAsyncHttpResponseHandler(current) {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File file) {
                    loadFromFile(file);
                }
            });
        }
    }

    private void loadFromFile(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String text = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();

        JSONObject response = null;
        try {
            response = new JSONObject(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        graphDetailsList.clear();

        JSONArray jsonArray = response.optJSONArray("history");
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                try {
                    JSONObject object;
                    object = jsonArray.getJSONObject(i);
                    if (object.has("date")) {
                        System.out.println(object.optString("date"));
                        GraphDetail graphDetail = new GraphDetail(object.optString("date"), object.optString("value"), object.optString("unit"));
                        graphDetailsList.add(graphDetail);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        displayGraph();
    }

    public void displayGraph() {
        List<Entry> entries = new ArrayList<>();
        String[] dateEntries = new String[graphDetailsList.size()-2];
        String unit = " -";
        for (int i = 0; i < (graphDetailsList.size() - 2); i++) {
            GraphDetail data = graphDetailsList.get(i);
            // turn your data into Entry objects
            entries.add(new Entry(i, Float.valueOf(data.getValue())));
            dateEntries[i] = data.getDate();
            unit = data.getUnit();
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Value");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.GRAY);
        dataSet.setDrawCircles(false);
        dataSet.setFillColor(R.color.colorPrimary);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter();
        formatter.setValues(dateEntries);
        chart.getXAxis().setValueFormatter(formatter);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setLabelRotationAngle(270);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisLeft().setValueFormatter(new GraphValueFormatter(unit));
        chart.getLegend().setEnabled(false);
        chart.invalidate(); // refresh

        progressBar.setVisibility(View.GONE);
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
