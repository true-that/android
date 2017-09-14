package com.truethat.android.model;

import com.truethat.android.common.network.NetworkUtil;
import java.io.Serializable;

/**
 * Proudly created by ohad on 11/09/2017 for TrueThat.
 * <p>
 * Describes relations between media nodes and the flow in which user will interact with them.
 * {@code <0, 1, HAPPY>} means users that had a {@code HAPPY} reaction to the 0-indexed media node
 * will than view 1-indexed node.
 * <p>
 * Note that we regard the {@link Media} node order in {@link Scene#mMediaNodes} as its index.
 */

public class Edge implements Serializable {
  private static final long serialVersionUID = -8255565106026309672L;
  private Integer mSourceIndex;
  private Integer mTargetIndex;
  private Emotion mReaction;

  public Edge(int sourceIndex, int targetIndex, Emotion reaction) {
    mSourceIndex = sourceIndex;
    mTargetIndex = targetIndex;
    mReaction = reaction;
  }

  public Edge(int sourceIndex, Emotion reaction) {
    mSourceIndex = sourceIndex;
    mReaction = reaction;
  }

  public Emotion getReaction() {
    return mReaction;
  }

  @Override public String toString() {
    return NetworkUtil.GSON.toJson(this);
  }

  public Integer getTargetIndex() {
    return mTargetIndex;
  }

  Integer getSourceIndex() {
    return mSourceIndex;
  }
}
