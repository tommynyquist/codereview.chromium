package com.chrome.codereview.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.chrome.codereview.utils.SwipeListAdapter;
import com.chrome.codereview.utils.ViewUtils;

import java.util.HashMap;

/**
 * Created by sergeyv on 9/8/14.
 */
public class SwipeListView extends ListView {

    public interface BackgroundToggle {

        void showBackground(int top, int bottom, int swipeDirection);

        void changeDirection(int swipeDirection);

        void hideBackground();
    }

    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;

    private static final int UNKNOWN = -1;
    private static final int SWIPE = -2;
    private static final int NOT_SWIPE = -3;

    private float downX;
    private int swipeSlop = -1;
    private int state = UNKNOWN;
    private int swipeDirection = 0;
    private BackgroundToggle backgroundToggle;
    public SwipeListView(Context context) {
        super(context);
        init();
    }

    public SwipeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private HashMap<Long, Integer> itemIdTopMap = new HashMap<Long, Integer>();
    private View swipedView = null;
    private float downY;


    public void setBackgroundToggle(BackgroundToggle backgroundToggle) {
        this.backgroundToggle = backgroundToggle;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof SwipeListAdapter) {
            super.setAdapter(adapter);
            return;
        }
        throw new IllegalArgumentException("Adapter must be implementation of SwipeListAdapter");
    }

    @Override
    public SwipeListAdapter getAdapter() {
        return (SwipeListAdapter) super.getAdapter();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            int position = pointToPosition((int) event.getX(), (int) event.getY());
            swipedView = getChildAt(position - getFirstVisiblePosition());
            state = position == INVALID_POSITION || !getAdapter().isItemSwipable(position)? NOT_SWIPE : UNKNOWN;
            downX = event.getX();
            downY = event.getY();
            return super.onTouchEvent(event);
        }

        if (state == NOT_SWIPE) {
            return super.onTouchEvent(event);
        }

        if (state == UNKNOWN && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)) {
            swipedView = null;
            return super.onTouchEvent(event);
        }

        float deltaX = event.getX() - downX;
        int direction = deltaX >= 0 ? DIRECTION_RIGHT : DIRECTION_LEFT;
        float deltaXAbs = Math.abs(deltaX);
        float deltaYAbs = Math.abs(event.getY() - downY);
        if (state == UNKNOWN && action == MotionEvent.ACTION_MOVE) {
            if (deltaYAbs > swipeSlop) {
                state = NOT_SWIPE;
                swipedView = null;
                return super.onTouchEvent(event);
            }
            if (deltaXAbs > 2 * swipeSlop) {
                state = SWIPE;
                requestDisallowInterceptTouchEvent(true);
                swipeDirection = direction;
                backgroundToggle.showBackground(swipedView.getTop(), swipedView.getHeight(), direction);
                return true;
            }
        }

        if (state == UNKNOWN) {
            return super.onTouchEvent(event);
        }

        if (action == MotionEvent.ACTION_CANCEL) {
            setPressedViewState(0, 1);
            swipedView = null;
            return super.onTouchEvent(event);
        }

        int width = swipedView.getWidth();
        if (action == MotionEvent.ACTION_MOVE) {
            setPressedViewState(deltaX, 1 - deltaXAbs / width);
            if (direction != swipeDirection) {
                swipeDirection = direction;
                backgroundToggle.changeDirection(direction);
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP) {
            float fractionCovered;
            float endX;
            float endAlpha;
            final boolean remove;
            if (deltaXAbs > width / 4) {
                // Greater than a quarter of the width - animate it out
                fractionCovered = deltaXAbs / width;
                endX = width * Math.signum(deltaX);
                endAlpha = 0;
                remove = true;
            } else {
                // Not far enough - animate it back
                fractionCovered = 1 - (deltaXAbs / width);
                endX = 0;
                endAlpha = 1;
                remove = false;
            }
            // Animate position and alpha of swiped item
            // NOTE: This is a simplified version of swipe behavior, for the
            // purposes of this demo about animation. A real version should use
            // velocity (via the VelocityTracker class) to send the item off or
            // back at an appropriate speed.
            long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
            setEnabled(false);
            ViewPropertyAnimator animator = swipedView.animate().setDuration(duration).
                    alpha(endAlpha).translationX(endX);
            ViewUtils.onAnimationEnd(animator, new Runnable() {
                @Override
                public void run() {
                    // Restore animated values
                    setPressedViewState(0, 1);
                    if (remove) {
                        animateRemoval(swipedView);
                    } else {
                        backgroundToggle.hideBackground();
                        setEnabled(true);
                    }
                    swipedView = null;
                }
            });

            return true;
        }
        return super.onTouchEvent(event);
    }


    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    private void animateRemoval(View viewToRemove) {
        final SwipeListAdapter adapter = getAdapter();
        int firstVisiblePosition = getFirstVisiblePosition();
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = adapter.getItemId(position);
                itemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        int position = getPositionForView(viewToRemove);
        adapter.remove(position);

        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = getFirstVisiblePosition();
                Runnable lastAction = new Runnable() {
                    public void run() {
                        backgroundToggle.hideBackground();
                        setEnabled(true);
                    }
                };
                for (int i = 0; i < getChildCount(); ++i) {
                    final View child = getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = adapter.getItemId(position);
                    Integer startTop = itemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop == null) {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                    }
                    if (startTop == top) {
                        continue;
                    }
                    int delta = startTop - top;
                    child.setTranslationY(delta);
                    child.animate().setDuration(MOVE_DURATION).translationY(0);
                    if (firstAnimation) {
                        ViewUtils.onAnimationEnd(child.animate(), lastAction);
                        firstAnimation = false;
                    }

                }
                if (firstAnimation) {
                    lastAction.run();
                }
                itemIdTopMap.clear();
                return true;
            }
        });
    }

    private void setPressedViewState(float x, float alpha) {
        swipedView.setAlpha(alpha);
        swipedView.setTranslationX(x);
    }

    private void init() {
        swipeSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }
}
