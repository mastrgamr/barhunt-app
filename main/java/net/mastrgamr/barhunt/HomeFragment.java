package net.mastrgamr.barhunt;

/**
 * Created by mastrgamr on 8/10/14.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.mastrgamr.api.Keys;
import net.mastrgamr.api.YelpAPI;

import com.beust.jcommander.JCommander;
import org.json.simple.parser.JSONParser;

import static net.mastrgamr.api.YelpAPI.YelpAPICLI;

public class HomeFragment extends Fragment implements View.OnClickListener, Keys{

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private Button getBarInfoBtn;

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

        getBarInfoBtn = (Button) rootView.findViewById(R.id.barInfoBtn);
        getBarInfoBtn.setOnClickListener(this);

        barListView = (ListView) rootView.findViewById(R.id.listView);
        //barListView.setAdapter(new BarList(rootView.getContext(), barNames, barAddresses, barRatings));

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((Main) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onClick(View v) {
        SearchBars lookup = new SearchBars();
        lookup.execute();
    }

    YelpAPICLI yelpApiCli;
    YelpAPI yelpApi;

    private class SearchBars extends AsyncTask<Void, String, Void> {
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
                    barAddresses[i] = barInfo.get("location").toString();
                    barRatings[i] = barInfo.get("rating").toString();
                    //TODO: Find better way to publish progress? Calls 10 times!!!
                    publishProgress(barNames[i], barAddresses[i], barRatings[i]);
                }

            } catch (org.json.simple.parser.ParseException pe) {
                Log.e("HomeFragment", pe.getMessage());
            }
            return null;
        }

        //Moved setAdapter to onPostExecute so it won't be called 10 times. Can't be efficient.
        @Override
        protected void onProgressUpdate(String... values) {
           // barListView.setAdapter(new BarList(rootView.getContext(), barNames, barAddresses, barRatings));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            barListView.setAdapter(new BarList(rootView.getContext(), barNames, barAddresses, barRatings));
            Toast.makeText(rootView.getContext(), "10 Bars found in the area.", Toast.LENGTH_SHORT).show();
        }
    }
}