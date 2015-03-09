package com.zwc.android_weather;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zwc.fragment.MainCheckFragment;
import com.zwc.fragment.MainSensorFragment;
import com.zwc.util.DeleteFileOrDirectory;

/**
 * ������Activity��Ƕ����ViewPager����������Fragment
 * 
 * @author ZWC
 * 
 */
public class MainActivity extends SherlockFragmentActivity implements
		ActionBar.TabListener {
	private ViewPager viewPager;
	private ActionBar actionBar;
	private SharedPreferences sp;

	private boolean isFinished = false;

	@Override
	protected void onCreate(Bundle savedInstanceBundle) {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main_tab);

		if (!isNetworkConnect(getApplicationContext())) {
			showNetworkAlert(this);
		}

		// �жϳ����Ƿ��ǵ�һ��ִ��
		sp = getSharedPreferences("settings", 0);
		boolean isFirstRun = sp.getBoolean("isFirstRun", true);
		if (isFirstRun) {// ��һ������
			Editor editor = sp.edit();
			editor.putBoolean("isFirstRun", false);
			editor.commit();
			InputStream is = getResources().openRawResource(R.raw.weather);
			File dababaseDir = new File("/data/data/" + getPackageName()
					+ "/databases");

			if (!dababaseDir.exists())
				dababaseDir.mkdir();

			// Log.d("weather", dababaseDir.getAbsolutePath());

			try {
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(dababaseDir.getAbsoluteFile()
								+ "/areaid"));
				byte[] buffer = new byte[1024];
				while (is.read(buffer) != -1) {
					bos.write(buffer);
				}
				bos.flush();
				bos.close();
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// ��ȡSherlockActionBar��ʵ��
		actionBar = getSupportActionBar();
		// �����������һ�εĽ�������
		if (savedInstanceBundle != null) {
			// ���õ�ǰTab���ڵ�ҳ��Ϊ��һ�δ洢��Tabҳ��
			actionBar.setSelectedNavigationItem(savedInstanceBundle
					.getInt("tab"));
		}
		// ��ȡ���fragment��ViewPager
		viewPager = (ViewPager) findViewById(R.id.main_pager);
		// �½�ViewPager��������,���캯��Ҫ���ݽ�һ��FragmentManager������д�������ļ�������
		FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(
				getSupportFragmentManager()) {

			// ������������������fragment
			@Override
			public int getCount() {
				return 2;
			}

			// ����fragment���ڵ�λ��
			@Override
			public Fragment getItem(int position) {
				switch (position) {
				case 0:
					return new MainSensorFragment();

				case 1:
					MainCheckFragment fragment = new MainCheckFragment();
					return fragment;
				}
				return null;
			}

			// ����fragment�ı���
			@Override
			public CharSequence getPageTitle(int position) {
				switch (position) {
				case 0:
					return "���ʹ�������";

				case 1:
					return "�鿴������������";
				}

				return super.getPageTitle(position);
			}

		};
		// ΪviewPager����������
		viewPager.setAdapter(pagerAdapter);
		// ����viewPagerҳ��ı�ļ�����������ActionBar�ı�ǩ�ƶ�����λ��
		viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						super.onPageSelected(position);
					}

				});

		// ����ActionBar�ĵ���ģʽΪ��ǩ����
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (int i = 0; i < 2; i++) {
			// �½�һ��tab
			ActionBar.Tab tab = actionBar.newTab();
			// ����tab�ı���
			tab.setText(pagerAdapter.getPageTitle(i));
			// ����tab�ļ�����
			tab.setTabListener(this);
			// ��Ӹ�tab
			actionBar.addTab(tab);
		}

		super.onCreate(savedInstanceBundle);
	}

	// ���浱ǰ״̬
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", actionBar.getSelectedNavigationIndex());
	}

	// �����˵�
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem deleteItem = menu.add(0, 1, 0, "������м�¼");
		deleteItem.setIcon(R.drawable.delete);
		deleteItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem serverItem = menu.add("���÷�������ַ");
		serverItem.setIcon(R.drawable.server);
		serverItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		// ֱ�����øò˵����intent,��ĳ��Activity
		serverItem.setIntent(new Intent(this, ConfigServer.class));

		return super.onCreateOptionsMenu(menu);
	}

	// �˵����ʱ���õķ���
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// ��ռ�¼
		case 1:
			// �����Ի���
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
					MainActivity.this);
			dialogBuilder
					.setTitle("�Ƿ�������м�¼?")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									getFilesDir();

									DeleteFileOrDirectory
											.delete(getExternalFilesDir(null));
								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							});
			dialogBuilder.create().show();

			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// ���·��ؼ�ʱ���á�
	@Override
	public void onBackPressed() {
		// 2��������������زſ����˳�
		if (isFinished) {
			finish();
		} else {
			Toast.makeText(this, "�ٰ�һ�η��ؼ�, ȷ���˳�", Toast.LENGTH_SHORT).show();
			isFinished = true;
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					isFinished = false;

				}

			}, 2000);
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	/**
	 * ��鵱ǰ����״̬
	 * 
	 * @param context
	 * @return
	 */
	private static boolean isNetworkConnect(Context context) {
		boolean isConnect = false;
		ConnectivityManager connmgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connmgr.getActiveNetworkInfo();
		if (networkInfo != null) {
			isConnect = connmgr.getActiveNetworkInfo().isAvailable();
		}
		return isConnect;
	}

	// ������ʱ����ʾ�������öԻ���
	private static void showNetworkAlert(final Context context) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

		alertBuilder
				.setTitle("����������ʾ")
				.setMessage("���������ӣ��Ƿ�����������ã�")
				.setPositiveButton("����", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent networkIntent = null;
						// ��ȡϵͳ�汾����API>10������3.0�����ϰ汾
						if (android.os.Build.VERSION.SDK_INT > 10) {
							networkIntent = new Intent(
									android.provider.Settings.ACTION_WIRELESS_SETTINGS);
						} else {
							ComponentName comp = new ComponentName(
									"com.android.settings",
									"com.android.provider.settings.WirelessSettings");
							networkIntent = new Intent();
							networkIntent.setComponent(comp);
							networkIntent
									.setAction("android.intent.action.VIEW");
						}
						context.startActivity(networkIntent);
					}
				})
				.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

}
