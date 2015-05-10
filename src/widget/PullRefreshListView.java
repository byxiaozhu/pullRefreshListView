package widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.zhuyongit.pullrefreshandloadmore.R;

/**
 * 下拉刷新 ， 上拉加载
 * 
 * @author zhuyong < zhuyongit@163.com >
 * 
 */
public class PullRefreshListView extends ListView implements OnScrollListener {

	// 下拉刷新顶部View
	private View mHeaderView ;
	private View mFooterView ;
	// 布局文件填充器
	private LayoutInflater mInflater;
	// 顶部布局高度
	private int mHeaderHeight;
	// 当前第一个可见的ListView Item
	private int mFirstVisibleItem ;
	// 记录当前滚动状态
	private int mScrollState ;
	// 记录是否是在ListView最顶端按下的
	private boolean mTopPressMark ;
	// 记录按下时Y轴的值 ， 即距离顶部的高度
	private int mStartY ;
	// 记录ListView中的Item总数
	private int mTotalItemCount ;
	// 最后一个可见的Item
	private int mLastVisibleItem ;
	// 是否正在加载
	private boolean mIsLoading ;
	
	// 刷新数据接口
	private OnPullToRefreshListener mOnPullRefreshListener ;
	// 加载更多数据接口
	private OnLoadMoreListener mOnLoadMoreListener ;
	
	final int NONE = 0;// 正常状态；
	final int PULL = 1;// 提示下拉状态；
	final int RELESE = 2;// 提示释放状态；
	final int REFRESHING = 3;// 刷新状态；

