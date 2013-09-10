package com.example.mygps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.location.Location;
import android.os.Environment;

public class StreamTool {
	/**
	 * ���������л�ȡ����
	 * 
	 * @param inStream
	 *            ������
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] ReadInputSream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}

	public static byte[] getImage(String path) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5 * 1000);
		InputStream inStream = conn.getInputStream();// ͨ����������ȡͼƬ����
		return ReadInputSream(inStream);// �õ�ͼƬ�Ķ���������
	}

	public static boolean save(ArrayList<Location> trace, long traffic) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File sdCardDir = Environment.getExternalStorageDirectory();// ��ȡSDCardĿ¼
			File saveFile = new File(sdCardDir, "route"
					+ System.currentTimeMillis() + ".txt");
			FileOutputStream outStream;
			try {
				outStream = new FileOutputStream(saveFile);
				for (Location tmp : trace) {
					outStream.write((tmp.getLatitude() + " "
							+ tmp.getLongitude() + "\n").getBytes());
				}
				outStream.write((""+traffic).getBytes());
				outStream.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
}
