package com.example.uberv.coordinatordemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ScrollingActivity extends AppCompatActivity {

    @BindView(R.id.root)
    CoordinatorLayout mRootLayout;
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab_to_top)
    FloatingActionButton mToTopFab;
    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private TextAdapter mAdapter;
    private boolean mIsLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::onRefreshFabClicked);

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            data.add("List Item #" + i);
        }
        setupRecycler(data);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Timber.d("VerticalOffset "+verticalOffset);
                if(Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()){
                    Timber.d("is Collapsed");
                }
            }
        });
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                Timber.d("State changed to: " + state.name());
            }
        });
    }

    private void onRefreshFabClicked(View view) {
        Snackbar.make(view, "Refreshing", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        FloatingActionButton fab = (FloatingActionButton) view;
        mIsLoading = true;
        fab.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Timber.d("Animation started");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Timber.d("Animation ended");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                Timber.d("Animation repeated");
                if (!mIsLoading) {
                    animation.cancel();
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
        view.startAnimation(animation);
        new Handler().postDelayed(() -> {
//            view.getAnimation().cancel();
//            view.animate().rotation(0).start();
//            view.clearAnimation();
            mIsLoading = false;
            fab.setEnabled(true);
        }, 3000);
    }

    private void setupRecycler(List<String> data) {
        mAdapter = new TextAdapter(data);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    hideScrollFab();
                }
            }
        });
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
    }

    @OnClick(R.id.fab_to_top)
    public void onScrollToTop() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
        int firstItemPos = layoutManager.findFirstVisibleItemPosition();
        // do not use smooth scrolling if we scrolled many items already (too deep)
        if (firstItemPos > 40) {
            layoutManager.scrollToPositionWithOffset(0, 0);
        } else {
            mRecyclerView.smoothScrollToPosition(0);
        }
        mAppBarLayout.setExpanded(true, true);
        hideScrollFab();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideScrollFab() {
        mToTopFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                // For 25.1.0, CoordinatorLayout is skipping views set to GONE when looking for
                // behaviors to call in its onNestedScroll method
                mToTopFab.setVisibility(View.INVISIBLE);
            }
        });
    }
}
