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
 * 主界面Activity，嵌套了ViewPager，包含两个Fragment
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

		// 判断程序是否是第一次执行
		sp = getSharedPreferences("settings", 0);
		boolean isFirstRun = sp.getBoolean("isFirstRun", true);
		if (isFirstRun) {// 第一次运行
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
		// 获取SherlockActionBar的实例
		actionBar = getSupportActionBar();
		// 如果保存有上一次的界面内容
		if (savedInstanceBundle != null) {
			// 设置当前Tab所在的页面为上一次存储的Tab页面
			actionBar.setSelectedNavigationItem(savedInstanceBundle
					.getInt("tab"));
		}
		// 获取存放fragment的ViewPager
		viewPager = (ViewPager) findViewById(R.id.main_pager);
		// 新建ViewPager的适配器,构造函数要传递进一个FragmentManager。并重写适配器的几个方法
		FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(
				getSupportFragmentManager()) {

			// 设置适配器包含两个fragment
			@Override
			public int getCount() {
				return 2;
			}

			// 设置fragment所在的位置
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

			// 设置fragment的标题
			@Override
			public CharSequence getPageTitle(int position) {
				switch (position) {
				case 0:
					return "发送传感数据";

				case 1:
					return "查看天气与空气情况";
				}

				return super.getPageTitle(position);
			}

		};
		// 为viewPager设置适配器
		viewPager.setAdapter(pagerAdapter);
		// 设置viewPager页面改变的监听器，并将ActionBar的标签移动到该位置
		viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						super.onPageSelected(position);
					}

				});

		// 设置ActionBar的导航模式为标签导航
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (int i = 0; i < 2; i++) {
			// 新建一个tab
			ActionBar.Tab tab = actionBar.newTab();
			// 设置tab的标题
			tab.setText(pagerAdapter.getPageTitle(i));
			// 设置tab的监听器
			tab.setTabListener(this);
			// 添加该tab
			actionBar.addTab(tab);
		}

		super.onCreate(savedInstanceBundle);
	}

	// 保存当前状态
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", actionBar.getSelectedNavigationIndex());
	}

	// 创建菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem deleteItem = menu.add(0, 1, 0, "清空所有记录");
		deleteItem.setIcon(R.drawable.delete);
		deleteItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem serverItem = menu.add("设置服务器地址");
		serverItem.setIcon(R.drawable.server);
		serverItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		// 直接设置该菜单项的intent,打开某个Activity
		serverItem.setIntent(new Intent(this, ConfigServer.class));

		return super.onCreateOptionsMenu(menu);
	}

	// 菜单点击时调用的方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// 清空记录
		case 1:
			// 构建对话框
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
					MainActivity.this);
			dialogBuilder
					.setTitle("是否清空所有记录?")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									getFilesDir();

									DeleteFileOrDirectory
											.delete(getExternalFilesDir(null));
								}
							})
					.setNegativeButton("取消",
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

	// 按下返回键时调用。
	@Override
	public void onBackPressed() {
		// 2秒内连续点击返回才可以退出
		if (isFinished) {
			finish();
		} else {
			Toast.makeText(this, "再按一次返回键, 确认退出", Toast.LENGTH_SHORT).show();
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
	 * 检查当前网络状态
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

	// 无网络时，显示网络设置对话框
	private static void showNetworkAlert(final Context context) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

		alertBuilder
				.setTitle("网络设置提示")
				.setMessage("无网络连接，是否进行网络设置？")
				.setPositiveButton("设置", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent networkIntent = null;
						// 获取系统版本，即API>10，就是3.0及以上版本
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
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

}
