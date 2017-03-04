package com.pandaq.pandaeye.ui.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pandaq.pandaeye.R;
import com.pandaq.pandaeye.adapters.TopNewsListAdapter;
import com.pandaq.pandaeye.config.Constants;
import com.pandaq.pandaeye.entity.neteasynews.TopNews;
import com.pandaq.pandaeye.presenter.news.NewsPresenter;
import com.pandaq.pandaeye.ui.ImplView.INewsListFrag;
import com.pandaq.pandaeye.ui.base.BaseFragment;
import com.pandaq.pandaqlib.magicrecyclerView.BaseItem;
import com.pandaq.pandaqlib.magicrecyclerView.BaseRecyclerAdapter;
import com.pandaq.pandaqlib.magicrecyclerView.MagicRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PandaQ on 2016/9/9.
 * email : 767807368@qq.com
 * 新闻列表界面
 */
public class NewsListFragment extends BaseFragment implements INewsListFrag, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.testRecycler)
    MagicRecyclerView mNewsRecycler;
    @BindView(R.id.refresh)
    SwipeRefreshLayout mRefresh;
    private NewsPresenter mPresenter = new NewsPresenter(this);
    private TopNewsListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.newslist_fragment, container, false);
        ButterKnife.bind(this, view);
        mNewsRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //屏蔽掉默认的动画，房子刷新时图片闪烁
        mNewsRecycler.getItemAnimator().setChangeDuration(0);
        initView();
        return view;
    }

    private void initView() {
        mNewsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mNewsRecycler.refreshAble()) {
                    mRefresh.setEnabled(true);
                }
                if (mNewsRecycler.loadAble()) {
                    loadMoreNews();
                }
            }
        });
        mRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.white_FFFFFF));
        mRefresh.setOnRefreshListener(this);
        mRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mRefresh.setRefreshing(true);
        refreshNews();
        mPresenter.loadCache();
        mNewsRecycler.addOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, BaseItem baseItem, View view) {
                //跳转到其他界面
                TopNews topNews = (TopNews) baseItem.getData();
                Bundle bundle = new Bundle();
                Intent intent = new Intent(NewsListFragment.this.getActivity(), TopNewsInfoActivity.class);
                bundle.putString(Constants.BUNDLE_KEY_TITLE, topNews.getTitle());
                bundle.putString(Constants.BUNDLE_KEY_ID, topNews.getDocid());
                bundle.putString(Constants.BUNDLE_KEY_IMG_URL, topNews.getImgsrc());
                intent.putExtras(bundle);
                String transitionName = getString(R.string.top_news_img);
                Pair pairImg = new Pair<>(view.findViewById(R.id.news_image), transitionName);
                ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pairImg);
                startActivity(intent, transitionActivityOptions.toBundle());
            }
        });
        View footer = mNewsRecycler.getFooterView();
        if (footer != null) {
            footer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadMoreNews();
                }
            });
        }
    }

    @Override
    public void showRefreshBar() {
        mRefresh.setRefreshing(true);
    }

    @Override
    public void hideRefreshBar() {
        mRefresh.setRefreshing(false);
    }

    @Override
    public void refreshNews() {
        mPresenter.refreshNews();
    }

    @Override
    public void refreshNewsFail(String errorMsg) {

    }

    @Override
    public void refreshNewsSuccessed(ArrayList<BaseItem> topNews) {
        if (mAdapter == null) {
            mAdapter = new TopNewsListAdapter(this);
            mAdapter.setBaseDatas(topNews);
            mNewsRecycler.setAdapter(mAdapter);
        } else {
            mAdapter.setBaseDatas(topNews);
        }
    }

    @Override
    public void loadMoreNews() {
        mPresenter.loadMore();
    }

    @Override
    public void loadMoreFail(String errorMsg) {

    }

    @Override
    public void loadMoreSuccessed(ArrayList<BaseItem> topNewses) {
        mAdapter.addBaseDatas(topNewses);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRefresh.setRefreshing(false);
        mPresenter.unSubscribe();
    }

    @Override
    public void onRefresh() {
        refreshNews();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden && mRefresh.isRefreshing()) { // 隐藏的时候停止 SwipeRefreshLayout 转动
            mRefresh.setRefreshing(false);
        }
    }
}