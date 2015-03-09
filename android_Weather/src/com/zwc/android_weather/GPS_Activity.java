package com.zwc.android_weather;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.StaticLayout;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.MyLocationOverlay.LocationMode;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class GPS_Activity extends SherlockActivity implements SensorEventListener {

	TextView tvGPS;
	LocationManager lm;
	Location location;
	LocationListener listener;
	String bestProvider;
	int gpsCount = 0;
	private List<GpsSatellite> gpsList = new ArrayList<GpsSatellite>();
	MapView mapView = null;
	BMapManager mapMan = null;
	MapController mapController;
	LocationClient mLocationClient = null;
	LocationData locData;
	MyLocationListener mBDListener = new MyLocationListener();
	MyLocationOverlay mLocationOverlay = null;
	private boolean isFirstLoc = true;
	private ClientThread clientThread;
	// ������������
	private SensorManager sensorManager;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("λ����ʾ");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		final Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// do nothing
			}
		};

		clientThread = new ClientThread(mHandler, this);
		new Thread(clientThread).start();

		mapMan = new BMapManager(getApplication());
		mapMan.init(null);
		// ������setContentView֮ǰ�������Ƚ���BMapManager�ĳ�ʼ��������ᱨ��
		setContentView(R.layout.activity_gps);
		tvGPS = (TextView) findViewById(R.id.tvGPS);
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(17);
		mapController.enableClick(true);
		locData = new LocationData();
		// ��λͼ���ʼ��
		mLocationOverlay = new MyLocationOverlay(mapView);
		// ���ö�λ����
		mLocationOverlay.setData(locData);
		// ��Ӷ�λͼ��
		mapView.getOverlays().add(mLocationOverlay);
		mLocationOverlay.enableCompass();
		mLocationOverlay.setLocationMode(LocationMode.FOLLOWING);
		Intent intent = getIntent();
		mapView.refresh();

		if (intent.getStringExtra("source").equals("GPS")) {
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			lm.addGpsStatusListener(new GpsStatus.Listener() {
				// GPS״̬�����ı�ʱ�ص��ú���,��������Ŀ�ı�
				@Override
				public void onGpsStatusChanged(int event) {
					GpsStatus status = lm.getGpsStatus(null);// ��ȡ��ǰ״̬
					gpsCount = updateGpsStatus(event, status);
				}
			});
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(true);
			criteria.setBearingRequired(true);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			bestProvider = lm.getBestProvider(criteria, true);
			Toast.makeText(this, bestProvider, Toast.LENGTH_SHORT).show();
			location = lm.getLastKnownLocation(bestProvider);
			updateView(location);
			listener = new LocationListener() {

				@Override
				public void onLocationChanged(Location location) {
					updateView(location);
				}

				@Override
				public void onProviderDisabled(String provider) {
					// updateView(null);

				}

				@Override
				public void onProviderEnabled(String provider) {
					updateView(lm.getLastKnownLocation(provider));
				}

				@Override
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					// TODO Auto-generated method stub

				}

			};
			lm.requestLocationUpdates(bestProvider, 5000, 0, listener);
		} else if (intent.getStringExtra("source").equals("BD")) {
			// ���ö�λѡ��
			LocationClientOption option = new LocationClientOption();
			// option.setLocationMode(LocationMode.Hight_Accuracy);
			option.setCoorType("bd09ll");// ���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
			option.setOpenGps(true);// ��GPS
			option.setScanSpan(3000);// ���÷���λ����ļ��ʱ��Ϊ3000ms
			option.setIsNeedAddress(true);// ���صĶ�λ���������ַ��Ϣ
			option.setNeedDeviceDirect(true);// ���صĶ�λ��������ֻ���ͷ�ķ���

			// ��λ�����ʼ��
			mLocationClient = new LocationClient(getApplicationContext());
			mLocationClient.registerLocationListener(new BDLocationListener() {
				@Override
				public void onReceiveLocation(BDLocation location) {
					if (location != null) {
						locData.latitude = location.getLatitude();
						locData.longitude = location.getLongitude();
						// �������ʾ��λ����Ȧ����accuracy��ֵΪ0����
						locData.accuracy = location.getRadius();
						// �˴��������� locData�ķ�����Ϣ, �����λ SDK
						// δ���ط�����Ϣ���û������Լ�ʵ�����̹�����ӷ�����Ϣ��
						// locData.direction = 0;
						// ����λ��ͼ��
						mLocationOverlay.setData(locData);
						// ����ͼ������ִ��ˢ�º���Ч
						mapView.refresh();
						// ���ֶ�����������״ζ�λʱ���ƶ�����λ��
						if (isFirstLoc) {
							// �ƶ���ͼ����λ��
							Toast.makeText(getApplicationContext(),
									location.getAddrStr(), Toast.LENGTH_SHORT)
									.show();
							mapController.animateTo(new GeoPoint(
									(int) (locData.latitude * 1e6),
									(int) (locData.longitude * 1e6)));
							// isRequest = false;
							// myLocationOverlay.setLocationMode(LocationMode.FOLLOWING);
							// requestLocButton.setText("����");
							// mCurBtnType = E_BUTTON_TYPE.FOLLOW;
						}
						// �״ζ�λ���
						isFirstLoc = false;

					}
				}

				@Override
				public void onReceivePoi(BDLocation arg0) {
					return;

				}

			});
			mLocationClient.setLocOption(option);
			mLocationClient.start();
			if (mLocationClient != null && mLocationClient.isStarted())
				mLocationClient.requestLocation();
			else
				Log.d("LocSDK", "locClient is null or not started");

		}
	}
	
	

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}



	@Override
	protected void onDestroy() {
		mapView.destroy();
		if (mapMan != null) {
			mapMan.destroy();
			mapMan = null;
		}
		if (listener != null)
			lm.removeUpdates(listener);
		if (mLocationClient != null)
			mLocationClient.stop();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mapView.onPause();
		if (mapMan != null) {
			mapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mapView.onResume();
		if (mapMan != null) {
			mapMan.start();
		}
		super.onResume();

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);
	}

	public void updateView(Location location) {
		if (location != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("GPS��Ϣ��\nά�ȣ�");
			sb.append(location.getLatitude());
			sb.append("\n���ȣ�");
			sb.append(location.getLongitude());
			sb.append("\n�߶ȣ�");
			sb.append(location.getAltitude());
			sb.append("\n����");
			sb.append(location.getBearing());
			sb.append("\n��ǰ�ѵ�������������" + gpsCount);
			tvGPS.setText(sb.toString());
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			mLocationOverlay.setData(locData);
			mapView.refresh();
			GeoPoint geoPoint = new GeoPoint((int) (locData.latitude * 1E6),
					(int) (locData.longitude * 1E6));
			mapController.animateTo(geoPoint);
			try {
				Message msg = new Message();
				msg.what = 0x456;
				msg.obj = sb.toString();
				if (clientThread.revHandler != null) {
					clientThread.revHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			tvGPS.setText("���ڶ�λ�У����Ժ򡣡���");
		}
	}

	private int updateGpsStatus(int event, GpsStatus status) {
		int count = 0;
		if (status == null) {
			count = 0;
		} else {
			if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
				gpsList.clear();
				Iterator<GpsSatellite> itr = status.getSatellites().iterator();
				while (itr.hasNext()) {
					GpsSatellite s = itr.next();
					gpsList.add(s);
					count++;
				}
			}
		}
		return count;
	}

	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			// �������ʾ��λ����Ȧ����accuracy��ֵΪ0����
			locData.accuracy = location.getRadius();
			// �˴��������� locData�ķ�����Ϣ, �����λ SDK δ���ط�����Ϣ���û������Լ�ʵ�����̹�����ӷ�����Ϣ��
			// locData.direction = location.getDerect();
			// ���¶�λ����
			mLocationOverlay.setData(locData);
			// ����ͼ������ִ��ˢ�º���Ч
			mapView.refresh();
			// ���ֶ�����������״ζ�λʱ���ƶ�����λ��
			if (isFirstLoc) {
				// �ƶ���ͼ����λ��
				Toast.makeText(GPS_Activity.this, location.getAddrStr(),
						Toast.LENGTH_SHORT).show();
				mapController.animateTo(new GeoPoint(
						(int) (locData.latitude * 1e6),
						(int) (locData.longitude * 1e6)));
				/*
				 * isRequest = false;
				 * myLocationOverlay.setLocationMode(LocationMode.FOLLOWING);
				 * requestLocButton.setText("����"); mCurBtnType =
				 * E_BUTTON_TYPE.FOLLOW;
				 */
			}
			// �״ζ�λ���
			isFirstLoc = false;
		}

		@Override
		public void onReceivePoi(BDLocation location) {
			if (location == null) {
				return;
			}

		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float values[] = event.values;
		int sensorType = event.sensor.getType();
		switch (sensorType) {
		case Sensor.TYPE_ORIENTATION:
			locData.direction = values[0];
			break;
		}
	}

}