	@SuppressLint("NewApi")
	public PullRefreshListView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView(context);
	}

	public PullRefreshListView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	public PullRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public PullRefreshListView(Context context) {
		super(context);
		initView(context);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
			this.mScrollState = scrollState ;
			
			if(mTotalItemCount == mLastVisibleItem && scrollState == SCROLL_STATE_IDLE)
			{
				if(!mIsLoading)
				{
					mIsLoading = true ;
					mFooterView.findViewById(R.id.load_layout).setVisibility(
							View.VISIBLE);
					mOnLoadMoreListener.onLoadMoreData() ;
				}
			}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
			this.mFirstVisibleItem = firstVisibleItem ;
			this.mLastVisibleItem = firstVisibleItem + visibleItemCount ;
			this.mTotalItemCount = totalItemCount ;
	}

	/**
	 * 初始化布局文件
	 */
	private void initView(Context context) {
		mInflater = LayoutInflater.from(context);
		// 获取到头部布局文件对象
		mHeaderView = mInflater.inflate(R.layout.header_layout, null);
		mFooterView = mInflater.inflate(R.layout.footer_layout, null) ;
		mFooterView.findViewById(R.id.load_layout).setVisibility(View.GONE) ;
		measuerView(mHeaderView);
		mHeaderHeight = mHeaderView.getMeasuredHeight();
		headerTopPadding(-mHeaderHeight) ;
		// 将顶部刷新添加到ListView中
		this.addHeaderView(mHeaderView) ;
		// 将底部加载布局添加到ListView中
		this.addFooterView(mFooterView) ;
		// 设置上下滑动监听
		this.setOnScrollListener(this) ;
	}

	/**
	 * 设置顶部刷新高度 
	 * @param topPadding
	 */
	private void headerTopPadding(int topPadding) {
		mHeaderView.setPadding(mHeaderView.getPaddingLeft(), topPadding,
				mHeaderView.getPaddingRight(), mHeaderView.getPaddingBottom());
		mHeaderView.invalidate() ;
	}

	/**
	 * 测量布局文件的高度和宽度
	 * 
	 * @param view
	 */
	private void measuerView(View view) {
		// 得到布局文件布局参数
		ViewGroup.LayoutParams p = view.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
		}

		int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int height;
		int tempHeight = p.height;
		if (tempHeight > 0) {
			height = MeasureSpec.makeMeasureSpec(tempHeight,
					MeasureSpec.EXACTLY);
		} else {
			height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}

		view.measure(width, height);
	}

	/**
	 * 屏幕触摸事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		
		switch (ev.getAction()) {
		// 按下
		case MotionEvent.ACTION_DOWN:
			if(mFirstVisibleItem == 0)
			{
				mTopPressMark = true ;
				mStartY = (int) ev.getY() ;
			}
			break;
		// 移动
		case MotionEvent.ACTION_MOVE:
			onMove(ev) ;
			break;
		// 松开
		case MotionEvent.ACTION_UP:
			if(mScrollState == RELESE)
			{
				mScrollState = REFRESHING ;
				reflashViewByState();
				mOnPullRefreshListener.pullToResfreshData() ;
			}else if (mScrollState == PULL) {
				mScrollState = NONE;
				mTopPressMark = false;
				reflashViewByState();
			}
			break;
		}
		
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 触摸移动的时的操作
	 * @param ev
	 */
	private void onMove(MotionEvent ev)
	{
		// 如果不是在顶部按下则不进行操作
		if(!mTopPressMark)
		{
			return ;
		}
		
		int tempY = (int) ev.getY() ;
		int space = tempY - mStartY ;
		int topPadding = space - mHeaderHeight ;
		
		switch (mScrollState) {
		case NONE:
			if (space > 0) {
				mScrollState = PULL;
				reflashViewByState();
			}
			break;

		case PULL:
			headerTopPadding(topPadding) ;
			if(space > mHeaderHeight + 20 && mScrollState == SCROLL_STATE_TOUCH_SCROLL )
			{
				mScrollState = RELESE;
				reflashViewByState();
			}
			break;
			
		case RELESE:
			headerTopPadding(topPadding) ;
			if (space < mHeaderHeight + 30) {
				mScrollState = PULL;
				reflashViewByState();
			} else if (space <= 0) {
				mScrollState = NONE;
			    mTopPressMark = false;
			    reflashViewByState();
			}
			break;
		}
	}
	
	/**
	 * 根据当前状态，改变界面显示；
	 */
	private void reflashViewByState() {
		TextView tip = (TextView) mHeaderView.findViewById(R.id.tip);
		ImageView arrow = (ImageView) mHeaderView.findViewById(R.id.arrow);
		ProgressBar progress = (ProgressBar) mHeaderView.findViewById(R.id.progress);
		RotateAnimation anim = new RotateAnimation(0, 180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setDuration(500);
		anim.setFillAfter(true);
		RotateAnimation anim1 = new RotateAnimation(180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim1.setDuration(500);
		anim1.setFillAfter(true);
		switch (mScrollState) {
		case NONE:
			arrow.clearAnimation();
			headerTopPadding(-mHeaderHeight);
			break;

		case PULL:
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("下拉可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim1);
			break;
		case RELESE:
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("松开可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim);
			break;
		case REFRESHING:
			headerTopPadding(50);
			arrow.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
			tip.setText("正在刷新...");
			arrow.clearAnimation();
			break;
		}
	}
	
	/**
	 * 获取完数据；
	 */
	public void reflashComplete() {
		mScrollState = NONE;
		mTopPressMark = false;
		reflashViewByState();
		TextView lastupdatetime = (TextView) mHeaderView
				.findViewById(R.id.lastupdate_time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String time = format.format(date);
		lastupdatetime.setText(time);
	}
	
	/**
	 * 加载完毕
	 */
	public void loadComplete(){
		mIsLoading = false;
		mFooterView.findViewById(R.id.load_layout).setVisibility(
				View.GONE);
	}
	
	/**
	 * 设置刷新事件接口
	 * @param onPullToRefreshListener
	 */
	public void setOnPullToResfeshListener(OnPullToRefreshListener onPullToRefreshListener)
	{
		this.mOnPullRefreshListener = onPullToRefreshListener ;
	}
	
	/**
	 * 设置加载更多数据接口
	 * @param onLoadMoreListener
	 */
	public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener)
	{
		this.mOnLoadMoreListener = onLoadMoreListener ;
	}
	
	/**
	 * 更新数据接口
	 * @author zhuyong < zhuyongit@163.com >
	 *
	 */
	public interface OnPullToRefreshListener
	{
		public void pullToResfreshData() ;
	}
	
	/**
	 * 向上拉加载数据接口
	 * @author zhuyong < zhuyongit@163.com >
	 *
	 */
	public interface OnLoadMoreListener
	{
		public void onLoadMoreData() ;
	}
}
