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
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.analytics.FirebaseAnalytics;
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

public class GraphsFragment extends Fragment {
    private ListView listView;
    private Button refreshButton;
    private ProgressBar progressBar;
    private List<Graph> graphList = new ArrayList<>();
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        progressBar = (ProgressBar) getView().findViewById(R.id.progress);
        listView = (ListView) getView().findViewById(R.id.listView);
        refreshButton = (Button) getView().findViewById(R.id.refreshButton);
        progressBar.setVisibility(View.VISIBLE);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrent();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().setTitle(graphList.get(position).toString());
                GraphsDetailFragment graphsDetailFragment = new GraphsDetailFragment();
                graphsDetailFragment.setGraph(graphList.get(position));
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragmentContent, graphsDetailFragment).commit();
            }
        });

        getCurrent();
    }

    private void getCurrent() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Graphs");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Graph");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "graph");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        String query = "http://www.lcd.lu/meteo/graph_json.php";
        final File current = new File(getContext().getFilesDir(), "graphs.json");
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

        JSONArray response = null;
        try {
            response = new JSONArray(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        graphList.clear();

        JSONArray jsonArray = response;
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                try {
                    JSONObject object;
                    object = jsonArray.getJSONObject(i);
                    if (object.has("label")) {
                        System.out.println(object.optString("label"));
                        Graph graph = new Graph(object.optString("id"), object.optString("label"));
                        graphList.add(graph);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        listView.setAdapter(new GraphAdapter(getContext(), R.layout.graph_list, graphList));
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
