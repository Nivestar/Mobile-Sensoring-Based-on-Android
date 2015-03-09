package com.zwc.android_weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
/**
 * 发送传感器数据Activity
 * @author ZWC
 *
 */
public class SendSensor_Activity extends SherlockActivity implements
		SensorEventListener, ActionBar.OnNavigationListener {
	private SensorManager sensorManager;
	private WakeLock mWakeLock;
	private TelephonyManager telephonyManager;
	private float[] accSensorValue = new float[3];
	private float[] magSensorValue = new float[3];
	TextView tvLocation, tvAcce, tvOrientation, tvMagnetic, tvTemp, tvLight,
			tvGravity, tvGyroscope;
	private Button bdMapButton;

	String[] SensorInfo = new String[] { "", "", "", "", "" };
	private double[] locationInfo = new double[2];
	private Timer timer;
	private LocationClient mLocationClient;
	private boolean locationFinish = false;
	private long sendPeroid;
	private boolean isRunning = false;
	private String url;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 使用电源管理器获取到WakeLock实例
		PowerManager powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"My Lock");
		// 使mWakeLock生效，屏幕锁定时传感器能继续工作
		mWakeLock.acquire();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("发送传感数据到服务器");
		// 设置发送周期的list选项
		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				context, R.array.sendPeroid, R.layout.sherlock_spinner_item);
		//设置下拉的样式
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		//设置导航方式为List的方式
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		//设置List导航的监听器
		getSupportActionBar().setListNavigationCallbacks(list, this);
		//设置默认list导航的位置
		getSupportActionBar().setSelectedNavigationItem(1);
		//从SharePreferences获取数据，并设置服务器URL
		SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
		url = "http://" + sp.getString("IP", "202.114.106.25") + ":"
				+ sp.getString("port", "9002") + "/AndroidSensorReceiver/servlet/UploadServlet";
		timer = new Timer();
		
		initLocation();

		setContentView(R.layout.activity_send_sensor);

		tvLocation = (TextView) findViewById(R.id.tvLocation);
		bdMapButton = (Button) findViewById(R.id.bdMap);
		tvAcce = (TextView) findViewById(R.id.tvAcce);
		tvOrientation = (TextView) findViewById(R.id.tvOrientation);
		tvMagnetic = (TextView) findViewById(R.id.tvMagnetic);
		tvTemp = (TextView) findViewById(R.id.tvTemp);
		tvLight = (TextView) findViewById(R.id.tvLight);
		tvGravity = (TextView) findViewById(R.id.tvGravity);
		tvGyroscope = (TextView) findViewById(R.id.tvGyroscope);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		initSensor();
		// 初始化手机信息管理器，为了获取到手机IMEI信息
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		bdMapButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent bdMapIntent = new Intent(getApplicationContext(),
						GPS_Activity.class);
				bdMapIntent.putExtra("source", "BD");
				startActivity(bdMapIntent);
			}

		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	private void initLocation() {

		// 定位服务初始化
		mLocationClient = new LocationClient(getApplicationContext());
		//注册定位服务的监听器
		mLocationClient.registerLocationListener(new BDLocationListener() {
			
			//接受到位置信息时调用
			@Override
			public void onReceiveLocation(BDLocation location) {
				tvLocation.setText("定位成功，信息如下" + "\n经度："
						+ location.getLatitude() + "\n纬度："
						+ location.getLongitude() + "\n地址："
						+ location.getAddrStr());
				locationInfo[0] = location.getLatitude();
				locationInfo[1] = location.getLongitude();
				locationFinish = true;

			}

			@Override
			public void onReceivePoi(BDLocation location) {

			}

		});
		// 设置定位选项
		LocationClientOption option = new LocationClientOption();
		// option.setLocationMode(LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
		option.setOpenGps(true);// 打开GPS
		option.setScanSpan(2000);// 设置发起定位请求的间隔时间为2000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		if (mLocationClient != null && mLocationClient.isStarted())
			mLocationClient.requestLocation();
		else
			Log.d("LocSDK", "locClient is null or not started");

	}
	/**
	 * 为所需要的传感器注册监听
	 */
	private void initSensor() {
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_UI);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_UI);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_UI);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE),
				SensorManager.SENSOR_DELAY_UI);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
				SensorManager.SENSOR_DELAY_UI);

	}

	@Override
	public void onDestroy() {
		mWakeLock.release();
		if (timer != null)
			timer.cancel();
		sensorManager.unregisterListener(this);
		sensorManager = null;
		super.onDestroy();
	}
	
	
	//传感器信息发生变化时调用
	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] values = event.values;
		int sensorType = event.sensor.getType();
		StringBuilder sb = null;
		switch (sensorType) {
		case Sensor.TYPE_ACCELEROMETER:
			accSensorValue = values;

			sb = new StringBuilder();
			sb.append("X方向上的加速度：");
			sb.append(values[0]);
			sb.append("\nY方向的加速度：");
			sb.append(values[1]);
			sb.append("\nZ方向的加速度：");
			sb.append(values[2]);
			tvAcce.setText(sb.toString());
			SensorInfo[0] = "0" + "#" + values[0] + "#" + values[1] + "#"
					+ values[2];

			float R[] = new float[9];
			float a[] = new float[3];
			sensorManager.getRotationMatrix(R, null, accSensorValue,
					magSensorValue);
			sensorManager.getOrientation(R, a);
			a[0] = (float) Math.toDegrees(a[0]);
			a[1] = (float) Math.toDegrees(a[1]);
			a[2] = (float) Math.toDegrees(a[2]);
			sb = new StringBuilder();
			sb.append("绕Z轴转过的角度：");
			sb.append(a[0]);
			sb.append("\n绕Y轴转过的角度：");
			sb.append(a[1]);
			sb.append("\n绕X轴转过的角度：");
			sb.append(a[2]);
			tvOrientation.setText(sb.toString());
			SensorInfo[1] = "1" + "#" + a[0] + "#" + a[1] + "#" + a[2];

			break;

		case Sensor.TYPE_GYROSCOPE:
			sb = new StringBuilder();
			sb.append("沿X轴旋转的角速度：");
			sb.append(values[0]);
			sb.append("\n沿Y轴旋转的角速度：");
			sb.append(values[1]);
			sb.append("\n沿Z轴旋转的角速度：");
			sb.append(values[2]);
			tvGyroscope.setText(sb.toString());
			SensorInfo[2] = "2" + "#" + values[0] + "#" + values[1] + "#"
					+ values[2];
			break;
		case Sensor.TYPE_GRAVITY:
			sb = new StringBuilder();
			sb.append("沿X轴方向的重力：");
			sb.append(values[0]);
			sb.append("\n沿y轴方向的重力：");
			sb.append(values[1]);
			sb.append("\n沿z轴方向的重力：");
			sb.append(values[2]);
			tvGravity.setText(sb.toString());
			SensorInfo[3] = "3" + "#" + values[0] + "#" + values[1] + "#"
					+ values[2];
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			magSensorValue = values;
			sb = new StringBuilder();
			sb.append("X方向上的磁场强度：");
			sb.append(values[0]);
			sb.append("\nY方向上的磁场强度：");
			sb.append(values[1]);
			sb.append("\nZ方向上的磁场强度：");
			sb.append(values[2]);
			tvMagnetic.setText(sb.toString());
			SensorInfo[4] = "4" + "#" + values[0] + "#" + values[1] + "#"
					+ values[2];
			break;
		case Sensor.TYPE_TEMPERATURE:
			sb = new StringBuilder();
			sb.append("当前温度为：");
			sb.append(values[0]);
			tvTemp.setText(sb.toString());
			break;
		case Sensor.TYPE_LIGHT:
			sb = new StringBuilder();
			sb.append("当前光强为：");
			sb.append(values[0] + "lux(勒克斯)");
			tvLight.setText(sb.toString());
			break;
		}

	}
	
	/**
	 * 周期性发送传感器数据
	 * @param peroid 发送周期
	 */
	private void sendMessage(final long peroid) {

		if (isRunning) {
			timer.cancel();
			timer = new Timer();
		}

		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (locationFinish) { // 发送传感器数据
					try {
						StringBuilder sBuilder = new StringBuilder();
						String imei = telephonyManager.getDeviceId();
						sBuilder.append(imei + "|");
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
								"yyyyMMdd_HHmmss");
						sBuilder.append(simpleDateFormat.format(new Date())
								+ "|");
						sBuilder.append(locationInfo[0] + "," + locationInfo[1]);
						sBuilder.append("|" + SensorInfo[0]);
						for (int i = 1; i < 5; i++)
							sBuilder.append("," + SensorInfo[i]);

						HttpClient client = new DefaultHttpClient();
						HttpPost post = new HttpPost(url);
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("sensor", sBuilder
								.toString()));
						post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
						client.execute(post);
					} catch (Exception e) {
						System.out.println("无法连接服务器");
						e.printStackTrace();
					}

					System.out.println("hahahaha");

					isRunning = true;
				}

			}

		}, 1000, sendPeroid);

	}

	// ActionBar的list导航的回调函数（选择发送周期）
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		switch (itemPosition) {
		case 0:

			sendPeroid = 30 * 1000;
			sendMessage(sendPeroid);

			break;
		case 1:
			sendPeroid = 1 * 60 * 1000;
			sendMessage(sendPeroid);
			break;
		case 2:
			sendPeroid = 5 * 60 * 1000;
			sendMessage(sendPeroid);
			break;
		case 3:
			sendPeroid = 10 * 60 * 1000;
			sendMessage(sendPeroid);
			break;

		default:
			break;
		}

		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
}
