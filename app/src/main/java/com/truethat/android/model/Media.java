package com.truethat.android.model;

import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.StudioApi;
import com.truethat.android.view.fragment.MediaFragment;
import okhttp3.MultipartBody;

/**
 * Proudly created by ohad on 07/09/2017 for TrueThat.
 * <p>
 * Media items such as videos or photos.
 * <p>
 * Each implementation should register at {@link NetworkUtil#GSON}.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/Media.java</a>
 */

public abstract class Media extends BaseModel {
  private static final long serialVersionUID = 2882621624492397474L;
  private String mUrl;

  public Media() {
  }

  @VisibleForTesting public Media(@Nullable Long id, String url) {
    super(id);
    mUrl = url;
  }

  Media(Parcel in) {
    super(in);
    mUrl = (String) in.readValue(String.class.getClassLoader());
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeValue(mUrl);
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mUrl != null ? mUrl.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Media)) return false;
    if (!super.equals(o)) return false;

    Media media = (Media) o;

    return mUrl != null ? mUrl.equals(media.mUrl) : media.mUrl == null;
  }

  public String getUrl() {
    return mUrl;
  }

  /**
   * @return a view displaying this media.
   */
  public abstract MediaFragment createFragment();

  /**
   * @return a HTTP multipart part with the binary data of this media.
   */
  abstract MultipartBody.Part createPart();

  /**
   * @return the HTTP multipart name for that media.
   */
  String generatePartName() {
    return StudioApi.MEDIA_PART_PREFIX + mId;
  }
}
