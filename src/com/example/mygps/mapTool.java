package com.example.mygps;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.location.Location;
import android.util.Base64;
import android.util.Log;

public class mapTool {

	// 已知经纬度，计算两点间距离
	public static double EARTH_RADIUS = 6378.137;

	public static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	public static double GetDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s *= 1000;
		// s = Math.round(s * 10000) / 10000;
		return s;
	}

	// 获取google的标准路线距离
	public static String basicURL = "http://maps.googleapis.com/maps/api/directions/json?";

	public static int accessGoogleDirection(double lat1, double lng1,
			double lat2, double lng2) throws Exception {
		String path = basicURL + "origin=" + lat1 + "," + lng1
				+ "&destination=" + lat2 + "," + lng2 + "&sensor=false";
		Log.i("url", path);
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(10000);
		InputStream inStream = conn.getInputStream();
		byte[] data = StreamTool.ReadInputSream(inStream);
		String json = new String(data);
		JSONObject item = new JSONObject(json);
		JSONArray routes = item.getJSONArray("routes");
		int distance = 0;
		for (int i = 0; i < routes.length(); i++) {
			JSONArray legs = (routes.getJSONObject(i)).getJSONArray("legs");
			// 没有路标，因此leg只可能有一个
			distance = legs.getJSONObject(0).getJSONObject("distance")
					.getInt("value");
		}
		return distance;
	}

	public static String baiduURL = "http://api.map.baidu.com/telematics/v2/navigation?output=json&currentCity=131&ak=";
	public static String yourKey = "28460d87fbd9d9dcca550f1360d69850";

	public static int accessBaiduDirection(double lat1, double lng1,
			double lat2, double lng2) throws Exception {
		String path = baiduURL + yourKey + "&origin=" + lng1 + "," + lat1
				+ "&destination=" + lng2 + "," + lat2;
		Log.i("url", path);
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(10000);
		InputStream inStream = conn.getInputStream();
		byte[] data = StreamTool.ReadInputSream(inStream);
		String json = new String(data);
		JSONObject item = new JSONObject(json);
		JSONArray routes = item.getJSONArray("results");
		int distance = routes.getJSONObject(0).getInt("distance");
		return distance;
	}

	private static final int ONE_MINUTES = (int) (1000 * 60 * 0.5);

	// 判断是否加入队列
	public static boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			return true;
		}
		String provider = location.getProvider();
		String providerCurrent = currentBestLocation.getProvider();
		if (providerCurrent.equals("network")){
			return true;
		}
		if (provider.equals("gps")) {
			return true;
		}
		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > ONE_MINUTES;

		if (isSignificantlyNewer) {
			return true;
		}
		return false;
	}
	
	public static void fixPosition(Location location) throws Exception {
		String x = "" + location.getLongitude();
		String y = "" + location.getLatitude();
		URL url = new URL(
				"http://api.map.baidu.com/ag/coord/convert?from=0&to=4&x=" + x
						+ "&y=" + y);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(10000);
		InputStream inStream = conn.getInputStream();
		byte[] data = StreamTool.ReadInputSream(inStream);
		String json = new String(data);
		JSONObject item = new JSONObject(json);
		String mapX = item.getString("x");
		String mapY = item.getString("y");
		mapX = new String(Base64.decode(mapX, Base64.DEFAULT));
		mapY = new String(Base64.decode(mapY, Base64.DEFAULT));
		location.setLongitude(Double.parseDouble(mapX));
		location.setLatitude(Double.parseDouble(mapY));
	}
	public static ArrayList<Location> batchFixPosition(List<Location> raw) throws Exception{
		ArrayList<Location> result = new ArrayList<Location>();
		String x = "";
		String y = "";
		for( Location tmp:raw){
			x += ""+ tmp.getLongitude()+",";
			y += "" + tmp.getLatitude()+",";
		}
		URL url = new URL(
				"http://api.map.baidu.com/ag/coord/convert?from=0&to=4&x=" + x
						+ "&y=" + y+"&mode=1");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(10000);
		InputStream inStream = conn.getInputStream();
		byte[] data = StreamTool.ReadInputSream(inStream);
		String json = new String(data);
		JSONArray jArray = new JSONArray(json);
		for(int i = 0; i < jArray.length()-1; i++){
			JSONObject item = jArray.getJSONObject(i);
			String mapX = item.getString("x");
			String mapY = item.getString("y");
			mapX = new String(Base64.decode(mapX, Base64.DEFAULT));
			mapY = new String(Base64.decode(mapY, Base64.DEFAULT));
			Location location=new Location(raw.get(i));
			location.setLongitude(Double.parseDouble(mapX));
			location.setLatitude(Double.parseDouble(mapY));
			result.add(location);			
		}
		return result;
	}

}
