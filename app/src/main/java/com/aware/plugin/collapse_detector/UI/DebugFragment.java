package com.aware.plugin.collapse_detector.UI;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.aware.plugin.collapse_detector.AES;
import com.aware.plugin.collapse_detector.CollapseInfo;
import com.aware.plugin.collapse_detector.DatabaseHandler;
import com.aware.plugin.collapse_detector.R;

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
                db.addCollapse(new CollapseInfo(1428238890000L, AES.encrypt("[[\"111111111111111\", \"65.0513\", \"25.4614\", \"1\"]]")));
                db.addCollapse(new CollapseInfo(1428248890000L, AES.encrypt("[[\"111111111111112\", \"65.0633\", \"25.4714\", \"1\"]]")));

                Toast.makeText(getActivity(), "Added sample data to database.", Toast.LENGTH_SHORT).show();

            }
        });

        return rootView;
    }
}