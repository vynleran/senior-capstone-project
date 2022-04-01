package com.example.seizuredetectionapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class UsualLocationsAdapter extends ArrayAdapter<UsualLocationsLayout> {

    private Context mContext;
    int mResource;
    public UsualLocationsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<UsualLocationsLayout> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;

    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        UsualLocationsLayout item = getItem(position);
        String street = item.getStreet();
        String city = item.getCity();
        String stateAndCountry = item.getStateAndCountry();

        UsualLocationsLayout usualLocationsLayout = new UsualLocationsLayout(street, city, stateAndCountry);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView streetName = convertView.findViewById(R.id.streetTextView);
        TextView cityName = convertView.findViewById(R.id.cityTextView);
        TextView stateAndCountryName = convertView.findViewById(R.id.stateCountryTextView);

        streetName.setText(street);
        cityName.setText(city);
        stateAndCountryName.setText(stateAndCountry);

        return convertView;
    }
}
