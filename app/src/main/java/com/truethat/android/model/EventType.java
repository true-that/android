package com.truethat.android.model;

/**
 * Proudly created by ohad on 07/06/2017 for TrueThat.
 * <p>
 * EventCodes of our deal users and magical application. Each event code has an integer code, which
 * should be aligned
 * with our backend.
 *
 * @backend <a>https://goo.gl/5HOYDY</a>
 */
public enum EventType {
  /**
   * User viewed a scene.
   */
  VIEW,

  /**
   * User reacted to a scene.
   */
  REACTION
}
