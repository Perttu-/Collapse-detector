package com.aware.plugin.collapse_detector;


import android.app.IntentService;
import android.content.Intent;

import com.aware.ESM;


public class PopUp extends IntentService {

    private static String Q;

    void initialize() {
        Q = "{'esm':{" +
                "'esm_type': 1," +
                "'esm_title': 'Fall detected'," +
                "'esm_instructions': 'The plugin detected a fall. Can you elaborate why did this happen?'," +
                "'esm_submit': 'Send'," +
                "'esm_expiration_threashold': 1200," +
                "'esm_trigger': ''" +
                "}}";
    }

    public PopUp() {
        super("PopUp");
        initialize();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent queue_esm = new Intent(ESM.ACTION_AWARE_QUEUE_ESM);
        String esm = "[" + Q + "]";
        queue_esm.putExtra(ESM.EXTRA_ESM, esm);
        this.sendBroadcast(queue_esm);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
