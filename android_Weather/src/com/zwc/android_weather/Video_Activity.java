package com.zwc.android_weather;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zwc.android.http.AsyncHttpClient;
import com.zwc.android.http.AsyncHttpResponseHandler;
import com.zwc.android.http.RequestParams;
/**
 * 拍摄视频Activity
 * @author ZWC
 *
 */
public class Video_Activity extends SherlockActivity implements
		SurfaceHolder.Callback {
	private SurfaceView sView;
	private TextView tvTimer;
	private ImageButton capture, stop;
	private ProgressBar progressBar;
	private SurfaceHolder surfaceHolder;
	private MediaRecorder mediaRecorder;
	private EditText msg_userEdit;//here
	private Button sendButton;//here
	boolean isRecording = false, isPreviewing = false;
	private Camera mCamera;
	private File destFile;
	private LocationClient mLocationClient;
	private double[] locationInfo = new double[2];
	private double[] startLocationInfo = new double[2];
	private double[] endLocationInfo = new double[2];
	private boolean locationFinish = false;
	String startTime;
	String endTime;
	private int recordTime = 0;
	private Timer timer;
	private Handler mHandler;
	private String url;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// 选择支持半透明模式，在有SurfaceView的activity里使用
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.activity_video);
		SharedPreferences sp = getSharedPreferences("config",
				Context.MODE_PRIVATE);
		url = "http://" + sp.getString("IP", "202.114.106.25") + ":"
				+ sp.getString("port", "9002") + "/AndroidSensorReceiver/servlet/UploadServlet";
		sView = (SurfaceView) findViewById(R.id.sView);
		tvTimer = (TextView) findViewById(R.id.tvTimer);
		capture = (ImageButton) findViewById(R.id.capture);
		stop = (ImageButton) findViewById(R.id.stop);
		progressBar = (ProgressBar) findViewById(R.id.video_progressBar);
		msg_userEdit = (EditText) findViewById(R.id.msg_user);  //here
		sendButton=(Button)findViewById(R.id.send_video);//here
		capture.setOnClickListener(new MyOnclickListener());
		stop.setOnClickListener(new MyOnclickListener());
		sendButton.setOnClickListener(new MyOnclickListener());//here
		capture.setEnabled(true);
		stop.setEnabled(false);
		sendButton.setEnabled(false);//here

		initLocation();

	}

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
		// option.setLocationMode(LocationMode.Hight_Accfuracy);
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
		option.setOpenGps(true);// 打开GPS
		option.setScanSpan(3000);// 设置发起定位请求的间隔时间为3000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		if (mLocationClient != null && mLocationClient.isStarted())
			mLocationClient.requestLocation();
		else
			Log.d("LocSDK", "locClient is null or not started");

	}

	@Override
	public void onResume() {
		surfaceHolder = sView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);
		surfaceHolder.setFixedSize(800, 480);
		sView.setKeepScreenOn(true);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
		super.onDestroy();
	}

	public class MyOnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.capture:
				//while(!locationFinish);
				if (!Environment.getExternalStorageState().equals(
						android.os.Environment.MEDIA_MOUNTED)) 
				{
					Toast.makeText(getApplicationContext(), "SD卡不存在，请插入SD卡",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (!isRecording) {
					try {
						startRecording();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				while(!locationFinish){};//here
				startLocationInfo[0]=locationInfo[0];
				startLocationInfo[1]=locationInfo[1];
				
				break;
			case R.id.stop:
				if (isRecording && mediaRecorder != null & destFile != null) 
				{
					SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");//
					endTime = simpleFormat.format(new Date());//
					mediaRecorder.stop();
					mediaRecorder.release();
					mediaRecorder = null;
					mCamera.release();
					mCamera = null;
					timer.cancel();
					recordTime = 0;
					tvTimer.setVisibility(View.GONE);
					tvTimer.setText("00 : 00");
					capture.setEnabled(false);//true-->false
				    sendButton.setEnabled(true);//here
					stop.setEnabled(false);
					msg_userEdit.setVisibility(View.VISIBLE);//可见！！！！！！！
					isRecording = false;
					cameraPreview();
					while(!locationFinish){};//here
					endLocationInfo[0]=locationInfo[0];
					endLocationInfo[1]=locationInfo[1];
				}
				break;
			case R.id.send_video:
					if (locationFinish) {
						try {
							//while(!locationFinish);
							msg_userEdit.setVisibility(View.INVISIBLE);//不可见
							String editStr = msg_userEdit.getText().toString();//here,send
							msg_userEdit.setText("");
							AsyncHttpClient client = new AsyncHttpClient();

							RequestParams params = new RequestParams();
							// 发送视频拍摄时间
							params.put("startTime", startTime);
							params.put("endTime", endTime);
							// 发送纬度和经度
							params.put("startLocation", startLocationInfo[0] + ","
									+ startLocationInfo[1]);
							params.put("endLocation", endLocationInfo[0] + ","
									+ endLocationInfo[1]);
							
							// 发送视频文件
							params.put("photo", destFile);
							params.put("videoEventMsg", editStr);//here,send
							client.post(url, params,
									new AsyncHttpResponseHandler() {

										@Override
										public void onStart() {
											progressBar
													.setVisibility(View.VISIBLE);
											super.onStart();
										}

										@Override
										public void onProgress(
												int bytesWritten, int totalSize) {
											int progress = (int) (bytesWritten
													* 1.0 / totalSize * 100);
											progressBar.setProgress(progress);
										}

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
				break;
			default:
				break;
			}

		}

	}
	
	/**
	 * 设置camera的预览界面
	 */
	private void cameraPreview() {
		if (!isPreviewing) {
			mCamera = Camera.open(0);
			mCamera.setDisplayOrientation(90);
		}
		if (mCamera != null && !isPreviewing) {
			try {

				mCamera.setPreviewDisplay(surfaceHolder);
				Camera.Parameters parameters = mCamera.getParameters();
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreviewing = true;
		}
	}
	
	/**
	 * 开始录像
	 * @throws Exception
	 */
	private void startRecording() throws Exception {
		isPreviewing = false;
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		startTime = simpleFormat.format(new Date());
		destFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES),
				"VID" + startTime + ".mp4");
		mCamera.unlock();
		mediaRecorder = new MediaRecorder();
		mediaRecorder.reset();
		mediaRecorder.setCamera(mCamera);
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mediaRecorder.setAudioSamplingRate(44100);
		mediaRecorder.setAudioEncodingBitRate(128000);
		mediaRecorder.setVideoSize(1280, 720); 
		mediaRecorder.setVideoEncodingBitRate(3000000);
		mediaRecorder.setVideoFrameRate(30);
		mediaRecorder.setOrientationHint(90);
		mediaRecorder.setOutputFile(destFile.getAbsolutePath());
		mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
		mediaRecorder.prepare();
		mediaRecorder.start();
		Toast.makeText(getApplicationContext(), "开始录像", Toast.LENGTH_SHORT)
				.show();
		startTimer();
		capture.setEnabled(false);
		sendButton.setEnabled(false);//here
		stop.setEnabled(true);
		isRecording = true;
	}
	
	/**
	 * 开始对录像计时显示
	 */
	private void startTimer() {
		tvTimer.setVisibility(View.VISIBLE);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x111) {
					recordTime++;
					int recordSeconds = recordTime % 60;
					int recordMinutes = recordTime / 60;
					StringBuffer sb = new StringBuffer();
					if (recordMinutes < 10)
						sb.append("0" + Integer.toString(recordMinutes));
					else
						sb.append(Integer.toString(recordMinutes));
					sb.append(" : ");
					if (recordSeconds < 10)
						sb.append("0" + Integer.toString(recordSeconds));
					else
						sb.append(Integer.toString(recordSeconds));
					tvTimer.setText(sb.toString());
				}
				super.handleMessage(msg);
			}

		};
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (mHandler != null) {
					mHandler.sendEmptyMessage(0x111);
				}

			}

		}, 1000, 1000);

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		surfaceHolder = holder;

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		cameraPreview();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mediaRecorder != null) {
			mediaRecorder.stop();
			mediaRecorder.release();
			mediaRecorder = null;
			timer.cancel();
			recordTime = 0;
			tvTimer.setVisibility(View.GONE);
			tvTimer.setText("00 : 00");
			capture.setEnabled(true);
			stop.setEnabled(false);
			//sendButton.setEnabled(false);//here
			isRecording = false;
		}
		if (mCamera == null){
			mCamera.stopPreview();
			isPreviewing = false;
			mCamera.release();
			mCamera = null;
		}
		
	}
	
	//屏幕触摸时调用
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {// 按下时自动对焦
			mCamera.autoFocus(null);
		}
		return true;
	}

}
