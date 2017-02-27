package ethanfortin_nicaragua.elbluffhospital;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedMap;

public class FetchShipments extends AppCompatActivity {

    ListView listView;
    ArrayList<Class_Fetch_shipment_rows> shipment_data = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch_shipments);

        getShipments();
    }

    private void getShipments() {

        class fetchShipments extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(FetchShipments.this,"Buscando...","Espera, por favor",false,false);
            }

            // Once JSON received correctly, parse and display it
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                parseShipments(s);
            }

            protected String doInBackground(Void... params) {

                RequestHandler reqHan = new RequestHandler();
                HashMap<String, String> map = new HashMap<>();
                String s;

                String str1 = getIntent().getStringExtra("drugid");
                System.out.println("ID: " + str1);
                String str2 = getIntent().getStringExtra("drugname");
                System.out.println("Name: " + str2);
                String str3 = getIntent().getStringExtra("shipdate");
                System.out.println("Date: " + str3);

                map.put("drugid", str1);
                map.put("drugname", str2);
                map.put("shipdate", str3);

                s = reqHan.sendGetRequestParam(ConnVars.URL_FETCH_SHIPMENTS, map);

                return s;
            }
        }

        fetchShipments fS = new fetchShipments();
        fS.execute();
    }

    private void parseShipments(String json) {

        Context context = this;
        ListView listView;
        ArrayList<Class_Fetch_shipment_rows> shipmentData = new ArrayList();

        int totalCast, count = 0;
        String drugId, drugName, shipQuant, shipDate;

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray resArr = jsonObject.getJSONArray(ConnVars.TAG_SHIPMENTS);

            while (count < resArr.length()) {
                JSONObject resObj = resArr.getJSONObject(count);
                drugName = resObj.getString(ConnVars.TAG_SHIPMENTS_DRUGNAME);
                drugId = resObj.getString(ConnVars.TAG_SHIPMENTS_DRUGID);
                shipQuant = resObj.getString(ConnVars.TAG_SHIPMENTS_SHIPQUANT);
                shipDate = resObj.getString(ConnVars.TAG_SHIPMENTS_SHIPDATE);

                try {
                    totalCast = Integer.parseInt(shipQuant);
                    shipmentData.add(new Class_Fetch_shipment_rows(shipDate, drugId, drugName, totalCast));
                } catch (NumberFormatException e) {
                    System.out.println("Number format exception occurred...");
                }

                count++;
            }

        } catch (JSONException j) {
            System.out.println("JSON Exception occurred...");
        }

        ArrayAdapter<Class_Fetch_shipment_rows> adapter = new ArrayAdapter_Fetch_shipment_rows(context, shipmentData);
        listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(adapter);
    }

}