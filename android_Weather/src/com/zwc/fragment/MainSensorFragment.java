package com.zwc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.zwc.android_weather.Photo_Activity;
import com.zwc.android_weather.R;
import com.zwc.android_weather.SendSensor_Activity;
import com.zwc.android_weather.Video_Activity;
/**
 * 发送传感数据页面的fragment
 * @author ZWC
 *
 */
public class MainSensorFragment extends SherlockFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_sensor, container,
				false);
		Button send = (Button) rootView.findViewById(R.id.send);
		EditText etSend = (EditText) rootView.findViewById(R.id.etSend);
		Button picture = (Button) rootView.findViewById(R.id.picture);
		Button video = (Button) rootView.findViewById(R.id.video);
		Button sendSensor = (Button) rootView.findViewById(R.id.send_Sensor);
		
		picture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						Photo_Activity.class);
				startActivity(intent);
			}

		});
		video.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						Video_Activity.class);
				startActivity(intent);
			}

		});

		sendSensor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						SendSensor_Activity.class);
				startActivity(intent);
			}

		});
		
		return rootView;
	}

}
