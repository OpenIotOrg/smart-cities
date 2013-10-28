/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2013-08-07 19:00:49 UTC)
 * on 2013-08-13 at 18:32:26 UTC 
 * Modify at your own risk.
 */

package com.google.api.services.smartmeetings.model;

/**
 * Model definition for UserInfo.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the . For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class UserInfo extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<java.lang.String> pushIds;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<java.lang.String> topics;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<Discussion> topicsList;

  static {
    // hack to force ProGuard to consider Discussion used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(Discussion.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private User user;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userName;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public UserInfo setId(java.lang.Long id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.String> getPushIds() {
    return pushIds;
  }

  /**
   * @param pushIds pushIds or {@code null} for none
   */
  public UserInfo setPushIds(java.util.List<java.lang.String> pushIds) {
    this.pushIds = pushIds;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.String> getTopics() {
    return topics;
  }

  /**
   * @param topics topics or {@code null} for none
   */
  public UserInfo setTopics(java.util.List<java.lang.String> topics) {
    this.topics = topics;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<Discussion> getTopicsList() {
    return topicsList;
  }

  /**
   * @param topicsList topicsList or {@code null} for none
   */
  public UserInfo setTopicsList(java.util.List<Discussion> topicsList) {
    this.topicsList = topicsList;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public User getUser() {
    return user;
  }

  /**
   * @param user user or {@code null} for none
   */
  public UserInfo setUser(User user) {
    this.user = user;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserName() {
    return userName;
  }

  /**
   * @param userName userName or {@code null} for none
   */
  public UserInfo setUserName(java.lang.String userName) {
    this.userName = userName;
    return this;
  }

  @Override
  public UserInfo set(String fieldName, Object value) {
    return (UserInfo) super.set(fieldName, value);
  }

  @Override
  public UserInfo clone() {
    return (UserInfo) super.clone();
  }

}
