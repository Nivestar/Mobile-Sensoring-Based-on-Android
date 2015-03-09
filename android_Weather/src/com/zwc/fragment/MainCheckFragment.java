package com.zwc.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.zwc.android_weather.PM25_Activity;
import com.zwc.android_weather.R;
import com.zwc.android_weather.Weather_Activity;


/**
 * 查看天气和空气质量页面的fragment
 * @author ZWC
 *
 */
public class MainCheckFragment extends SherlockFragment {
	
	private SharedPreferences sp ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		sp = getActivity().getSharedPreferences("settings", 0);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_check, container, false);
		final Button weather = (Button) rootView.findViewById(R.id.weather);
		final Button PM25 = (Button) rootView.findViewById(R.id.pm25);
		final EditText weather_edit = (EditText) rootView.findViewById(R.id.city_weather);
		
		weather.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent weather_Intent = new Intent(getActivity(),
						Weather_Activity.class);
				Bundle weather_Bundle = new Bundle();
				weather_Bundle.putString("city", weather_edit.getText()
						.toString().trim());
				Editor editor = sp.edit();
				editor.putString("lastCity", weather_edit.getText().toString()
						.trim());
				editor.commit();
				weather_Intent.putExtras(weather_Bundle);
				startActivityForResult(weather_Intent, 1);
			}

		});
		
		PM25.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent pm25_Intent = new Intent(getActivity(),
						PM25_Activity.class);
				pm25_Intent.putExtra("city", "武汉");
				startActivity(pm25_Intent);
			}
		});
		
		return rootView;
				
	}
		
	
}
