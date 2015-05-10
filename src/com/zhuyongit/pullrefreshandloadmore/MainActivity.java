package com.zhuyongit.pullrefreshandloadmore;

import java.util.ArrayList;

import widget.PullRefreshListView;
import widget.PullRefreshListView.OnLoadMoreListener;
import widget.PullRefreshListView.OnPullToRefreshListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;


public class MainActivity extends FragmentActivity implements OnPullToRefreshListener,OnLoadMoreListener{

	ArrayList<ApkEntity> apk_list = new ArrayList<ApkEntity>();
	MyAdapter adapter;
	PullRefreshListView listview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getData();
		showListView(apk_list);
		
/*		listview.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				//获取最新数据
				setReflashData();
				//通知界面显示
				showListView(apk_list) ;
				//通知listview 刷新数据完毕；
				listview.onRefreshComplete();
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				
			}
			
		}) ;*/
	}

	
	private void showListView(ArrayList<ApkEntity> apk_list) {
		if (adapter == null) {
			listview = (PullRefreshListView) findViewById(R.id.lv01);
			listview.setOnPullToResfeshListener(this) ;
			listview.setOnLoadMoreListener(this) ;
			adapter = new MyAdapter(this, apk_list);
			listview.setAdapter(adapter);
		} else {
			adapter.onDateChange(apk_list);
		}
	}

	private void getData() {
		for (int i = 0; i < 10; i++) {
			ApkEntity entity = new ApkEntity();
			entity.setName("测试程序");
			entity.setInfo("50w用户");
			entity.setDes("这是一个神奇的应用！");
			apk_list.add(entity);
		}
	}
	private void getLoadData() {
		for (int i = 0; i < 2; i++) {
			ApkEntity entity = new ApkEntity();
			entity.setName("更多程序");
			entity.setInfo("50w用户");
			entity.setDes("这是一个神奇的应用！");
			apk_list.add(entity);
		}
	}

	@Override
	public void onLoadMoreData() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//获取更多数据
				getLoadData();
				//更新listview显示；
				showListView(apk_list);
				//通知listview加载完毕
				listview.loadComplete();
			}
		}, 2000);
	}

	@Override
	public void pullToResfreshData() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//获取最新数据
				setReflashData();
				//通知界面显示
				showListView(apk_list) ;
				//通知listview 刷新数据完毕；
				listview.reflashComplete();
			}
		}, 2000);
	}

	private void setReflashData() {
		for (int i = 0; i < 2; i++) {
			ApkEntity entity = new ApkEntity();
			entity.setName("刷新数据");
			entity.setDes("这是一个神奇的应用");
			entity.setInfo("50w用户");
			apk_list.add(0,entity);
		}
	
	}

}
