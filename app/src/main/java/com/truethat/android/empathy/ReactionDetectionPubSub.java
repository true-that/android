package com.truethat.android.empathy;

import android.media.Image;
import com.truethat.android.model.Emotion;
import com.truethat.android.view.fragment.CameraFragment;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 * <p>
 * Communication interface through which {@link ReactionDetectionManager} publishes its
 * classification and subscribes to requests input.
 */

public interface ReactionDetectionPubSub {
  /**
   * Handler for emotion reaction detected event.
   *
   * @param reaction as determined by {@link ReactionDetectionManager}.
   */
  void onReactionDetected(Emotion reaction);

  /**
   * Request additional input (i.e. takes a photo with the device's camera, or calls {@link
   * CameraFragment#takePicture()}), in order to invoke {@link ReactionDetectionManager#attempt(Image)}
   */
  void requestInput();
}
