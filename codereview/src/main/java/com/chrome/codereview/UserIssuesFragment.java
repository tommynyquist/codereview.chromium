package com.chrome.codereview;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import com.chrome.codereview.model.Issue;
import com.chrome.codereview.model.UserIssues;
import com.chrome.codereview.requests.ServerCaller;
import com.chrome.codereview.utils.BaseListFragment;
import com.chrome.codereview.utils.CachedLoader;
import com.chrome.codereview.utils.ViewUtils;

import java.util.HashMap;

/**
 * Created by sergeyv on 13/4/14.
 */
public class UserIssuesFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<UserIssues> {

    public interface IssueSelectionListener {
        void onIssueSelected(Issue issue);
    }

    private static class IssuesLoader extends CachedLoader<UserIssues> {

        private final String userName;

        public IssuesLoader(Context context, String userName) {
            super(context);
            this.userName = userName;
        }

        @Override
        public UserIssues loadInBackground() {
            return ServerCaller.from(getContext()).loadIssuesForUser(userName);
        }
    }

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;

    private UserIssuesAdapter issuesAdapter;
    private IssueSelectionListener selectionListener;
    private boolean selectFirstIssue = false;
    private BackgroundContainer mBackgroundContainer;
    private ListView listView;

    private UserIssuesAdapter.onIssueClickListener issueClickListener = new UserIssuesAdapter.onIssueClickListener() {
        @Override
        public void onIssueClicked(Issue issue) {
            if (selectionListener != null) {
                selectionListener.onIssueSelected(issue);
            }
        }
    };

    @Override
    public Loader<UserIssues> onCreateLoader(int i, Bundle bundle) {
        startProgress();
        return new IssuesLoader(this.getActivity(), ServerCaller.from(getActivity()).getAccountName());
    }

    @Override
    public void onLoadFinished(Loader<UserIssues> listLoader, UserIssues issues) {
        issuesAdapter.setUserIssues(issues);
        stopProgress();
        setListAdapter(issuesAdapter);
        if (!selectFirstIssue) {
            return;
        }
        selectFirstIssue = false;
        for (int i = 0; i < issuesAdapter.getCount(); i++) {
            Issue issue = issuesAdapter.getItem(i);
            if (issue == null) {
                continue;
            }
            getListView().setSelection(i);
            if (selectionListener != null) {
                selectionListener.onIssueSelected(issue);
            }
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<UserIssues> listLoader) {
        issuesAdapter.setUserIssues(null);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_user_issues;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        issuesAdapter = new UserIssuesAdapter(getActivity(), touchListener, issueClickListener);
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        listView = (ListView) layout.findViewById(android.R.id.list);
        mBackgroundContainer = (BackgroundContainer) layout.findViewById(R.id.ptr_layout);
        return layout;
    }

    @Override
    protected void refresh() {
        getLoaderManager().restartLoader(0, new Bundle(), UserIssuesFragment.this);
    }

    public void selectFirstIssue() {
        this.selectFirstIssue = true;
    }

    public void setIssueSelectionListener(IssueSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    private HashMap<Long, Integer> itemIdTopMap = new HashMap<Long, Integer>();
    private boolean mSwiping = false;
    private View pressedView = null;

    /**
     * Handle touch events to fade/move dragged items as they are swiped out
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {

        float downX;
        private int swipeSlop = -1;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (swipeSlop < 0) {
                swipeSlop = ViewConfiguration.get(getActivity()).
                        getScaledTouchSlop();
            }
            if (pressedView != v && event.getAction() != MotionEvent.ACTION_DOWN) {
                return false;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (pressedView != null) {
                        // Multi-item swipes not handled
                        return false;
                    }
                    pressedView = v;
                    downX = event.getX();
                    return false;
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1);
                    v.setTranslationX(0);
                    pressedView = null;
                    return false;
                case MotionEvent.ACTION_MOVE: {
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - downX;
                    float deltaXAbs = Math.abs(deltaX);
                    if (!mSwiping) {
                        if (deltaXAbs > swipeSlop) {
                            mSwiping = true;
                            listView.requestDisallowInterceptTouchEvent(true);
                            mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                            return true;
                        }
                    }
                    if (mSwiping) {
                        v.setTranslationX((x - downX));
                        v.setAlpha(1 - deltaXAbs / v.getWidth());
                        return true;
                    }
                    return false;
                }
                case MotionEvent.ACTION_UP: {
                    // User let go - figure out whether to animate the view out, or back into place
                    pressedView = null;
                    if (mSwiping) {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - downX;
                        float deltaXAbs = Math.abs(deltaX);
                        float fractionCovered;
                        float endX;
                        float endAlpha;
                        final boolean remove;
                        if (deltaXAbs > v.getWidth() / 4) {
                            // Greater than a quarter of the width - animate it out
                            fractionCovered = deltaXAbs / v.getWidth();
                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                            endAlpha = 0;
                            remove = true;
                        } else {
                            // Not far enough - animate it back
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
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
                        listView.setEnabled(false);
                        ViewPropertyAnimator animator = v.animate().setDuration(duration).
                                alpha(endAlpha).translationX(endX);
                        ViewUtils.onAnimationEnd(animator, new Runnable() {
                            @Override
                            public void run() {
                                // Restore animated values
                                v.setAlpha(1);
                                v.setTranslationX(0);
                                if (remove) {
                                    animateRemoval(v);
                                } else {
                                    mBackgroundContainer.hideBackground();
                                    mSwiping = false;
                                    listView.setEnabled(true);
                                }
                            }
                        });

                        return true;
                    }
                    return false;
                }
            }
            return false;
        }
    };

    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    private void animateRemoval(View viewToRemove) {
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = issuesAdapter.getItemId(position);
                itemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        int position = listView.getPositionForView(viewToRemove);
        issuesAdapter.remove(issuesAdapter.getItem(position));

        final ViewTreeObserver observer = listView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listView.getFirstVisiblePosition();
                Runnable lastAction = new Runnable() {
                    public void run() {
                        mBackgroundContainer.hideBackground();
                        mSwiping = false;
                        listView.setEnabled(true);
                    }
                };
                for (int i = 0; i < listView.getChildCount(); ++i) {
                    final View child = listView.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = issuesAdapter.getItemId(position);
                    Integer startTop = itemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop == null) {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listView.getDividerHeight();
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

}
