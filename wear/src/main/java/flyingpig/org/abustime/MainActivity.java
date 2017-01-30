package flyingpig.org.abustime;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends WearableActivity {

    private ProgressBar spinner;
    private Button refresher;
    private ListView output;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        refresher = (Button) findViewById(R.id.refresh_button);
        output = (ListView) findViewById(R.id.mylistView);

        refresher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setVisibility(View.VISIBLE);

                // Check Network Availability
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {

                    //Get Webpage

                    String urlToConnect = "http://nextbuses.mobi/WebView/BusStopSearch/BusStopSearchResults/nwmgamtg?currentPage=0";
                    String busStop = "nwmgamtg";
                    String myUrl = urlToConnect;

                    DownloadWebpageTask task = new DownloadWebpageTask(view);
                    task.execute(myUrl);


                } else {
                    System.out.println("No internet");
                    spinner.setVisibility(View.GONE);
                }
            }
        });
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        private View view;

        public DownloadWebpageTask(View view) {
            this.view = view;
        }

        @Override
        protected String doInBackground(String... urls) {
            ActivatorService service = new ActivatorService();
            // params comes from the execute() call: params[0] is the url.
            try {
                return service.downloadUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "Unable to connect to activator server";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
            spinner.setVisibility(View.GONE);


            String[] items = result.split("`");
            List<String> myList = new ArrayList<String>();
            myList.add(items[0]);

            for(int i=1; i < 6; i+=2) {
                int index = items[i].indexOf(";") + 17;
                int end_index = items[i].indexOf("<", index);

//                int index2 = items[i + 1].indexOf(";");
//                int end_index2 = items[i + 1].indexOf("<", index);

//                + items[i + 1].substring(index2, end_index2)

                String busstring = items[i].substring(index, end_index);

                myList.add(busstring);
            }

            String[] myArray = new String[myList.size()];
            myArray = myList.toArray(myArray);

            ArrayAdapter adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.activity_listview, myArray);

            output.setAdapter(adapter);

        }
    }

}
