package com.truethat.android.common.network;

/**
 * Proudly created by ohad on 07/06/2017 for TrueThat.
 *
 * EventCodes of our deal users and magical application. Each event code has an integer code, which should be aligned
 * with our backend.
 *
 * @backend <a>https://goo.gl/IBiMDz</a>
 */
public enum EventCode {
  /**
   * User viewed a reactable.
   */
  REACTABLE_VIEW(100),

  /**
   * User reacted to a reactable.
   */
  REACTABLE_REACTION(101);

  private int mCode;

  EventCode(int code) {
    mCode = code;
  }
}
