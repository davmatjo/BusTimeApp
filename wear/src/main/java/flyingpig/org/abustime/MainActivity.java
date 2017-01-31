package flyingpig.org.abustime;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.location.LocationListener;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static double latitude;
    public static double longitude;

    private GoogleApiClient mGoogleApiClient;
    private ProgressBar spinner;
    private FrameLayout spinner_background;
    private Button refresher;
    private ListView output;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mGoogleApiClient.connect();

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner_background = (FrameLayout) findViewById(R.id.spinner_background);
        refresher = (Button) findViewById(R.id.refresh_button);
        output = (ListView) findViewById(R.id.mylistView);



        refresher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner_background.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);


                String busStop = "";

                if((latitude >= 52.430961 && latitude <= 52.436868) && (longitude >= -1.568901 && longitude <= -1.560439)){
                    busStop = "nwmgamtg";
                }else if((latitude >= 52.409519 && latitude <= 52.412283) && (longitude >= -1.520113 && longitude <= -1.512624)){
                    busStop = "nwmadaga";
                }else if((latitude >= 52.398766 && latitude <= 52.412260) && (longitude >= -1.512623 && longitude <= -1.495199)){
                    busStop = "nwmdptjm";
                }else if((latitude >= 52.430961 && latitude <= 52.436868) && (longitude >= -1.495198 && longitude <= -1.487220)){
                    busStop = "nwmdtptd" ;
                }else {
                    busStop = "";
                }


                // Check Network Availability
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if ((networkInfo != null) && networkInfo.isConnected() && !busStop.isEmpty()) {

                    //Get Webpage


                    String urlToConnect = "http://nextbuses.mobi/WebView/BusStopSearch/BusStopSearchResults/";
                    String myUrl = urlToConnect + busStop;

                    DownloadWebpageTask task = new DownloadWebpageTask(view);
                    task.execute(myUrl);


                } else if(busStop.isEmpty()){
                    System.out.println("Wrong Location");
                    spinner_background.setVisibility(View.GONE);
                    spinner.setVisibility(View.GONE);
                    String[] myArray = {"Incorrect location"};

                    ArrayAdapter adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.activity_listview, myArray);

                    output.setAdapter(adapter);
                } else {
                    System.out.println("No internet");
                    spinner_background.setVisibility(View.GONE);
                    spinner.setVisibility(View.GONE);
                    String[] myArray = {"No internet"};

                    ArrayAdapter adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.activity_listview, myArray);

                    output.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("connected API");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(500)
                .setFastestInterval(500);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connect fail");
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
            spinner_background.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);


            String[] items = result.split("`");
            List<String> myList = new ArrayList<String>();
            myList.add(items[0]);
            System.out.println(items.length);

            for(int i=1; i < 21; i+=2) {
                int index = items[i].indexOf(";") + 14;
                int end_index = items[i].indexOf("\"", index);

                int index2 = items[i + 1].indexOf(";") + 1;
                int end_index2 = items[i + 1].indexOf("<", index2);



                String busstring = items[i].substring(index, end_index) + " " + items[i + 1].substring(index2, end_index2);

                myList.add(busstring);
            }

            String[] myArray = new String[myList.size()];
            myArray = myList.toArray(myArray);

            ArrayAdapter adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.activity_listview, myArray);

            output.setAdapter(adapter);

        }
    }
}



