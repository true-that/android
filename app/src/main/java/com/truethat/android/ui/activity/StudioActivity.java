package com.truethat.android.ui.activity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.truethat.android.R;
import com.truethat.android.common.network.StudioApi;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import com.truethat.android.ui.common.camera.CameraFragment;
import com.truethat.android.ui.common.util.OnSwipeTouchListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.view.View.GONE;
import static com.truethat.android.ui.activity.RepertoireActivity.FROM_REPERTOIRE;

public class StudioActivity extends BaseActivity implements CameraFragment.OnPictureTakenListener {

  @VisibleForTesting @BindString(R.string.signing_in) String UNAUTHORIZED_TOAST;
  @VisibleForTesting @BindString(R.string.sent_failed) String SENT_FAILED;
  @BindView(R.id.captureButton) ImageButton mCaptureButton;
  @BindView(R.id.switchCameraButton) ImageButton mSwitchCameraButton;
  @BindView(R.id.sendButton) ImageButton mSendButton;
  @BindView(R.id.cancelButton) ImageButton mCancelButton;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  private DirectingState mDirectingState = DirectingState.DIRECTING;
  private Reactable mDirectedReactable;

  /**
   * API interface for saving scenes.
   */
  private StudioApi mStudioApi;
  private Call<Reactable> mSaveSceneCall;
  private CameraFragment mCameraFragment;
  private Callback<Reactable> mSaveSceneCallback = new Callback<Reactable>() {
    @Override
    public void onResponse(@NonNull Call<Reactable> call, @NonNull Response<Reactable> response) {
      if (response.isSuccessful() && response.body() != null) {
        mDirectedReactable = response.body();
        onPublished();
      } else {
        Log.e(TAG,
            "Failed to save scene.\n" + response.code() + " " + response.message() + "\n" + response
                .headers());
        StudioActivity.this.runOnUiThread(new Runnable() {
          @Override public void run() {
            onPublishError();
          }
        });
      }
    }

    @Override public void onFailure(@NonNull Call<Reactable> call, @NonNull Throwable t) {
      Log.e(TAG, "Saving scene request to " + call.request().url() + " had failed.", t);
      StudioActivity.this.runOnUiThread(new Runnable() {
        @Override public void run() {
          onPublishError();
        }
      });
    }
  };

  /**
   * UI initiated picture taking.
   */
  @OnClick(R.id.captureButton) public void captureImage() {
    if (mAuthManager.isAuthOk() && mCameraFragment.isCameraOpen()) {
      mCameraFragment.takePicture();
    } else {
      Toast.makeText(this, UNAUTHORIZED_TOAST, Toast.LENGTH_SHORT).show();
    }
  }

  @Override public void processImage(Image image) {
    mDirectedReactable = new Scene(mAuthManager.currentUser(), CameraUtil.toByteArray(image));
    runOnUiThread(new Runnable() {
      @Override public void run() {
        onApproval();
      }
    });
  }

  @OnClick(R.id.switchCameraButton) public void switchCamera() {
    mCameraFragment.switchCamera();
  }

  public Reactable getDirectedReactable() {
    return mDirectedReactable;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Defines the navigation to the Theater.
    mRootView.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeUp() {
        Intent intent = new Intent(StudioActivity.this, RepertoireActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        //onBackPressed();
        //finish();
      }

      @Override public void onSwipeDown() {
        startActivity(new Intent(StudioActivity.this, TheaterActivity.class));
      }
    });
    // Initializes API interface
    mStudioApi = createApiInterface(StudioApi.class);
    // Hooks the camera fragment
    mCameraFragment =
        (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_studio;
  }

  @Override protected void onResume() {
    super.onResume();
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
  }

  @Override protected void onPause() {
    super.onPause();
    cancelSent();
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    // Initialize activity transitions.
    if (intent.getBooleanExtra(FROM_REPERTOIRE, false)) {
      this.overridePendingTransition(R.animator.slide_in_bottom, R.animator.slide_out_bottom);
    } else {
      this.overridePendingTransition(R.animator.slide_in_top, R.animator.slide_out_top);
    }
  }

  @MainThread @OnClick(R.id.cancelButton) void onDirecting() {
    Log.v(TAG, "Change state: " + DirectingState.DIRECTING.name());
    mDirectingState = DirectingState.DIRECTING;
    // Hides approval buttons.
    mCancelButton.setVisibility(GONE);
    mSendButton.setVisibility(GONE);
    // Exposes capture buttons.
    mCaptureButton.setVisibility(View.VISIBLE);
    mSwitchCameraButton.setVisibility(View.VISIBLE);
    // Restores the camera preview.
    mCameraFragment.restorePreview();
    // Hides loading image.
    mLoadingImage.setVisibility(GONE);
    // Removes preview tint
    mCameraFragment.getCameraPreview().setBackgroundTintList(null);
    // Delete reactable.
    mDirectedReactable = null;
  }

  @MainThread @OnClick(R.id.sendButton) void onSent() {
    Log.v(TAG, "Change state: " + DirectingState.SENT.name());
    mDirectingState = DirectingState.SENT;
    mSaveSceneCall = mDirectedReactable.createApiCall(mStudioApi, mGson);
    mSaveSceneCall.enqueue(mSaveSceneCallback);
    // Hides buttons.
    mCaptureButton.setVisibility(GONE);
    mSwitchCameraButton.setVisibility(GONE);
    mCancelButton.setVisibility(GONE);
    mSendButton.setVisibility(GONE);
    // Tinting camera preview and showing a loader.
    mCameraFragment.getCameraPreview().setBackgroundTintList(getColorStateList(R.color.tint));
    mLoadingImage.setVisibility(View.VISIBLE);
    mLoadingImage.bringToFront();
    Glide.with(this).load(R.drawable.anim_loading_elephant).into(mLoadingImage);
  }

  private void cancelSent() {
    if (mSaveSceneCall != null) {
      mSaveSceneCall.cancel();
      onApproval();
    }
  }

  @MainThread private void onApproval() {
    Log.v(TAG, "Change state: " + DirectingState.APPROVAL.name());
    mDirectingState = DirectingState.APPROVAL;
    // Exposes approval buttons.
    mCancelButton.setVisibility(View.VISIBLE);
    mSendButton.setVisibility(View.VISIBLE);
    // Hides capture button.
    mCaptureButton.setVisibility(GONE);
    mSwitchCameraButton.setVisibility(GONE);
    // Hides loading image.
    mLoadingImage.setVisibility(GONE);
    // Removes preview tint
    mCameraFragment.getCameraPreview().setBackgroundTintList(null);
  }

  private void onPublished() {
    Log.v(TAG, "Change state: " + DirectingState.PUBLISHED.name());
    mDirectingState = DirectingState.PUBLISHED;
    // Navigate to theater after publishing.
    startActivity(new Intent(StudioActivity.this, TheaterActivity.class));
  }

  @MainThread private void onPublishError() {
    Toast.makeText(this, SENT_FAILED, Toast.LENGTH_SHORT).show();
    onApproval();
  }

  private enum DirectingState {
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
