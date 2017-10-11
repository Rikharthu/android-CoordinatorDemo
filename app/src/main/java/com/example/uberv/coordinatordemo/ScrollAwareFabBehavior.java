package com.example.uberv.coordinatordemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class ScrollAwareFabBehavior extends FloatingActionButton.Behavior {

    private boolean mIsFabShown = true;

    public ScrollAwareFabBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        // Return true to accept this scroll event
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL // ensure we react to vertical scrolling
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes); // any other fab behavior
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull final FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
            // Scrolling down and Fab is still visible
            hideFab(child);
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            // Scrolling up
            child.show();
            mIsFabShown = true;
        }
    }

    private void hideFab(@NonNull final FloatingActionButton child) {
        child.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                // For 25.1.0, CoordinatorLayout is skipping views set to GONE when looking for
                // behaviors to call in its onNestedScroll method
                child.setVisibility(View.INVISIBLE);
            }
        });
    }
}
