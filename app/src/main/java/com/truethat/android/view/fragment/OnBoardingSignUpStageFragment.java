package com.truethat.android.view.fragment;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import com.truethat.android.R;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.databinding.FragmentOnBoardingSignUpBinding;
import com.truethat.android.view.activity.OnBoardingActivity;
import com.truethat.android.view.custom.BaseDialog;
import com.truethat.android.viewmodel.OnBoardingSignUpStageViewModel;
import com.truethat.android.viewmodel.viewinterface.OnBoardingSignUpStageViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * Proudly created by ohad on 25/10/2017 for TrueThat.
 */

public class OnBoardingSignUpStageFragment extends
    OnBoardingStageFragment<OnBoardingSignUpStageViewInterface, OnBoardingSignUpStageViewModel, FragmentOnBoardingSignUpBinding>
    implements OnBoardingSignUpStageViewInterface, AuthListener {
  @BindView(R.id.nameEditText) EditText mNameEditText;
  @BindView(R.id.onBoarding_signUp_loadingImage) ImageView mLoadingImage;
  private BaseDialog mFailedSignUpDialog;

  public static OnBoardingSignUpStageFragment newInstance() {
    Bundle args = new Bundle();
    OnBoardingSignUpStageFragment fragment = new OnBoardingSignUpStageFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onAuthOk() {
    mOnBoardingListener.onComplete(OnBoardingActivity.SIGN_UP_STAGE_INDEX);
  }

  @Override public void onAuthFailed() {
    Log.d(TAG, "onAuthFailed");
    getViewModel().failedSignUp();
  }

  @Override public void onVisible() {
    super.onVisible();
    // Plays loading animation.
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
  }

  @Override public void onHidden() {
    super.onHidden();
    if (mNameEditText != null) {
      hideSoftKeyboard();
    }
    if (mFailedSignUpDialog != null) {
      mFailedSignUpDialog.dismiss();
      mFailedSignUpDialog = null;
    }
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_on_boarding_sign_up, getContext());
  }

  @Override public void requestNameEditFocus() {
    mNameEditText.requestFocus();
  }

  @Override public void clearNameEditFocus() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        mNameEditText.clearFocus();
      }
    });
  }

  @Override public void hideSoftKeyboard() {
    if (getActivity() != null) {
      InputMethodManager inputMethodManager =
          (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
    }
  }

  @Override public void showSoftKeyboard() {
    InputMethodManager inputMethodManager =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMethodManager.showSoftInput(mNameEditText, InputMethodManager.SHOW_FORCED);
  }

  @Override public AuthListener getAuthListener() {
    return this;
  }

  @Override public void showFailedSignUpDialog() {
    if (mFailedSignUpDialog != null) {
      mFailedSignUpDialog.dismiss();
    }
    mFailedSignUpDialog = new BaseDialog(getContext(), R.string.on_boarding_sign_up_dialog_title,
        R.string.on_boarding_sign_up_dialog_message, R.string.on_boarding_sign_up_dialog_button);
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        mFailedSignUpDialog.show();
      }
    });
  }

  @OnTextChanged(R.id.nameEditText) void onNameChange(CharSequence typedName) {
    getViewModel().mNameEditText.set(typedName.toString());
  }

  @SuppressWarnings("SameReturnValue") @OnEditorAction(R.id.nameEditText) boolean onNameDone(
      int actionId) {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      getViewModel().onNameDone();
    }
    return true;
  }

  @OnFocusChange(R.id.nameEditText) void onNameFocusChange(boolean hasFocus) {
    getViewModel().onNameFocusChange(hasFocus);
  }

  @OnClick(R.id.onBoarding_signUpButton) void doSignUp() {
    getViewModel().doSignUp();
  }
}
