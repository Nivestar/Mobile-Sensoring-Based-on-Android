package com.zwc.android_weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zwc.util.SendCommand;

public class ClientThread implements Runnable {
	public Handler revHandler;
	private Socket s;
	private OutputStream os = null;
	private Handler handler;
	private BufferedReader br;
	public String messageSend;
	Context context;
	
	ClientThread (Handler handler, Context context){
		this.handler = handler;
		this.context = context;
	}

	@Override
	public void run() {
		try{
			SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
			String ip = sp.getString("IP", "202.114.106.25");
			String  port = sp.getString("port", "9004");
			s = new Socket(ip, Integer.parseInt(port));
			s.setSoTimeout(5*1000);
			os = s.getOutputStream();
			br = new BufferedReader(new InputStreamReader(s.getInputStream(),"utf-8"));
			new Thread(){
				@Override
				public void run(){
					String content = null;
					
					try{
						while ((content = br.readLine()) != null){
							Message msg = new Message();
							msg.what = 0x123;
							msg.obj = content;
							handler.sendMessage(msg);
						}
					}catch (IOException e){
						e.printStackTrace();
					}
				}
			}.start();
			Looper.prepare();
			revHandler = new Handler(){
				@Override
				public void handleMessage(Message msg){
					if(msg.what == SendCommand.SENSORMESSAGE){
						try{
							os.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
							os.flush();
						}catch (Exception e){
							e.printStackTrace();
						}
					} else if (msg.what == SendCommand.PHOTOMESSAGE) {
						try {
							os.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
							os.flush();
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				}
			};
			
			Looper.loop();
			br.close();
			os.close();
			s.close();
			
		}catch (SocketTimeoutException e){
			System.out.println("Socket连接超时");
			
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("无法连接上服务器");
			//无法连上服务器后，3秒后尝试继续连接
			try {
				Thread.sleep(3*1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			run();
		}
		
	}
	

}
