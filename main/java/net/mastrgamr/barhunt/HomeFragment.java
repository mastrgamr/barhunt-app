package net.mastrgamr.barhunt;

/**
 * Created by mastrgamr on 8/10/14.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import net.mastrgamr.api.Keys;
import net.mastrgamr.api.YelpAPI;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class HomeFragment extends Fragment implements Keys, View.OnClickListener{

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = HomeFragment.class.getSimpleName();

    EditText zipCodeTxt;
    Button goBtn;
    ListView barListView;
    String[] barNames;
    String[] barAddresses;
    String[] barRatings;
    SearchBars lookup;

    View rootView;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        zipCodeTxt = (EditText) rootView.findViewById(R.id.zipcodeTxt);
        goBtn = (Button) rootView.findViewById(R.id.goBtn);
        goBtn.setOnClickListener(this);
        barListView = (ListView) rootView.findViewById(R.id.listView);

        //The following checks whether or not the phone is connected to an internet source.
        //If not, don't run the AsyncTask to prevent app crash.
        ConnectivityManager cm = (ConnectivityManager) rootView.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if(ni != null && ni.isConnected()){
            lookup = new SearchBars();
            lookup.execute();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());
            dialog.setMessage("Your phone is not connected to a an internet source!\n" +
                    "Please connect and refresh.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Don't do shit.
                            //TODO: Later implement this as a refresh action?
                        }
                    });
            dialog.show();
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((Main) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    YelpAPI yelpApi;

    //TODO: Create efficient way to check if thread is being run
    //if task is running create the new instance. Since an instance can only be run once >_>
    @Override
    public void onClick(View v) {
        //if AsyncTask is running, do nothing.
        if(lookup.getStatus() != AsyncTask.Status.RUNNING) {
            lookup = new SearchBars();
            lookup.execute(zipCodeTxt.getText().toString());
        }
    }

    private class SearchBars extends AsyncTask<String, String[], Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
        }

        @Override
        protected Void doInBackground(String... location) {

            if (location.length == 0) {
                yelpApi.queryAPI(yelpApi, YelpAPI.DEFAULT_LOCATION);
            }
            if (location.length != 0) {
                yelpApi.queryAPI(yelpApi, location[0]);
            }

            //Create a temp string to hold the JSON found in API return.
            String tempJSON;
            barNames = new String[10];
            barAddresses = new String[10];
            barRatings = new String[10];

            try {
                for (int i = 0; i < 10; i++) {
                    JSONParser parser = new JSONParser();
                    org.json.simple.JSONObject barInfo;

                    tempJSON = yelpApi.getBusinessResponseJSON(i); //Use temp JSON to parse relevant bar info.
                    barInfo = (org.json.simple.JSONObject) parser.parse(tempJSON);
                    Log.d("HomeFragment", tempJSON);

                    barNames[i] = barInfo.get("name").toString();

                    String tempLoc = barInfo.get("location").toString();
                    JSONObject tempLocObj = (JSONObject) parser.parse(tempLoc);
                    JSONArray tempArray = (JSONArray) tempLocObj.get("display_address");
                    barAddresses[i] = tempArray.get(0) + " | " + tempArray.get(1);

                    Log.d("HomeFragment", barAddresses[i]);
                    barRatings[i] = barInfo.get("rating").toString();
                }
                publishProgress(barNames, barAddresses, barRatings);

            } catch (org.json.simple.parser.ParseException pe) {
                Log.e("HomeFragment", pe.getMessage());
            }
            return null;
        }

        //Moved setAdapter to onPostExecute so it won't be called 10 times. Can't be efficient.
        @Override
        protected void onProgressUpdate(String[]... values) {
           // barListView.setAdapter(new BarList(rootView.getContext(), barNames, barAddresses, barRatings));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            barListView.setAdapter(new BarList(rootView.getContext(), barNames, barAddresses, barRatings));
            Toast.makeText(rootView.getContext(), "10 Bars found in the area.", Toast.LENGTH_SHORT).show();
        }
    }

    //Not fully working yet.
    public void refresh() {
        //System.out.println(lookup.getStatus() == AsyncTask.Status.RUNNING);
        //if AsyncTask is running, do nothing.
        if(lookup != null) {
            lookup = new SearchBars();
            lookup.execute();
        }
    }
}