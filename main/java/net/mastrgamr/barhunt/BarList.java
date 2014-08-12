package net.mastrgamr.barhunt;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mastr_000 on 8/11/2014.
 */
public class BarList extends BaseAdapter {

    ArrayList<SingleRow> list;
    Context c;

    //Make the row items easily accessible through this private class.
    private class SingleRow {
        int image;
        String title;
        String address;
        String rating;

        SingleRow(String title, String address, String rating) {
            //this.image = image;
            this.title = title;
            this.address = address;
            this.rating = rating;
        }
    }

    public BarList(Context c, String[] titles, String[] addresses, String[] ratings) {
        this.c = c;
        list = new ArrayList<SingleRow>();

        for(int i = 0; i < 10; i++) {
            list.add(new SingleRow(titles[i], addresses[i], ratings[i]));
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    //Not a completely necessary method, yet?...
    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Inflater returns Object instance of the xml file. findviewbyid returns specific parts.
        //Tutorial: https://www.youtube.com/watch?v=_l9e2t4fcfM @3:00
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.bar_info_row, parent, false);

        TextView barName = (TextView) row.findViewById(R.id.barNameTxt);
        //TextView barAddress;
        TextView barRating = (TextView) row.findViewById(R.id.barRatingTxt);

        SingleRow temp = list.get(position);
        barName.setText(temp.title);
        barRating.setText(temp.rating);

        return row;
    }
}
