package com.chrome.codereview.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import com.chrome.codereview.R;

/**
 * Created by sergeyv on 22/4/14.
 */
public class ViewUtils {

    private ViewUtils() {
    }

    public static void setText(View view, int id, CharSequence text) {
        TextView textView = (TextView) view.findViewById(id);
        textView.setText(text);
    }

    public static void setText(View view, int id, int textRes) {
        ViewUtils.setText(view, id, view.getContext().getString(textRes));
    }

    public static void expandView(View convertView, boolean isExpanded) {
        ImageView imageView = (ImageView) convertView.findViewById(R.id.expander);
        imageView.getDrawable().setState(isExpanded ? new int[]{android.R.attr.state_expanded} : new int[]{});
    }

    public static void onAnimationEnd(ViewPropertyAnimator viewPropertyAnimator, final Runnable onEnd) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            viewPropertyAnimator.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onEnd.run();
                }
            });
        } else {
            viewPropertyAnimator.withEndAction(onEnd);
        }
    }
}
