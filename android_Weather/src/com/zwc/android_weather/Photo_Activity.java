package com.zwc.android_weather;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.Header;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zwc.android.http.AsyncHttpClient;
import com.zwc.android.http.AsyncHttpResponseHandler;
import com.zwc.android.http.RequestParams;

/**
 * 拍照Activity
 * 
 * @author ZWC
 * 
 */
public class Photo_Activity extends SherlockActivity {
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	private Uri tempFileUri;
	private ImageView imageView;
	private EditText msg_userET;
	private Button send, delete, capture;
	private TextView tvpPotoDate, tvPhotoPosition;
	private ProgressBar progressBar;
	private int screenWidth, screenHeight;
	private SimpleDateFormat simpleFormat = new SimpleDateFormat(
			"yyyyMMdd_HHmmss");
	private Bitmap photo;
	private LocationClient mLocationClient;
	private double[] locationInfo = new double[2];
	private boolean locationFinish = false;
	private String photoDate, photoPosition;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_photo);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		SharedPreferences sp = getSharedPreferences("config",
				Context.MODE_PRIVATE);
		// 设置发送的服务器URL地址
		url = "http://" + sp.getString("IP", "202.114.106.25") + ":"
				+ sp.getString("port", "9002")
				+ "/AndroidSensorReceiver/servlet/UploadServlet";
		findViewById(R.id.photo_info_linear).setVisibility(View.GONE);
		imageView = (ImageView) findViewById(R.id.photo);
		send = (Button) findViewById(R.id.send_picture);
		progressBar = (ProgressBar) findViewById(R.id.photo_progressBar);
		delete = (Button) findViewById(R.id.cancel_picture);
		capture = (Button) findViewById(R.id.capture_photo);
		tvpPotoDate = (TextView) findViewById(R.id.photo_date);
		tvPhotoPosition = (TextView) findViewById(R.id.photo_position);
		msg_userET = (EditText) findViewById(R.id.msg_user);
		

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (fileUri != null) {
					File imageFile = new File(fileUri.getPath());
					if (imageFile.exists()) {
						try {
							String editStr = msg_userET.getText().toString();
							msg_userET.setText("");
							// 发送异步HTTP请求（发送请求信息和文件），用到了AsyncHttp框架
							AsyncHttpClient client = new AsyncHttpClient();
							RequestParams params = new RequestParams();
							// 向请求中加入文件
							params.put("photo", imageFile);
							params.put("picEventMsg", editStr);
							// 发送POST请求
							client.post(url, params,
									new AsyncHttpResponseHandler() {

										@Override
										public void onStart() {
											progressBar
													.setVisibility(View.VISIBLE);
											super.onStart();
										}

										// 上传状态，更新进度条
										@Override
										public void onProgress(
												int bytesWritten, int totalSize) {
											int progress = (int) (bytesWritten
													* 1.0 / totalSize * 100);
											progressBar.setProgress(progress);
										}

										// 上传成功
										@Override
										public void onSuccess(int statusCode,
												Header[] headers,
												byte[] responseBody) {
											progressBar
													.setVisibility(View.GONE);
											Toast.makeText(
													getApplicationContext(),
													"上传完毕", Toast.LENGTH_SHORT)
													.show();
										}

										// 上传失败
										@Override
										public void onFailure(int statusCode,
												Header[] headers,
												byte[] responseBody,
												Throwable error) {
											Toast.makeText(
													getApplicationContext(),
													"上传失败，请重新发送",
													Toast.LENGTH_SHORT).show();

										}
									});
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
			}
		});
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				msg_userET.setText("");
				if (fileUri != null) {
					File imageFile = new File(fileUri.getPath());
					if (imageFile.exists()) {

						imageFile.delete();
						imageView.setImageBitmap(null);
						findViewById(R.id.photo_info_linear).setVisibility(
								View.GONE);
						progressBar.setVisibility(View.GONE);

					}
				}
			}
		});

		capture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (fileUri != null) {
					tempFileUri = fileUri;
				}
				fileUri = getFileOutputUri();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(intent,
						CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
		});

		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getWidth();
		// 初始化定位服务
		initLocation();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("上传列表").setIcon(R.drawable.upload_icon)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 根据double类型的经纬度，计算出相应的度、分、秒
	 * 
	 * @param gpsInfo
	 * @return
	 */
	private String gpsInfoConvert(double gpsInfo) {
		gpsInfo = Math.abs(gpsInfo);
		String dms = Location.convert(gpsInfo, Location.FORMAT_SECONDS);
		String[] splits = dms.split(":");
		// splits[2]为秒，只有它可能为小数
		// 获取秒的整数值
		String seconds = Math.floor(Double.parseDouble(splits[2])) + "";
		// EXIf的GPS信息格式为：秒/1 分/1 度/1
		return seconds + "/1," + splits[1] + "/1," + splits[0] + "/1";
	}

	/**
	 * 初始化定位服务
	 */
	private void initLocation() {

		// 定位服务初始化
		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.registerLocationListener(new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation location) {
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
		option.setScanSpan(30000);// 设置发起定位请求的间隔时间为30000ms
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
	 * 获取activity返回来的数据，即拍照完成后调用的方法
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 照片如果设置了指定路径，返回的data等于null；如果没有设置存储路径，data不等于null
		if (resultCode == RESULT_OK) {
			if (data == null & locationInfo != null) {
				try {
					if (locationFinish) {
						//
						ExifInterface exifInterface = new ExifInterface(
								fileUri.getPath());
						exifInterface.setAttribute(
								ExifInterface.TAG_GPS_LATITUDE_REF,
								locationInfo[0] > 0 ? "N" : "S");
						exifInterface.setAttribute(
								ExifInterface.TAG_GPS_LATITUDE,
								gpsInfoConvert(locationInfo[0]));
						exifInterface.setAttribute(
								ExifInterface.TAG_GPS_LONGITUDE_REF,
								locationInfo[1] > 0 ? "E" : "W");
						exifInterface.setAttribute(
								ExifInterface.TAG_GPS_LONGITUDE,
								gpsInfoConvert(locationInfo[1]));
						// 这句话很重要，一定要加上
						exifInterface.saveAttributes();
						// 显示图片插入的信息
						photoDate = exifInterface
								.getAttribute(ExifInterface.TAG_DATETIME);
						photoPosition = "纬度：" + locationInfo[0] + "\n经度："
								+ locationInfo[1];
						File imageFile = new File(fileUri.getPath());
						String parentPath = imageFile.getParent();
						File newFile = new File(parentPath, "JPG"
								+ simpleFormat.format(new Date()) + ".jpg");
						imageFile.renameTo(newFile);
						fileUri = Uri.fromFile(newFile);
						exifInterface = null;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 加载图片选项
				BitmapFactory.Options opts = new BitmapFactory.Options();
				// 不去加載整个图片，而只去读取图片的首部信息，获取图片宽高
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(fileUri.getPath(), opts);
				int imageWidth = opts.outWidth;
				int imageHeight = opts.outHeight;
				int scale = Math.max(imageWidth / screenWidth, imageHeight
						/ screenHeight);
				// 读取图片整个信息
				opts.inJustDecodeBounds = false;
				// 采样率
				opts.inSampleSize = scale;
				photo = BitmapFactory.decodeFile(fileUri.getPath(), opts);
				imageView.setImageBitmap(photo);
				tvpPotoDate.setText(photoDate);
				tvPhotoPosition.setText(photoPosition);
				findViewById(R.id.photo_info_linear)
						.setVisibility(View.VISIBLE);

			}
		} else {
			if (tempFileUri != null)
				fileUri = tempFileUri;
		}

	}

	/**
	 * 获取存放图片位置的URI
	 * 
	 * @return Uri
	 */
	private Uri getFileOutputUri() {
		String sdCardStatus = Environment.getExternalStorageState();
		if (sdCardStatus.equals(Environment.MEDIA_MOUNTED)) {
			File photoDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			File photo = new File(photoDirectory, "JPG"
					+ simpleFormat.format(new Date()) + ".jpg");
			return Uri.fromFile(photo);
		}

		else {
			File photoDirectory = getFilesDir();
			File photo = new File(photoDirectory, "JPG"
					+ simpleFormat.format(new Date()) + ".jpg");
			return Uri.fromFile(photo);
		}

	}
}
