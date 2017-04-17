package com.gizwits.opensource.appkit.extra.ui.utils;

import java.text.SimpleDateFormat;

import android.content.Context;

public class Utils {
	/**
	 * 判断文件是否是图片
	 * @param fileName
	 * @return
	 */
	public static boolean isImage(String fileName){
		if (fileName.endsWith(".jpg")|| fileName.endsWith(".JPG")|| fileName.endsWith(".png")|| fileName.endsWith(".PNG")
				|| fileName.endsWith(".jpeg")|| fileName.endsWith(".JPEG")|| fileName.endsWith(".gif")|| fileName.endsWith(".GIF"))
			return true;
		return false;
	}
	
	/**
	 * 根据资源ID得到字符串
	 * @param context
	 * @param resId
	 * @return
	 */
	public static String getStringByResId(Context context,int resId){
		return context.getString(resId);
	}
	
	public static String getMusicDuration(int duration){
		SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
		return sdf.format(duration);
	}
}
