package com.truethat.android.common.util;

import com.truethat.android.common.BaseApplicationTestSuite;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 25/05/2017 for TrueThat.
 */
public class AssetsReaderUtilTest extends BaseApplicationTestSuite {
  private static final String TINY_FILE_PATH = "common/util/tiny_file.txt";

  @Test public void readAsBytes() throws Exception {
    byte[] res = AssetsReaderUtil.readAsBytes(mActivityTestRule.getActivity(), TINY_FILE_PATH);
    assertEquals("My name is Inigo Montoya", new String(res, "UTF-8"));
  }
}