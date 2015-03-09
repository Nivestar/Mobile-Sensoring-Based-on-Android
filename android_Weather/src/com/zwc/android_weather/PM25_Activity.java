package com.zwc.android_weather;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class PM25_Activity extends SherlockActivity {
	private String city, url_str;
	private List<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
	private ListView listView;
	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 0x124) {
				SimpleAdapter adapter = new SimpleAdapter(
						getApplicationContext(), listItems,
						R.layout.pm25_listitem, new String[] { "position_name",
								"aqi", "quality", "primary_pollutant", "pm2_5",
								"pm10", "co", "no2", "o3", "so2" }, new int[] {
								R.id.position_name, R.id.aqi, R.id.quality,
								R.id.primary_pollutant, R.id.pm2_5, R.id.pm10,
								R.id.co, R.id.no2, R.id.o3, R.id.so2 });
				if (listView != null)
					listView.setAdapter(adapter);
			}

		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pm25);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		listView = (ListView) findViewById(R.id.pm25_list);
		try {
			city = URLEncoder.encode(getIntent().getStringExtra("city"),
					"utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		url_str = "http://www.pm25.in/api/querys/aqi_details.json?city=" + city
				+ "&token=" + getResources().getString(R.string.pm2_5_key);
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					URL url = new URL(url_str);

					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();
					connection.setRequestMethod("GET");
					BufferedReader br = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String line;
					StringBuilder sb = new StringBuilder();
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					br.close();
					JSONArray jsonArray = new JSONArray(sb.toString());
					sb = null;
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Map<String, String> listItem = new HashMap<String, String>();
						listItem.put("position_name",
								jsonObject.getString("position_name"));
						listItem.put("aqi", jsonObject.getString("aqi"));
						listItem.put("quality", jsonObject.getString("quality"));
						listItem.put("primary_pollutant", jsonObject.getString("primary_pollutant"));
						listItem.put("pm2_5", jsonObject.getString("pm2_5"));
						listItem.put("pm10", jsonObject.getString("pm10"));
						listItem.put("co", jsonObject.getString("co"));
						listItem.put("no2", jsonObject.getString("no2"));
						listItem.put("o3", jsonObject.getString("o3"));
						listItem.put("so2", jsonObject.getString("so2"));
						listItems.add(listItem);
					}
					handler.sendEmptyMessage(0x124);

				} catch (Exception e) {
					e.printStackTrace();
				}
				Looper.loop();
			}
		}.start();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home){
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}