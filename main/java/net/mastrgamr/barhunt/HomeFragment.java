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
import android.widget.TextView;

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

    private TextView barInfoTxt;
    private Button getBarInfoBtn;

    String barName;


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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        barInfoTxt = (TextView) rootView.findViewById(R.id.barInfoTxt);
        getBarInfoBtn = (Button) rootView.findViewById(R.id.barInfoBtn);

        getBarInfoBtn.setOnClickListener(this);

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
            try {
                JSONParser parser = new JSONParser();
                org.json.simple.JSONObject barTitle;
                barName = yelpApi.getBusinessResponseJSON();
                barTitle = (org.json.simple.JSONObject) parser.parse(barName);
                Log.d("HomeFragment", barName);
                barName = barTitle.get("name").toString();
                publishProgress(barName);

            } catch (org.json.simple.parser.ParseException pe) {
                Log.e("HomeFragment", pe.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            barInfoTxt.setText(values[0]);
        }
    }
}