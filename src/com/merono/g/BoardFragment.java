package com.merono.g;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;

public class BoardFragment extends ListFragment implements View.OnTouchListener {
    private ArrayList<String> threadLinks = new ArrayList<String>(15);
    private ArrayList<Post> posts = new ArrayList<Post>(15);
    private PostAdapter adapter;

    // variables for onTouch
    private static final int SWIPE_DURATION = 300;
    private boolean mSwiping = false;
    private boolean mIsImagePress;
    private FrameLayout imageFrameLayout;
    private float mDownX;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new PostAdapter(getActivity(), R.layout.post_item, posts, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(adapter);
    }

    public void setData(ArrayList<Post> data, ArrayList<String> links) {
        posts.clear();
        posts.addAll(data);

        threadLinks.clear();
        threadLinks.addAll(links);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private boolean isImagePress(View v, MotionEvent ev) {
        ImageView imageView = (ImageView) v.findViewById(R.id.post_img);
        int imgWidth = imageView.getWidth();
        return ev.getX() < imgWidth;
    }

    private void completeClick(View v, MotionEvent ev) {
        int position = getListView().getPositionForView(v);

        if (isImagePress(v, ev)) {
            Post selected = posts.get(position);
            if (selected.hasFullImgUrl()) {
                String imgUrl = selected.getFullImgUrl();
                ImageWebViewFragment.openImageWebView(getActivity(), imgUrl);
            }
        } else if (threadLinks.get(position) != null) {
            Intent i = new Intent(getActivity(), ThreadActivity.class);
            i.putExtra("URL", threadLinks.get(position));
            startActivity(i);
        }
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mSwiping) {
                    return false;
                }
                mDownX = event.getX();

                if (isImagePress(v, event)) {
                    mIsImagePress = true;
                    imageFrameLayout = (FrameLayout) v.findViewById(R.id.post_img_frame);
                    imageFrameLayout.setForeground(getResources().getDrawable(R.color.image_foreground));
                } else {
                    mIsImagePress = false;
                    v.setBackgroundResource(android.R.color.holo_blue_dark);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                v.setAlpha(1);
                v.setTranslationX(0);
                mSwiping = false;
                if (mIsImagePress) {
                    imageFrameLayout.setForeground(null);
                } else {
                    v.setBackgroundResource(0);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX() + v.getTranslationX();
                float deltaX = x - mDownX;
                float deltaXAbs = Math.abs(deltaX);
                if (!mSwiping) {
                    if (deltaXAbs > 40) {
                        mSwiping = true;
                        getListView().requestDisallowInterceptTouchEvent(true);
                    }
                } else {
                    v.setTranslationX(x - mDownX);
                    v.setAlpha(1 - deltaXAbs / v.getWidth());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mSwiping) {
                    x = event.getX() + v.getTranslationX();
                    deltaX = x - mDownX;
                    deltaXAbs = Math.abs(deltaX);
                    float fractionCovered;
                    float endX;
                    float endAlpha;
                    final boolean remove;
                    if (deltaXAbs > v.getWidth() / 4) {
                        fractionCovered = deltaXAbs / v.getWidth();
                        endX = (deltaX < 0) ? -v.getWidth() : v.getWidth();
                        endAlpha = 0;
                        remove = true;
                    } else {
                        fractionCovered = 1 - (deltaXAbs / v.getWidth());
                        endX = 0;
                        endAlpha = 1;
                        remove = false;
                    }

                    long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                    v.animate().setDuration(duration).alpha(endAlpha).
                            translationX(endX).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            v.setAlpha(1);
                            v.setTranslationX(0);
                            if (remove) {
                                int position = getListView().getPositionForView(v);
                                posts.remove(position);
                                threadLinks.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                            mSwiping = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {
                        }
                    });
                } else {
                    completeClick(v, event);
                }
                if (mIsImagePress) {
                    imageFrameLayout.setForeground(null);
                } else {
                    v.setBackgroundResource(0);
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
