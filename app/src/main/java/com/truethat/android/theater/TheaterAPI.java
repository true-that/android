package com.truethat.android.theater;

import com.truethat.android.common.media.Reactable;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Proudly created by ohad on 01/06/2017 for TrueThat.
 *
 * @backend <a>https://goo.gl/PbBPFT</a>
 */

public interface TheaterAPI {
  /**
   * Get reactables from out beloved backend to add some drama to our users life.
   */
  @GET("theater") Call<List<Reactable>> getReactables();

  /**
   * Informs our backend of the the current user interaction with reactables.
   *
   * @param reactableEvent the encapsulates all the event information.
   */
  @POST("theater") Call<ResponseBody> postEvent(@Body ReactableEvent reactableEvent);
}
