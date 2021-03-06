/**
 * Copyright (c) 2015
 *
 * Licensed under the UCG License, Version 1.0 (the "License");
 */
package com.app.jdy.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.app.jdy.R;
import com.app.jdy.adapter.BankAdapter;
import com.app.jdy.adapter.FaceValueAdapter;
import com.app.jdy.adapter.MyOrderAdapter;
import com.app.jdy.adapter.MyRecordAdapter;
import com.app.jdy.entity.BankCard;
import com.app.jdy.entity.FaceValue;
import com.app.jdy.entity.MyOrder;
import com.app.jdy.entity.MyRecord;
import com.app.jdy.utils.ChineseMoneyUtils;
import com.app.jdy.utils.Constants;
import com.app.jdy.utils.HttpUtils;
import com.app.jdy.utils.URLs;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * description :
 *
 * @version 1.0
 * @author zhoufeng
 * @createtime : 2015-1-31 下午9:53:49
 * 
 * 修改历史:
 * 修改人                                          修改时间                                                  修改内容
 * --------------- ------------------- -----------------------------------
 * zhoufeng        2015-1-31 下午9:53:49
 *
 */
public class MyRecordActivity extends Activity implements OnClickListener {
	/**
	 * 返回按钮
	 */
	private ImageView mBackImg;
	/**
	 * 标题
	 */
	private TextView title;
	/**
	 * list组件
	 */
	private ListView my_record_list;
	/**
	 * handler
	 */
	private Handler handler;
	/**
	 * 用户ID号
	 */
	private String ID;
	/**
	 * 提交数据
	 */
	private ArrayList<NameValuePair> params;
	/**
	 * 我的提现记录的实体类
	 */
	private List<MyRecord> list;
	/**
	 * 服务的json数据
	 */
	private String dataJson;
	/**
	 * 我的提现记录适配器
	 */
	private MyRecordAdapter myRecordAdapter;
	/**
	 * 我的提现金额的总数
	 */
	private TextView my_record_sum;
	/**
	 * 元的组件
	 */
	private TextView my_record_sum_yuan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_record);

		initView();
		
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					Toast.makeText(MyRecordActivity.this, Constants.NO_INTENT_TIPS,
							Toast.LENGTH_SHORT).show();
					break;
				case 1:
					list = new ArrayList<MyRecord>();
					try {
						JSONObject jsonObject = new JSONObject(dataJson);
						if (jsonObject.getString("moneySum").equals("null")) {
							my_record_sum.setText("0");
							my_record_sum_yuan.setText("元");
						}else{
							my_record_sum.setText(ChineseMoneyUtils
									.numWithDigitArray(jsonObject.getDouble("moneySum"))[0]);
							my_record_sum_yuan.setText(ChineseMoneyUtils
									.numWithDigitArray(jsonObject.getDouble("moneySum"))[1]);
						}
						JSONArray jsonArray = jsonObject.getJSONArray("list");
						
						for (int i = 0; i < jsonArray.length(); i++) {
							
							MyRecord myRecord = new MyRecord();
							myRecord.setMoney(jsonArray.getJSONObject(i)
									.getDouble("withdCash"));
							myRecord.setDopositeTime(jsonArray.getJSONObject(i)
									.getString("applyTime"));
							myRecord.setMessage(jsonArray.getJSONObject(i)
									.getString("statuts"));
							list.add(myRecord);
						}
						myRecordAdapter = new MyRecordAdapter(
								MyRecordActivity.this,
								R.layout.my_order_item, list);
						my_record_list.setAdapter(myRecordAdapter);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
			}
		};
		
		getData();
		mBackImg.setOnClickListener(this);
	}

	public void initView() {
		mBackImg = (ImageView) findViewById(R.id.back_img);
		mBackImg.setVisibility(View.VISIBLE);
		title = (TextView) findViewById(R.id.title_tv);
		title.setText("提现记录");

		SharedPreferences userPreferences = getSharedPreferences(
				"umeng_general_config", Context.MODE_PRIVATE);
		ID = userPreferences.getString("ID", "").trim();

		my_record_list = (ListView) findViewById(R.id.my_record_list);
		my_record_sum = (TextView) findViewById(R.id.my_record_sum);
		my_record_sum_yuan = (TextView) findViewById(R.id.my_record_sum_yuan);
		
	}
	
	public void getData() {
		params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("memberId", ID));
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = new Message();
				dataJson = HttpUtils.request(params, URLs.MY_RECORD);
				if (dataJson.length() != 0 && !dataJson.equals("0x110")) {
					msg.what = 1;
				} else {
					msg.what = 0;
				}
				handler.sendMessage(msg);
			}
		});
		thread.start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_img:
			finish();
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("SplashScreen"); 
		MobclickAgent.onResume(this); 
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("SplashScreen"); 
		MobclickAgent.onPause(this);
	}
}
