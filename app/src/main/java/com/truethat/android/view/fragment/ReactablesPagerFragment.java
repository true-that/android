package com.truethat.android.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewPager;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.databinding.FragmentReactablesPagerBinding;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.custom.OnSwipeTouchListener;
import com.truethat.android.view.custom.ReactableFragmentAdapter;
import com.truethat.android.viewmodel.ReactablesPagerViewModel;
import com.truethat.android.viewmodel.viewinterface.ReatablesPagerViewInterface;
import java.util.List;
import retrofit2.Call;

/**
 * Proudly created by ohad on 03/07/2017 for TrueThat.
 */

public class ReactablesPagerFragment extends
    BaseFragment<ReatablesPagerViewInterface, ReactablesPagerViewModel, FragmentReactablesPagerBinding>
    implements ReatablesPagerViewInterface {
  @BindView(R.id.reactablesPager) ViewPager mPager;
  private ReactableFragmentAdapter mReactableFragmentAdapter;
  private ReactablePagerListener mListener;

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof ReactablePagerListener) {
      mListener = (ReactablePagerListener) context;
    } else {
      throw new RuntimeException(context.getClass().getSimpleName()
          + " must implement "
          + ReactablePagerListener.class.getSimpleName());
    }
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Navigation to other activities
    mRootView.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
      @Override public void onSwipeLeft() {
        getViewModel().onSwipeLeft();
      }

      @Override public void onSwipeDown() {
        mListener.onSwipeDown();
      }

      @Override public void onSwipeUp() {
        mListener.onSwipeUp();
      }
    });
    mPager.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
      @Override public void onSwipeLeft() {
        getViewModel().onSwipeLeft();
      }

      @Override public void onSwipeRight() {
        getViewModel().onSwipeRight();
      }

      @Override public void onSwipeDown() {
        mListener.onSwipeDown();
      }

      @Override public void onSwipeUp() {
        mListener.onSwipeUp();
      }
    });
    // Initialize views
    mReactableFragmentAdapter =
        new ReactableFragmentAdapter(getActivity().getSupportFragmentManager());
    mPager.setAdapter(mReactableFragmentAdapter);
  }

  @VisibleForTesting public ReactableFragment getDisplayedReactable() {
    return (ReactableFragment) mReactableFragmentAdapter.instantiateItem(mPager,
        mPager.getCurrentItem());
  }

  @Override public void displayItem(int index) {
    mPager.setCurrentItem(index, true);
  }

  @Override public Call<List<Reactable>> buildFetchReactablesCall() {
    return mListener.buildFetchReactablesCall();
  }

  public interface ReactablePagerListener {
    void onSwipeUp();

    void onSwipeDown();

    Call<List<Reactable>> buildFetchReactablesCall();
  }
}