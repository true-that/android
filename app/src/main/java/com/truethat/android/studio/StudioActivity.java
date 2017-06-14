package com.truethat.android.studio;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.Scene;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.camera.CameraUtil;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.OnSwipeTouchListener;
import com.truethat.android.theater.TheaterActivity;
import java.io.IOException;
import java.util.Date;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudioActivity extends CameraActivity {

  /**
   * File name for HTTP post request for saving scenes.
   */
  private static final String FILENAME = "studio-image";
  /**
   * Retrofit API interface for saving scenes.
   */
  private StudioAPI mStudioAPI = NetworkUtil.createAPI(StudioAPI.class);
  private Callback<Scene> mSaveSceneCallback = new Callback<Scene>() {
    @Override public void onResponse(@NonNull Call<Scene> call, @NonNull Response<Scene> response) {
      if (response.isSuccessful()) {
        try {
          Scene respondedScene = response.body();
          if (respondedScene == null) {
            throw new AssertionError("Responded scene no tiene nada!");
          }
          App.getInternalStorage().write(StudioActivity.this, respondedScene.internalStoragePath(), respondedScene);
        } catch (IOException e) {
          Log.e(TAG, "Failed to save scene to internal storage.", e);
        } catch (NullPointerException e) {
          Log.e(TAG, "saveScene response is null.");
        }
      } else {
        Log.e(TAG, "Failed to save scene.\n" + response.code() + " " + response.message() + "\n" + response.headers());
      }
    }

    @Override public void onFailure(@NonNull Call<Scene> call, @NonNull Throwable t) {
      Log.e(TAG, "Saving scene request to " + call.request().url() + " had failed.", t);
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_studio);
    // Initialize activity transitions.
    this.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_right);
    // Defines the navigation to the Theater.
    final ViewGroup rootView = (ViewGroup) this.findViewById(R.id.studioActivity);
    rootView.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeLeft() {
        startActivity(new Intent(StudioActivity.this, TheaterActivity.class));
      }
    });
    // Sets the camera preview.
    mCameraPreview = (TextureView) this.findViewById(R.id.cameraPreview);
  }

  public void takePicture(View view) {
    takePicture();
  }

  protected void processImage() {
    Log.v(TAG, "Sending multipart request to: " + NetworkUtil.getBackendUrl());
    MultipartBody.Part imagePart = MultipartBody.Part.createFormData(StudioAPI.SCENE_IMAGE_PART, FILENAME,
        RequestBody.create(MediaType.parse("image/jpg"), CameraUtil.toByteArray(supplyImage())));
    MultipartBody.Part creatorPart = MultipartBody.Part.createFormData(StudioAPI.DIRECTOR_PART,
        Long.toString(App.getAuthModule().getUser().getId()));
    MultipartBody.Part timestampPart =
        MultipartBody.Part.createFormData(StudioAPI.CREATED_PART, Long.toString(new Date().getTime()));
    mStudioAPI.saveScene(imagePart, creatorPart, timestampPart).enqueue(mSaveSceneCallback);
  }
}
