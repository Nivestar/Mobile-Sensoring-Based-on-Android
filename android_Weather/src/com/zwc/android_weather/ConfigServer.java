package com.zwc.android_weather;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ConfigServer extends Activity {

	private EditText etIP;
	private EditText etPort;
	private SharedPreferences sp;
	private Button save;
	private Button cancel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.configserver);

		sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		String ip = sp.getString("IP", "202.114.106.25");
		String port = sp.getString("port", "9002");
		etIP = (EditText) findViewById(R.id.etIP);
		etPort = (EditText) findViewById(R.id.etPort);
		save = (Button) findViewById(R.id.serverSave);
		cancel = (Button) findViewById(R.id.serverCancel);
		etIP.setText(ip);
		etPort.setText(port);

		save.setOnClickListener(new MyOnClickListener());
		cancel.setOnClickListener(new MyOnClickListener());
		super.onCreate(savedInstanceState);
	}

	class MyOnClickListener implements OnClickListener {
		
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.serverSave:
				String ip = etIP.getText().toString();
				String port = etPort.getText().toString();
				Editor editor = sp.edit();
				if (!"".equals(ip))
					editor.putString("IP", ip);
				if (!"".equals(port))
					editor.putString("port", port);
				editor.commit();
				finish();
				break;

			case R.id.serverCancel:
				finish();
				break;

			}

		}

	}


}
