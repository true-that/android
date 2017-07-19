package com.truethat.android.application;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

public interface DeviceManager {
  /**
   * @return a pseudo-unique identifier for the device.
   */
  String getDeviceId();

  /**
   * @return device line number. Usually given to it by its SIM card via mobile carriers.
   */
  String getPhoneNumber();
}
