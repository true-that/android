package com.truethat.android.model;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;

/**
 * Proudly created by ohad on 13/09/2017 for TrueThat.
 */

class FlowTree {
  /**
   * Owner of this lovely tree.
   */
  private Scene mOwner;
  /**
   * Maps {@link Media#mId} to tree nodes
   */
  private Map<Long, Node> mNodes = new HashMap<>();

  FlowTree(Scene owner) {
    mOwner = owner;
  }

  @Nullable public Media getRoot() {
    if (mNodes.isEmpty()) return null;
    Node node = mNodes.entrySet().iterator().next().getValue();
    while (node.getParent() != null) {
      node = node.getParent();
    }
    return node.getMedia();
  }

  /**
   * Removes the node that is associated with the provided media ID and all its children.
   *
   * @param mediaId to remove.
   *
   * @return the media of the parent node of the removed node.
   */
  @Nullable public Media remove(long mediaId) {
    Media parentMedia = null;
    if (mNodes.containsKey(mediaId)) {
      Node node = mNodes.get(mediaId);
      if (node.getParent() != null) {
        parentMedia = node.getParent().getMedia();
        node.getParent().removeChild(node);
      }
      for (Node childNode : node.mChildren.values()) {
        remove(childNode.getMedia().getId());
      }
      mOwner.removeMediaInternal(node.getMedia());
      mNodes.remove(mediaId);
    }
    return parentMedia;
  }

  boolean isTree() {
    int numRoots = 0;
    for (Node node : mNodes.values()) {
      if (node.getParent() == null) {
        numRoots++;
      }
    }
    return numRoots <= 1;
  }

  @VisibleForTesting @Nullable Media getMedia(long mediaId) {
    if (!mNodes.containsKey(mediaId)) {
      return null;
    }
    return mNodes.get(mediaId).getMedia();
  }

  /**
   * @param mediaId of the source node.
   * @param emotion that resembles the edge color.
   *
   * @return the child of the node associated with {@code mediaId} that is colored by {@code
   * emotion}.
   */
  @Nullable Media getChild(long mediaId, Emotion emotion) {
    if (!mNodes.containsKey(mediaId)) {
      throw new IllegalArgumentException(
          "Node with media ID " + mediaId + " was not added to the tree.");
    }
    if (mNodes.get(mediaId).getChildren().containsKey(emotion)) {
      return mNodes.get(mediaId).getChildren().get(emotion).getMedia();
    }
    return null;
  }

  /**
   * @param mediaId to return the parent node of.
   *
   * @return the media of the parent node of the node associated with {@code mediaId}.
   */
  @Nullable Media getParent(long mediaId) {
    if (!mNodes.containsKey(mediaId)) {
      throw new IllegalArgumentException(
          "Node with media ID " + mediaId + " was not added to the tree.");
    }
    if (mNodes.get(mediaId).getParent() == null) {
      return null;
    }
    return mNodes.get(mediaId).getParent().getMedia();
  }

  /**
   * Creates new nodes in the tree.
   *
   * @param mediaItems to create nodes from.
   */
  void addNode(Media... mediaItems) {
    for (Media media : mediaItems) {
      Node newNode = new Node(media);
      mNodes.put(media.getId(), newNode);
    }
  }

  Map<Long, Node> getNodes() {
    return mNodes;
  }

  void addEdge(Edge... edges) {
    for (Edge edge : edges) {
      if (!mNodes.containsKey(edge.getSourceId())) {
        throw new IllegalArgumentException(
            "Source node (id = " + edge.getSourceId() + ") does not exists in the tree.");
      }
      if (!mNodes.containsKey(edge.getTargetId())) {
        throw new IllegalArgumentException(
            "Target node (id = " + edge.getTargetId() + ") does not exists in the tree.");
      }
      mNodes.get(edge.getSourceId()).addChild(mNodes.get(edge.getTargetId()), edge.getReaction());
    }
  }

  private static class Node {
    private Media mMedia;
    private Node mParent;
    private Map<Emotion, Node> mChildren = new HashMap<>();

    Node(Media media) {
      mMedia = media;
    }

    public Media getMedia() {
      return mMedia;
    }

    public Node getParent() {
      return mParent;
    }

    public void setParent(Node parent) {
      mParent = parent;
    }

    @Override public int hashCode() {
      return mMedia != null ? mMedia.hashCode() : 0;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Node)) return false;

      Node node = (Node) o;

      return mMedia != null ? mMedia.equals(node.mMedia) : node.mMedia == null;
    }

    @Override public String toString() {
      return "Node: (media = " + mMedia + "\nparent = " + mParent + "\nchildren = " + mChildren;
    }

    Map<Emotion, Node> getChildren() {
      return mChildren;
    }

    void addChild(Node node, Emotion emotion) {
      mChildren.put(emotion, node);
      node.setParent(this);
    }

    void removeChild(Node node) {
      for (Map.Entry<Emotion, Node> childEntry : mChildren.entrySet()) {
        if (node.equals(childEntry.getValue())) {
          mChildren.remove(childEntry.getKey());
          return;
        }
      }
    }
  }
}