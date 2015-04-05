package com.aware.plugin.collapse_detector;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;


public class DebugFragment extends Fragment {

    public DebugFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_debug, container, false);
        System.out.println("fragment added");
        final Button clearButton = (Button) rootView.findViewById(R.id.clear_db_button);
        final Button addButton = (Button) rootView.findViewById(R.id.add_db_button);


        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View rootView) {
                DatabaseHandler db = new DatabaseHandler(getActivity());

                List<CollapseInfo> coll_list=db.getAllCollapses();
                for(CollapseInfo ci : coll_list){
                    db.deleteCollapse(ci);
                }
                Toast.makeText(getActivity(), "Database cleared.", Toast.LENGTH_SHORT).show();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View rootView) {
                DatabaseHandler db = new DatabaseHandler(getActivity());
                db.addCollapse(new CollapseInfo(1428238890000L,"[65.058113, 25.456262]"));
                db.addCollapse(new CollapseInfo(1428248890000L,"[65.051271, 25.438924]"));
                db.addCollapse(new CollapseInfo(1428258890000L,"[65.048248, 25.470167]"));
                db.addCollapse(new CollapseInfo(1428268890000L,"[65.037519, 25.459288]"));

                Toast.makeText(getActivity(), "Added sample data to database.", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}