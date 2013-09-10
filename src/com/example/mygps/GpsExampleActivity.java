package com.example.mygps;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class GpsExampleActivity extends Activity implements OnClickListener {

	/** Called when the activity is first created. */
	private LocationManager locationManager = null;
	private static String gps = LocationManager.GPS_PROVIDER;
	private static String wifi = LocationManager.NETWORK_PROVIDER;

	private ArrayList<Location> hybridArray = null;
	private double resultDistance;

	private int resultGap;
	private int threshold = 2 / 2 * 1000;
	private boolean isML = false;
	private boolean isStart = false;
	private SharedPreferences sp = null;

	private Timer timer = null;
	private TimerTask timerTask = null;
	private static final int UPDATE_TEXTVIEW = 0;

	private long traffic = 0;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_TEXTVIEW:
				makeConclusion();
				break;
			default:
				break;
			}
		}
	};

	private Button.OnClickListener collectInfoListener = new Button.OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.btnNo) {
				Editor editor = sp.edit();
				editor.putInt("THRESHOLD", (threshold > resultGap) ? resultGap
						: (resultGap + 1));
				editor.commit();
				threshold = getThreshold();
			}
			findViewById(R.id.collectInfo).setVisibility(View.INVISIBLE);
		}
	};
	private RadioGroup.OnCheckedChangeListener radioGroupListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup arg0, int arg1) {
			// TODO Auto-generated method stub
			if (arg1 == R.id.radio4) {
				threshold = getThreshold();
				isML = true;
				return;
			}
			isML = false;
			// ��ȡ������ѡ�����ID
			int radioButtonId = arg0.getCheckedRadioButtonId();
			// ����ID��ȡRadioButton��ʵ��
			RadioButton rb = (RadioButton) findViewById(radioButtonId);
			// �����м۸� ÿ����2Ԫ
			threshold = Integer.parseInt((String) rb.getText()) / 2 * 1000;
		}
	};

	// GPS�ͻ�վ��϶�λ
	private boolean updateHybrid(Location location) {
		int len = hybridArray.size();
		if (len == 0
				|| mapTool.isBetterLocation(location, hybridArray.get(len - 1))) {
			GeoPoint gp = CoordinateConvert.fromWgs84ToBaidu(new GeoPoint(
					(int) (location.getLatitude() * 1E6), (int) (location
							.getLongitude() * 1E6)));
			location.setLatitude(((double)gp.getLatitudeE6())/1E6);
			location.setLongitude(((double)gp.getLongitudeE6())/1E6);
			hybridArray.add(location);
			return true;
		}
		return false;
	}

	// ���ر�׼distance
	private double display() {
		ArrayList<Location> array = hybridArray;
		int id = R.id.conclusionText;
		double standardDistance = 0;
		Location start;
		Location end;
		if (array.size() > 1) {
			start = array.get(0);
			end = array.get(array.size() - 1);
			try {
				standardDistance = mapTool.accessGoogleDirection(
						start.getLatitude(), start.getLongitude(),
						end.getLatitude(), end.getLongitude());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		TextView tv = (TextView) findViewById(id);
		tv.setText("������Ŀǰ��ʻ�ľ����ǣ�" + resultDistance + "m\n ��׼·�߾���ӦΪ��"
				+ standardDistance + "m\n������ĿΪ��" + array.size() + "\n");
		return standardDistance;
	}

	private void makeConclusion() {
		double standardDistance = display();
		TextView tv = (TextView) findViewById(R.id.conclusionText);
		String conclusion = "��ʻ������ף��һ·˳�磡\n";
		resultGap = (int) (resultDistance - standardDistance);
		if (resultGap > threshold) {
			conclusion = "��ʻ�쳣��������·��������ͼ�鿴�������\n";
		}
		tv.append(conclusion);
	}

	private void init() {
		hybridArray.clear();
		resultDistance = 0.0;
		traffic = TrafficStats.getTotalTxBytes();
		isStart = true;
		findViewById(R.id.collectInfo).setVisibility(View.INVISIBLE);
		((TextView) findViewById(R.id.conclusionText))
				.setText(R.string.hello_world);
		Location location_gps = locationManager.getLastKnownLocation(gps);
		Location location_wifi = locationManager.getLastKnownLocation(wifi);
		updateWithNewLocation(location_gps);
		updateWithNewLocation(location_wifi);
	}

	// �����˶�ʱ��鹦��
	private void startTimer() {
		if (timer == null) {
			timer = new Timer();
		}
		if (timerTask == null) {
			timerTask = new TimerTask() {
				public void run() {
					if (mHandler != null) {
						Message message = Message.obtain(mHandler,
								UPDATE_TEXTVIEW);
						mHandler.sendMessage(message);
					}
				}
			};
		}
		if (timer != null && timerTask != null) {
			timer.schedule(timerTask, 3 * 60 * 1000, 3 * 60 * 1000);
		}
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
	}

	// ��ȡ�ļ��еľ���ֵ��δ�����
	private int getThreshold() {
		return sp.getInt("THRESHOLD", 0);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btnStart) {
			if (isStart) {
				return;
			}
			init();
			startTimer();

		} else if (v.getId() == R.id.btnStop) {
			isStart = false;
			stopTimer();
			TextView tv = (TextView) this
					.findViewById(R.id.currentLocationText);
			tv.setText("��ǰ״̬��ֹͣ��λ\n");

			traffic = TrafficStats.getTotalTxBytes() - traffic;
			StreamTool.save(hybridArray, traffic);

			makeConclusion();
			if (isML) {
				findViewById(R.id.collectInfo).setVisibility(View.VISIBLE);
			}

		} else if (v.getId() == R.id.showRoutes) {

			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("trace", hybridArray);
			Intent intent = new Intent();
			intent.setClass(this, ShowRoutesActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		return;
	}

	// �ж��Ƿ��� GPS ����δ�������� GPS ���ý���
	private void openGPS() {
		if (locationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
				|| locationManager
						.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(this, " λ��Դ�����ã� ", Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(this, " λ��Դδ���ã� ", Toast.LENGTH_SHORT).show();
		// ת�� GPS ���ý���
		Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
		startActivityForResult(intent, 0);
	}

	// Gps ��Ϣ������
	private final LocationListener GpsLocationListener = new LocationListener() {
		// λ�÷����ı�����
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		// provider ���û��رպ����
		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);
		}

		// provider ���û����������
		public void onProviderEnabled(String provider) {
		}

		// provider ״̬�仯ʱ����
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private final LocationListener WifiLocationListener = new LocationListener() {
		// λ�÷����ı�����
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		// provider ���û��رպ����
		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);
		}

		// provider ���û����������
		public void onProviderEnabled(String provider) {
		}

		// provider ״̬�仯ʱ����
		public void onStatusChanged(String provider, int status, Bundle xtras) {
		}
	};

	private void updateWithNewLocation(Location location) {
		if(!isStart){
			return;
		}
		String latLongString;
		TextView myLocationText = (TextView) this
				.findViewById(R.id.currentLocationText);
		if (location != null) {
			if (!updateHybrid(location)) {
				return;
			}
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			ArrayList<Location> array = hybridArray;
			latLongString = " γ�� :" + lat + "\n ���� :" + lng;
			if (array.size() >= 2) {
				Location start = array.get(array.size() - 2);
				Location end = array.get(array.size() - 1);
				resultDistance += start.distanceTo(end);
			}
		} else {
			latLongString = " �޷���ȡ������Ϣ ";
		}
		myLocationText.setText("����ǰ��״̬��������ʻ��\n" + latLongString + "\n�Ѿ���ʻ����"
				+ resultDistance);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BMapManager mBMapMan = new BMapManager(getApplication());
		mBMapMan.init("8430EEE8A9A8D7AEB66F7005BF020ABB995F5F89", null);
		setContentView(R.layout.activity_gps_example);

		Button btn = (Button) findViewById(R.id.btnStart);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.btnStop);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.showRoutes);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.btnYes);
		btn.setOnClickListener(collectInfoListener);
		btn = (Button) findViewById(R.id.btnNo);
		btn.setOnClickListener(collectInfoListener);

		// ����ID�ҵ�RadioGroupʵ��
		RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
		// ��һ������������
		group.setOnCheckedChangeListener(radioGroupListener);

		hybridArray = new ArrayList<Location>();
		sp = this.getSharedPreferences("SP", MODE_PRIVATE);

		// ��ȡ LocationManager ����
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		// ���δ����λ��Դ���� GPS ���ý���
		openGPS();
		locationManager.requestLocationUpdates(gps, 10 * 1000, 30,
				GpsLocationListener);
		locationManager.requestLocationUpdates(wifi, 30 * 1000, 30,
				WifiLocationListener);

	}

	public void onDestroy(){
		super.onDestroy();
		locationManager.removeUpdates(GpsLocationListener);
		locationManager.removeUpdates(WifiLocationListener);
	}

}
