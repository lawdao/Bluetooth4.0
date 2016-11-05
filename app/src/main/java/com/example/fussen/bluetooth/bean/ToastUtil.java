package com.example.fussen.bluetooth.bean;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	private static Toast toast;
	/**
	 * 可以连续弹吐司，不用等上个吐司消失
	 * @param text
	 */
	public static void showToast(String text, Context context){
		if(toast==null){
			toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		}
		toast.setText(text);
		toast.show();
	}
}
