package com.ogaclejapan.qiitanium.presentation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.ogaclejapan.qiitanium.R;
import com.ogaclejapan.qiitanium.presentation.adapter.ArticleListAdapter;
import com.ogaclejapan.qiitanium.presentation.helper.LoadMoreHelper;
import com.ogaclejapan.qiitanium.presentation.helper.ScrollableHelper;
import com.ogaclejapan.qiitanium.presentation.listener.Refreshable;
import com.ogaclejapan.qiitanium.presentation.listener.ScrollableTab;
import com.ogaclejapan.qiitanium.presentation.viewmodel.ArticleListViewModel;
import com.ogaclejapan.qiitanium.presentation.widget.TextProgressBar;
import com.ogaclejapan.qiitanium.util.ViewUtils;
import com.ogaclejapan.rx.binding.Rx;
import com.ogaclejapan.rx.binding.RxActions;
import com.ogaclejapan.rx.binding.RxView;
import com.ogaclejapan.smarttablayout.utils.v13.Bundler;
import com.ogaclejapan.smarttablayout.utils.v13.FragmentPagerItem;
import com.twotoasters.jazzylistview.JazzyListView;

import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class ArticleListFragment extends AppFragment
    implements AbsListView.OnScrollListener, ScrollableTab, Refreshable {

  public static final String TAG = ArticleListFragment.class.getSimpleName();
  private static final String KEY_TAG_ID = TAG + ":tag_id";

  private Rx<TextProgressBar> progressBar;
  private ArticleListAdapter listAdapter;
  private ArticleListViewModel viewModel;
  private LoadMoreHelper loadMoreHelper;
  private ScrollableHelper scrollableHelper;

  public static ArticleListFragment newInstance() {
    return newInstance(null);
  }

  public static ArticleListFragment newInstance(String tagId) {
    return new Bundler()
        .putString(KEY_TAG_ID, tagId)
        .into(new ArticleListFragment());
  }

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
      final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_article_list, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    listAdapter = ArticleListAdapter.create(getContext());
    viewModel = ArticleListViewModel.create(getContext());
    viewModel.setTagId(getTagId());

    progressBar = RxView.findById(view, R.id.progress);

    JazzyListView listView = ViewUtils.findById(view, R.id.list);

    loadMoreHelper = LoadMoreHelper.with(listView);
    scrollableHelper = ScrollableHelper.with(getActivity(), listView);

    listView.setAdapter(listAdapter);
    listView.setOnScrollListener(this);

  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    refresh();
  }

  @Override
  protected Subscription onBind() {
    return Subscriptions.from(
        viewModel,
        listAdapter.bind(viewModel.items()),
        progressBar.bind(viewModel.isLoading(), RxActions.setVisibility())
    );
  }

  @Override
  public void refresh() {
    viewModel.refresh();
  }

  @Override
  public void onScrollStateChanged(final AbsListView view, final int scrollState) {
    //Do nothing.
  }

  @Override
  public void onScroll(final AbsListView view, final int firstVisibleItem,
      final int visibleItemCount,
      final int totalItemCount) {

    scrollableHelper.onScroll(getPosition());

    if (ViewUtils.isVisible(progressBar.get())) {
      return;
    }

    if (loadMoreHelper.onNext(firstVisibleItem + visibleItemCount, totalItemCount)) {
      viewModel.loadMore();
    }

  }

  @Override
  public void adjustScroll(int scrollY) {
    scrollableHelper.adjustScroll(scrollY);
  }

  private int getPosition() {
    return FragmentPagerItem.getPosition(getArguments());
  }

  private String getTagId() {
    return getArguments().getString(KEY_TAG_ID);
  }

}
