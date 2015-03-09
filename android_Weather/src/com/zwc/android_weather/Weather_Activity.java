package com.zwc.android_weather;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.zwc.util.CreateKey;
import com.zwc.util.DateUtil;
import com.zwc.util.WeatherDatabaseHelper;
import com.zwc.util.WeatherUtil;
/**
 * 天气Activity
 * @author ZWC
 *
 */
public class Weather_Activity extends SherlockActivity {
	private String city;
	ListView listView;
	private TextView tv_Release;
	Handler mhandler;
	SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy年MM月dd日");
	List<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
	private String releaseTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		tv_Release = (TextView) findViewById(R.id.tv_Release);
		listView = (ListView) findViewById(R.id.list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mhandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x123) {
					try {
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyyMMddHHmm");
						Date release = dateFormat.parse(releaseTime);
						dateFormat = new SimpleDateFormat(
								"数据更新时间：yyyy年MM月dd日  HH：mm");
						tv_Release.setText(dateFormat.format(release));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					SimpleAdapter simpleAdapter = new SimpleAdapter(
							getApplicationContext(), listItems,
							R.layout.weather_listitem, new String[] { "date",
									"dayWeather", "dayTemp", "dayWindDegree",
									"nightWeather", "nightTemp",
									"nightWindDegree" }, new int[] { R.id.date,
									R.id.dayWeather, R.id.dayTemp,
									R.id.dayWindDegree, R.id.nightWeather,
									R.id.nightTemp, R.id.nightWindDegree });
					listView.setAdapter(simpleAdapter);

				}
			}
		};

		Intent intent = this.getIntent();
		city = intent.getStringExtra("city");// 或者intent.getExtras().getString("city");
		new Thread() {
			@Override
			public void run() {
				try {
					String areaid = null;
					WeatherDatabaseHelper databaseHelper = new WeatherDatabaseHelper(
							getApplicationContext(), "areaid", 1);
					SQLiteDatabase db = databaseHelper.getReadableDatabase();
					Cursor cursor = db.rawQuery(
							"select areaid from areaid where namecn = ?",
							new String[] { city });
					Log.d("haha", cursor.getCount()+"");
					if (cursor.getCount() == 0) {
						Intent intent = new Intent();
						intent.putExtra("cityFound", false);
						setResult(RESULT_OK, intent);
						finish();
					}
					// getColumnIndex()区分大小写
					while (cursor.moveToNext())
						areaid = cursor.getString(cursor
								.getColumnIndex("AREAID"));
					cursor.close();
					db.close();
					if (areaid != null) {
						CreateKey key = new CreateKey(areaid, "forecast_f");
						String url = key.createUrl();
						InputStream is = new URL(url).openStream();
						if (is != null)
							getWeatherInfo(is);
						is.close();
						mhandler.sendEmptyMessage(0x123);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
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

	private void getWeatherInfo(InputStream is) throws Exception {

		InputStreamReader br = new InputStreamReader(is, "utf-8");
		int b;
		StringBuilder sb = new StringBuilder();
		while ((b = br.read()) != -1) {
			sb.append((char) b);
		}
		JSONObject jsonObject = new JSONObject(sb.toString());
		JSONObject cJsonObject = jsonObject.getJSONObject("c");
		String city = cJsonObject.getString("c3");
		String province = cJsonObject.getString("c7");
		String latitude = cJsonObject.getString("c13");
		String longtitude = cJsonObject.getString("c14");
		JSONObject fJsonObject = jsonObject.getJSONObject("f");
		releaseTime = fJsonObject.getString("f0");
		Log.d("weather", releaseTime);

		JSONArray f1JsonArray = fJsonObject.getJSONArray("f1");
		WeatherUtil weatherUtil = new WeatherUtil();
		DateUtil dateUtil = new DateUtil();
		for (int i = 0; i < f1JsonArray.length(); i++) {
			Map<String, String> listItem = new HashMap<String, String>();
			JSONObject tmpJsonObject = f1JsonArray.getJSONObject(i);
			Calendar calendar = Calendar.getInstance();
			Date today = new Date();
			calendar.setTime(today);
			calendar.add(Calendar.DATE, i);
			String dateChinese = (calendar.get(Calendar.MONTH) + 1)
					+ "月"
					+ calendar.get(Calendar.DAY_OF_MONTH)
					+ "日"
					+ "  "
					+ "星期"
					+ dateUtil.getChineseWeek(calendar
							.get(Calendar.DAY_OF_WEEK));
			listItem.put("date", dateChinese);

			String faString = tmpJsonObject.getString("fa");
			listItem.put("dayWeather", weatherUtil.getWeatherById(faString));

			String fbString = tmpJsonObject.getString("fb");
			listItem.put("nightWeather", weatherUtil.getWeatherById(fbString));

			String fcString = tmpJsonObject.getString("fc");
			if (!fcString.equals(""))
				listItem.put("dayTemp", fcString + "°");
			else
				listItem.put("dayTemp", "");

			String fdString = tmpJsonObject.getString("fd");
			listItem.put("nightTemp", fdString + "°");

			String feString = tmpJsonObject.getString("fe");
			listItem.put("dayWindDirection",
					weatherUtil.getWindDirectionById(feString));

			String ffString = tmpJsonObject.getString("ff");
			listItem.put("nightWindDirection",
					weatherUtil.getWindDirectionById(ffString));

			String fgString = tmpJsonObject.getString("fg");
			listItem.put("dayWindDegree",
					weatherUtil.getWindDegreeById(fgString));

			String fhString = tmpJsonObject.getString("fh");
			listItem.put("nightWindDegree",
					weatherUtil.getWindDegreeById(fhString));

			String fiString = tmpJsonObject.getString("fi");
			String[] sunTemp = fiString.split("|");
			listItem.put("sunrise", sunTemp[0]);
			listItem.put("sunset", sunTemp[1]);

			listItems.add(listItem);
		}
	}

}
