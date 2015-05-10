package com.ogaclejapan.qiitanium.presentation.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.norbsoft.typefacehelper.TypefaceHelper;
import com.ogaclejapan.qiitanium.R;
import com.ogaclejapan.qiitanium.presentation.fragment.ArticleListFragment;
import com.ogaclejapan.qiitanium.presentation.fragment.ComingSoonFragment;
import com.ogaclejapan.qiitanium.presentation.fragment.ComingSoonScrollableFragment;
import com.ogaclejapan.qiitanium.presentation.fragment.TagListFragment;
import com.ogaclejapan.qiitanium.presentation.helper.ParallaxHeaderHelper;
import com.ogaclejapan.qiitanium.presentation.helper.SlidingUpPanelHelper;
import com.ogaclejapan.qiitanium.presentation.listener.Refreshable;
import com.ogaclejapan.qiitanium.presentation.listener.ScrollableTab;
import com.ogaclejapan.qiitanium.presentation.listener.ScrollableTabListener;
import com.ogaclejapan.qiitanium.presentation.view.OverlayTopMenuView;
import com.ogaclejapan.qiitanium.util.ViewUtils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v13.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v13.FragmentPagerItems;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class TopActivity extends AppActivity
    implements ScrollableTabListener, Refreshable {

  private final ViewPager.SimpleOnPageChangeListener onPageChangeListener =
      new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
          Fragment f = viewPagerAdapter.getPage(position);
          if (f instanceof ScrollableTab) {
            ((ScrollableTab) f).adjustScroll(headerHelper.getTranslationY());
          }
        }
      };

  private OverlayTopMenuView topMenu;
  private ParallaxHeaderHelper headerHelper;
  private SlidingUpPanelHelper panelHelper;
  private ViewPager viewPager;
  private FragmentPagerItemAdapter viewPagerAdapter;

  public static void startActivity(Context context) {
    context.startActivity(intentOf(context));
  }

  public static Intent intentOf(Context context) {
    return new Intent(context, TopActivity.class);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_top);

    FragmentManager fm = getFragmentManager();

    View header = findViewById(R.id.header);
    View headerImage = findViewById(R.id.header_image);

    headerHelper = new ParallaxHeaderHelper.Builder(header, headerImage)
        .setMinHeight(getDimensionPixelSize(R.dimen.header_min_height))
        .setElevation(getDimensionPixelSize(R.dimen.header_elevation))
        .create();

    SlidingUpPanelLayout slidingUpPanel = ViewUtils.findById(this, R.id.panel);
    panelHelper = new SlidingUpPanelHelper(slidingUpPanel);

    TextView title = ViewUtils.findById(this, R.id.title);
    title.setText(R.string.label_articles);

    viewPagerAdapter = new FragmentPagerItemAdapter(fm, FragmentPagerItems.with(this)
        .add(R.string.tab_new, ArticleListFragment.class)
        .add(R.string.tab_following, ComingSoonScrollableFragment.class)
        .create());

    viewPager = ViewUtils.findById(this, R.id.article_top_viewpager);
    viewPager.setAdapter(viewPagerAdapter);
    SmartTabLayout viewPagerTab = ViewUtils.findById(this, R.id.viewpagertab);
    viewPagerTab.setCustomTabView(R.layout.view_tab, R.id.view_tab_text);
    viewPagerTab.setViewPager(viewPager);
    viewPagerTab.setOnPageChangeListener(onPageChangeListener);

    TextView tagTitle = ViewUtils.findById(this, R.id.tag_top_title);
    ViewPager tagViewPager = ViewUtils.findById(this, R.id.tag_top_viewpager);
    tagViewPager.setAdapter(new FragmentPagerItemAdapter(fm, FragmentPagerItems.with(this)
        .add(R.string.tab_popular, TagListFragment.class)
        .add(R.string.tab_following, ComingSoonFragment.class)
        .create()));

    SmartTabLayout tagViewPagerTab = ViewUtils.findById(this, R.id.tag_top_viewpagertab);
    tagViewPagerTab.setViewPager(tagViewPager);

    topMenu = ViewUtils.findById(this, R.id.overlay_top);
    topMenu.bindTo(this);

    TypefaceHelper.typeface(title);
    TypefaceHelper.typeface(tagTitle);
    TypefaceHelper.typeface(viewPagerTab);
    TypefaceHelper.typeface(tagViewPagerTab);

  }

  @Override
  public void onBackPressed() {
    if (topMenu.canTurnOff()) {
      topMenu.turnOff();
      return;
    }

    if (!panelHelper.isCollapsed()) {
      panelHelper.collapse();
      return;
    }

    super.onBackPressed();
  }

  @Override
  public void onScroll(int scrollY, int position) {
    if (viewPager.getCurrentItem() != position) {
      return;
    }

    headerHelper.scroll(scrollY);
  }

  @Override
  public void refresh() {
    final Fragment f = viewPagerAdapter.getPage(viewPager.getCurrentItem());
    if (f instanceof Refreshable) {
      ((Refreshable) f).refresh();
    }
  }

}
