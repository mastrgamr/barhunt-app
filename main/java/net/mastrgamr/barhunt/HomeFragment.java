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
import android.widget.ListView;
import android.widget.Toast;

import net.mastrgamr.api.Keys;
import net.mastrgamr.api.YelpAPI;

import com.beust.jcommander.JCommander;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import static net.mastrgamr.api.YelpAPI.YelpAPICLI;

public class HomeFragment extends Fragment implements Keys{

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    ListView barListView;
    String[] barNames;
    String[] barAddresses;
    String[] barRatings;

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

        barListView = (ListView) rootView.findViewById(R.id.listView);

        //The following checks whether or not the phone is connected to an internet source.
        //If not, don't run the AsyncTask to prevent app crash.
        ConnectivityManager cm = (ConnectivityManager) rootView.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if(ni != null && ni.isConnected()){
            SearchBars lookup = new SearchBars();
            lookup.execute();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());
            dialog.setMessage("Your phone is not connected to a an internet source!\n" +
                    "Please connect and refresh.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Don't do shit.
                            //TODO: Later implement this as a refresh button?
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

    YelpAPICLI yelpApiCli;
    YelpAPI yelpApi;

    private class SearchBars extends AsyncTask<Void, String[], Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            yelpApiCli = new YelpAPICLI();
            new JCommander(yelpApiCli);

            yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
        }

        @Override
        protected Void doInBackground(Void... params) {
            yelpApi.queryAPI(yelpApi, yelpApiCli);

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
                    barAddresses[i] = (String) tempArray.get(0) + " | " + (String) tempArray.get(1);

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
}