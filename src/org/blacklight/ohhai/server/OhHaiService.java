package org.blacklight.ohhai.server;

import org.blacklight.ohhai.socket.*;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class OhHaiService extends IntentService {
	private int listenPort;
	private OhHaiServerSocket serverSock;
	private OhHaiServerSocket.ServerSocketType sockType;
	
	public OhHaiService()  { this ("OhHaiService"); }
	
	public OhHaiService(String name)
	{
		super (name);
		sockType = OhHaiProgram.getSocketType();
		listenPort = OhHaiProgram.getListenPort();
		
        try {
        	serverSock = new OhHaiServerSocket(sockType, listenPort);
        } catch (Exception e) {}
	}
        	
	@Override
	protected void onHandleIntent(Intent intent) {
		notifyMessage (
			"OhHaiMessages SMS service",
			"SMS service started on " + OhHaiProgram.getLocalIpAddress() + ":" + listenPort
		);
		
		new Thread()  {
			@Override
			public void run()
			{
				try {
		        	while (true)
		        	{
			        	new OhHaiServer(serverSock.accept(), OhHaiService.this).start();
		        	}
		        } catch (Exception e)  {
		        	OhHaiProgram.addMessage("Error while sending the message: " + e.toString(), null, e);
		        }
			}
		}.start();
	}
	
	public void notifyMessage (String title, String msg)
	{
		Notification notify = new Notification (R.drawable.myicon, title, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, OhHaiService.class), 0);
		notify.setLatestEventInfo(getApplicationContext(), title, msg, contentIntent);
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, notify);
	}
}
