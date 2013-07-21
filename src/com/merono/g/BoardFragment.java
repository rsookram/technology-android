package com.merono.g;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class BoardFragment extends ListFragment {
    private ArrayList<String> threadLinks = new ArrayList<String>(15);
    private ArrayList<Post> posts = new ArrayList<Post>(15);
    private PostAdapter adapter;

    private boolean mSwiping = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new PostAdapter(getActivity(), R.layout.post_item, posts, mTouchListener);
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (threadLinks.get(position) != null) {
            Intent i = new Intent(getActivity(), ThreadActivity.class);
            i.putExtra("URL", threadLinks.get(position));
            startActivity(i);
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        private static final int SWIPE_DURATION = 300;
        private float mDownX;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            mGestureDetector.onTouchEvent(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mSwiping) {
                        return false;
                    }
                    mDownX = event.getX();
                    v.setBackgroundResource(android.R.color.holo_blue_dark);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1);
                    v.setTranslationX(0);
                    mSwiping = false;
                    v.setBackgroundResource(0);
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
                        int position = getListView().getPositionForView(v);
                        getListView().performItemClick(v, position, 0);
                    }
                    v.setBackgroundResource(0);
                    break;
                default:
                    v.setBackgroundResource(0);
                    return false;
            }
            return true;
        }
    };

    private GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent event) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent event, MotionEvent event2, float v, float v2) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            //TODO get the right position when long press is near the bottom of a view
            int position = getListView().pointToPosition(x, y);
            if (position == ListView.INVALID_POSITION) {
                return;
            }

            Post selected = posts.get(position);
            if (selected.hasFullImgUrl()) {
                String imgUrl = selected.getFullImgUrl();
                getListView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                ImageWebViewFragment.openImageWebView(getActivity(), imgUrl);
            }
        }

        @Override
        public boolean onFling(MotionEvent event, MotionEvent event2, float v, float v2) {
            return false;
        }
    });
}
