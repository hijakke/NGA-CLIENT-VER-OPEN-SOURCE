package gov.anzong.androidnga.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import gov.anzong.androidnga.R;
import sp.phone.bean.SignData;
import sp.phone.common.PhoneConfiguration;
import sp.phone.common.ThemeManager;
import sp.phone.fragment.MessageDetialListContainer;
import sp.phone.fragment.material.MessageDetailFragment;
import sp.phone.interfaces.OnChildFragmentRemovedListener;
import sp.phone.interfaces.OnSignPageLoadFinishedListener;
import sp.phone.interfaces.PagerOwner;
import sp.phone.interfaces.PullToRefreshAttacherOwner;
import sp.phone.utils.ActivityUtils;
import sp.phone.utils.NLog;
import sp.phone.utils.ReflectionUtil;
import sp.phone.utils.StringUtils;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;

public class MessageDetialActivity extends SwipeBackAppCompatActivity implements
        OnSignPageLoadFinishedListener, PagerOwner, OnItemClickListener,
        OnChildFragmentRemovedListener, PullToRefreshAttacherOwner {

    boolean dualScreen = true;
    int flags = ThemeManager.ACTION_BAR_FLAG;
    int mid;
    int nightmode;
    private String TAG = MessageDetialActivity.class.getSimpleName();
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private MessageDetailFragment mFragment;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setTitle("短消息正文");
        dualScreen = false;
        nightmode = ThemeManager.getInstance().getMode();
        String url = this.getIntent().getDataString();
        if (null != url) {
            mid = this.getUrlParameter(url, "mid");
        } else {
            mid = this.getIntent().getIntExtra("mid", 0);

        }
        if (PhoneConfiguration.getInstance().isMaterialMode()){
            mFragment = (MessageDetailFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
            if (mFragment == null) {
                mFragment = new MessageDetailFragment();
                Bundle args = new Bundle();// (getIntent().getExtras());
                if (null != getIntent().getExtras()) {
                    args.putAll(getIntent().getExtras());
                }
                args.putInt("mid", mid);
                mFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(android.R.id.content, mFragment).commit();
            }
            return;
        }
        this.setContentView(R.layout.messagedetaillist_activity);// OK
        PullToRefreshAttacher.Options options = new PullToRefreshAttacher.Options();
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this, options);
        FragmentManager fm = getSupportFragmentManager();
        Fragment f1 = fm.findFragmentById(R.id.item_list);// ok
        if (f1 == null) {
            f1 = new MessageDetialListContainer();
            Bundle args = new Bundle();// (getIntent().getExtras());
            if (null != getIntent().getExtras()) {
                args.putAll(getIntent().getExtras());
            }
            args.putInt("mid", mid);
            f1.setArguments(args);
            FragmentTransaction ft = fm.beginTransaction().add(R.id.item_list,
                    f1);
            // .add(R.id.item_detail_container, f);
            ft.commit();
        }// 生成左边

        f1.setHasOptionsMenu(true);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Fragment f1 = getSupportFragmentManager().findFragmentById(R.id.item_list);
        if (f1 != null){
            f1.onPrepareOptionsMenu(menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 123) {
            if (PhoneConfiguration.getInstance().isMaterialMode()){
                mFragment.onActivityResult(requestCode, resultCode, data);
            }  else {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.item_list);
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private int getUrlParameter(String url, String paraName) {
        if (StringUtils.isEmpty(url)) {
            return 0;
        }
        final String pattern = paraName + "=";
        int start = url.indexOf(pattern);
        if (start == -1)
            return 0;
        start += pattern.length();
        int end = url.indexOf("&", start);
        if (end == -1)
            end = url.length();
        String value = url.substring(start, end);
        int ret = 0;
        try {
            ret = Integer.parseInt(value);
        } catch (Exception e) {
            NLog.e(TAG, "invalid url:" + url);
        }
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (PhoneConfiguration.getInstance().isMaterialMode()){
            return super.onCreateOptionsMenu(menu);
        }

        ReflectionUtil.actionBar_setDisplayOption(this, flags);
        return false;// super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();// 关闭activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public PullToRefreshAttacher getAttacher() {
        return mPullToRefreshAttacher;
    }

    @Override
    protected void onResume() {
        if (nightmode != ThemeManager.getInstance().getMode()) {
            Intent intent = getIntent();
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
        } else {
            int orentation = ThemeManager.getInstance().screenOrentation;
            if (orentation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    || orentation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(orentation);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

            View view = findViewById(R.id.item_list);
            if (PhoneConfiguration.getInstance().fullscreen && view != null) {
                ActivityUtils.getInstance().setFullScreen(view);
            }
        }
        super.onResume();
    }

    @Override
    public void OnChildFragmentRemoved(int id) {
        finish();

    }// 竖屏变横屏就干这个

    @Override
    public void jsonfinishLoad(SignData result) {// 给左边SIGN信息用的
        Fragment SignContainer = getSupportFragmentManager().findFragmentById(
                R.id.item_list);

        OnSignPageLoadFinishedListener listener = null;
        try {
            listener = (OnSignPageLoadFinishedListener) SignContainer;
            if (listener != null)
                listener.jsonfinishLoad(result);
        } catch (ClassCastException e) {
            NLog.e(TAG, "topicContainer should implements "
                    + OnSignPageLoadFinishedListener.class.getCanonicalName());
        }
    }


    @Override
    public int getCurrentPage() {
        PagerOwner child = null;
        try {

            Fragment articleContainer = getSupportFragmentManager()
                    .findFragmentById(R.id.item_list);
            child = (PagerOwner) articleContainer;
            if (null == child)
                return 0;
            return child.getCurrentPage();
        } catch (ClassCastException e) {
            NLog.e(TAG, "fragment in R.id.item_detail_container does not implements interface " + PagerOwner.class.getName());
            return 0;
        }
    }

    @Override
    public void setCurrentItem(int index) {
        PagerOwner child = null;
        try {

            Fragment articleContainer = getSupportFragmentManager()
                    .findFragmentById(R.id.item_list);
            child = (PagerOwner) articleContainer;
            child.setCurrentItem(index);
        } catch (ClassCastException e) {
            NLog.e(TAG,
                    "fragment in R.id.item_detail_container does not implements interface "
                            + PagerOwner.class.getName());
            return;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    }
}