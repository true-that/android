package com.truethat.android.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.model.Pose;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Short;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.viewmodel.viewinterface.StudioViewInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

public class StudioViewModel extends BaseViewModel<StudioViewInterface>
    implements CameraFragment.CameraFragmentListener {
  @DrawableRes static final int CAPTURE_RESOURCE = R.drawable.capture;
  @DrawableRes static final int RECORD_RESOURCE = R.drawable.record;
  public final ObservableBoolean mCaptureButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mCancelButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mSwitchCameraButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mSendButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mLoadingImageVisibility = new ObservableBoolean();
  public final ObservableBoolean mReactablePreviewVisibility = new ObservableBoolean();
  public final ObservableBoolean mCameraPreviewVisibility = new ObservableBoolean();
  public final ObservableInt mCaptureButtonDrawableResource = new ObservableInt(CAPTURE_RESOURCE);
  private DirectingState mDirectingState = DirectingState.DIRECTING;
  private Reactable mDirectedReactable;
  /**
   * Api call to save {@link #mDirectedReactable}.
   */
  private Call<Reactable> mSaveReactableCall;
  /**
   * Callback for {@link #mSaveReactableCall}.
   */
  private Callback<Reactable> mSaveReactableCallback = new Callback<Reactable>() {
    @Override
    public void onResponse(@NonNull Call<Reactable> call, @NonNull Response<Reactable> response) {
      if (response.isSuccessful() && response.body() != null) {
        mDirectedReactable = response.body();
        onPublished();
      } else {
        Log.e(TAG,
            "Failed to save pose.\n" + response.code() + " " + response.message() + "\n" + response
                .headers());
        onPublishError();
      }
    }

    @Override public void onFailure(@NonNull Call<Reactable> call, @NonNull Throwable t) {
      t.printStackTrace();
      Log.e(TAG, "Saving pose request to " + call.request().url() + " had failed.", t);
      onPublishError();
    }
  };

  @Override public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
  }

  @Override public void onStop() {
    super.onStop();
    cancelSent();
  }

  @Override public void onStart() {
    super.onStart();
    switch (mDirectingState) {
      case APPROVAL:
        onApproval();
        break;
      case SENT:
        cancelSent();
        break;
      case PUBLISHED:
      case DIRECTING:
      default:
        onDirecting();
    }
    // Set default capture button
    mCaptureButtonDrawableResource.set(CAPTURE_RESOURCE);
  }

  @Override public void onImageAvailable(Image image) {
    mDirectedReactable =
        new Pose(AppContainer.getAuthManager().getCurrentUser(), CameraUtil.toByteArray(image));
    onApproval();
  }

  @Override public void onVideoAvailable(String videoPath) {
    mCaptureButtonDrawableResource.set(CAPTURE_RESOURCE);
    mDirectedReactable = new Short(AppContainer.getAuthManager().getCurrentUser(), videoPath);
    onApproval();
  }

  @Override public void onVideoRecordStart() {
    mCaptureButtonDrawableResource.set(RECORD_RESOURCE);
  }

  public void onSent() {
    Log.v(TAG, "Change state: " + DirectingState.SENT.name());
    mDirectingState = DirectingState.SENT;
    mSaveReactableCall = mDirectedReactable.createApiCall();
    mSaveReactableCall.enqueue(mSaveReactableCallback);
    // Hides buttons.
    mCaptureButtonVisibility.set(false);
    mSwitchCameraButtonVisibility.set(false);
    mCancelButtonVisibility.set(false);
    mSendButtonVisibility.set(false);
    mLoadingImageVisibility.set(true);
    getView().onSent();
  }

  public void disapprove() {
    Log.v(TAG, "Reactable disapproved.");
    onDirecting();
  }

  Reactable getDirectedReactable() {
    return mDirectedReactable;
  }

  private void onApproval() {
    if (mDirectedReactable == null) {
      onDirecting();
    }
    Log.v(TAG, "Change state: " + DirectingState.APPROVAL.name());
    mDirectingState = DirectingState.APPROVAL;
    // Exposes approval buttons.
    mCancelButtonVisibility.set(true);
    mSendButtonVisibility.set(true);
    // Hides capture button.
    mCaptureButtonVisibility.set(false);
    mSwitchCameraButtonVisibility.set(false);
    // Hides loading image.
    mLoadingImageVisibility.set(false);
    // Shows the directed reactable preview, and hides the camera preview.
    mReactablePreviewVisibility.set(true);
    mCameraPreviewVisibility.set(false);
    getView().onApproval();
    getView().displayPreview(mDirectedReactable);
  }

  private void onDirecting() {
    Log.v(TAG, "Change state: " + DirectingState.DIRECTING.name());
    mDirectingState = DirectingState.DIRECTING;
    // Hides approval buttons.
    mCancelButtonVisibility.set(false);
    mSendButtonVisibility.set(false);
    // Exposes capture buttons.
    mCaptureButtonVisibility.set(true);
    mSwitchCameraButtonVisibility.set(true);
    // Hides loading image.
    mLoadingImageVisibility.set(false);
    // Hides the directed reactable preview, and exposes the camera preview.
    mReactablePreviewVisibility.set(false);
    mCameraPreviewVisibility.set(true);
    // Delete reactable.
    mDirectedReactable = null;
    getView().onDirecting();
  }

  private void cancelSent() {
    if (mSaveReactableCall != null) {
      mSaveReactableCall.cancel();
      onApproval();
    }
  }

  private void onPublished() {
    Log.v(TAG, "Change state: " + DirectingState.PUBLISHED.name());
    mDirectingState = DirectingState.PUBLISHED;
    getView().onPublished();
  }

  private void onPublishError() {
    getView().toast(getContext().getResources().getString(R.string.sent_failed));
    onApproval();
  }

  @VisibleForTesting enum DirectingState {
    /**
     * The user directs the piece of art he is about to create, usually involves the camera preview.
     */
    DIRECTING, /**
     * A {@link Reactable} was created and is awaiting final approval to be published.
     */
    APPROVAL, /**
     * A {@link Reactable} was approved and is now being sent to the server.
     */
    SENT, /**
     * The created {@link Reactable} was successfully received by the server.
     */
    PUBLISHED
  }
}
