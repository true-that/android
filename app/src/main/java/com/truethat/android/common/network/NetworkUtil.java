package com.truethat.android.common.network;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.LoggingKey;
import com.truethat.android.external.gson.GsonUTCDateAdapter;
import com.truethat.android.external.gson.RuntimeTypeAdapterFactory;
import com.truethat.android.model.Media;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Scene;
import com.truethat.android.model.Video;
import com.truethat.android.view.fragment.SceneFragment;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.regex.Pattern;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Proudly created by ohad on 01/06/2017 for TrueThat.
 */

public class NetworkUtil {
  /**
   * Pattern for field names with Android naming convention.
   */
  private static final Pattern ANDROID_NAMING_PATTERN = Pattern.compile("^m[A-Z].*");
  /**
   * Naming strategy to translate between Android field names and camel case names.
   */
  private static final FieldNamingStrategy NAMING_STRATEGY = new FieldNamingStrategy() {
    /**
     * @return removes the prefixed "m" from the field name.
     */
    @Override public String translateName(Field f) {
      String translatedName = f.getName();
      if (ANDROID_NAMING_PATTERN.matcher(f.getName()).matches()) {
        translatedName = Character.toLowerCase(f.getName().charAt(1)) + f.getName().substring(2);
      }
      return translatedName;
    }
  };
  /**
   * Json converter for network requests.
   * <p>
   * The following modifications has been made:
   * <ul>
   * <li>Android naming strategy, so that serialized json match regular Java notations.</li>
   * <li>Date format, to sync with our backend.</li>
   * <li>{@link Scene} serialization, so that {@link SceneFragment} can be created more
   * freely.</li>
   * </ul>
   */
  public static final Gson GSON = new GsonBuilder().setFieldNamingStrategy(NAMING_STRATEGY)
      .registerTypeAdapter(Date.class, new GsonUTCDateAdapter())
      .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(Media.class)
          .registerSubtype(Photo.class)
          .registerSubtype(Video.class))
      .create();
  /**
   * Logging tag.
   */
  private static final String TAG = NetworkUtil.class.getSimpleName();
  /**
   * Backend URL to use. Can be changed during tests. Use {@link #getBackendUrl()} to retrieve it.
   */
  private static String sBackendUrl = BuildConfig.BASE_BACKEND_URL;
  /**
   * HTTP interceptor to append additional headers.
   */
  private static OkHttpClient CLIENT = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
    @Override public Response intercept(@NonNull Chain chain) throws IOException {
      Request request = chain.request();
      Log.d(TAG, "Sending " + request.method() + " request to " + request.url());
      if (!BuildConfig.DEBUG) {
        Crashlytics.setString(LoggingKey.LAST_NETWORK_REQUEST.name(), request.url().toString());
      }
      Request newRequest = request.newBuilder()
          .addHeader(HeadersContract.VERSION_NAME.getName(), BuildConfig.VERSION_NAME)
          .build();
      return chain.proceed(newRequest);
    }
  }).build();

  /**
   * @param service API interface.
   * @param <T>     type of API interface.
   *
   * @return the provided API interface, inflated by Retrofit.
   */
  public static <T> T createApi(final Class<T> service) {
    Retrofit retrofit = new Retrofit.Builder().baseUrl(getBackendUrl())
        .addConverterFactory(GsonConverterFactory.create(GSON))
        .client(CLIENT)
        .build();

    return retrofit.create(service);
  }

  private static String getBackendUrl() {
    return sBackendUrl;
  }

  @SuppressWarnings("SameParameterValue") @VisibleForTesting
  public static void setBackendUrl(String backendUrl) {
    sBackendUrl = backendUrl;
  }
}
