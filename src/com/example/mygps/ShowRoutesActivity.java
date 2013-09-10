package com.example.mygps;

import java.util.ArrayList;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class ShowRoutesActivity extends Activity implements OnClickListener {
	BMapManager mBMapMan = null;
	MapView mMapView = null;
	MKSearch mSearch = null; // ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��
	RouteOverlay yourRoute = null;
	GeoPoint[] routeData = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init("8430EEE8A9A8D7AEB66F7005BF020ABB995F5F89", null);
		// ע�⣺��������setContentViewǰ��ʼ��BMapManager���󣬷���ᱨ��
		setContentView(R.layout.routes_show);
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mMapView.getController().enableClick(true);
		mMapView.getController().setZoom(12);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setDoubleClickZooming(true);
		
		// �����Լ�����ʷ�켣
		Bundle bundle = getIntent().getExtras();
		ArrayList<Location> traceArray = bundle.getParcelableArrayList("trace");
		if(traceArray.isEmpty())
			return;
		int len = traceArray.size();
		routeData = new GeoPoint[len];
		for (int i = 0; i < len; i++) {
			routeData[i] = new GeoPoint(
					(int) (traceArray.get(i).getLatitude() * 1E6),
					(int) (traceArray.get(i).getLongitude() * 1E6));
		}
		// ��վ�����ݹ���һ��MKRoute
		MKRoute myRoute = new MKRoute();
		myRoute.customizeRoute(routeData[0], routeData[len - 1], routeData);
		// ������վ����Ϣ��MKRoute��ӵ�RouteOverlay��
	    yourRoute = new RouteOverlay(ShowRoutesActivity.this, mMapView);
		yourRoute.setData(myRoute);

		// ��ʼ������ģ�飬ע���¼�����
		mSearch = new MKSearch();
		mSearch.init(mBMapMan, new MKSearchListener() {

			@Override
			public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
				// TODO Auto-generated method stub
				// ����ſɲο�MKEvent�еĶ���
				if (error != 0 || res == null) {
					Toast.makeText(ShowRoutesActivity.this, "��Ǹ��δ�ҵ����",
							Toast.LENGTH_SHORT).show();
					return;
				}
				RouteOverlay standardRoute = new RouteOverlay(
						ShowRoutesActivity.this, mMapView);
				// �˴���չʾһ��������Ϊʾ��
				standardRoute.setData(res.getPlan(0).getRoute(0));
				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(standardRoute);
				mMapView.refresh();
				// ʹ��zoomToSpan()���ŵ�ͼ��ʹ·������ȫ��ʾ�ڵ�ͼ��
				mMapView.getController().zoomToSpan(
						standardRoute.getLatSpanE6(),
						standardRoute.getLonSpanE6());
				mMapView.getController().animateTo(standardRoute.getCenter());
			}

			@Override
			public void onGetPoiDetailSearchResult(int arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,
					int arg1) {
				// TODO Auto-generated method stub

			}
		});

		Button btnYourRoute = (Button) findViewById(R.id.yourRoute);
		Button btnStandardRoute = (Button) findViewById(R.id.standardRoute);
		btnYourRoute.setOnClickListener(this);
		btnStandardRoute.setOnClickListener(this);

	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		if (mBMapMan != null) {
			mBMapMan.start();
		}
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.yourRoute) {
			mMapView.getOverlays().clear();
			mMapView.getOverlays().add(yourRoute);
			mMapView.refresh();
			// ʹ��zoomToSpan()���ŵ�ͼ��ʹ·������ȫ��ʾ�ڵ�ͼ��
			mMapView.getController().zoomToSpan(yourRoute.getLatSpanE6(),
					yourRoute.getLonSpanE6());
			mMapView.getController().setCenter(yourRoute.getCenter());
		} else {
			MKPlanNode stNode = new MKPlanNode();
			stNode.pt = routeData[0];
//			Log.i("start",""+stNode.pt);
			MKPlanNode enNode = new MKPlanNode();
			enNode.pt = routeData[routeData.length-1];
//			Log.i("end",""+enNode.pt);
			mSearch.drivingSearch("����", stNode, "����", enNode);
		}
	}
}