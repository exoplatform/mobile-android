/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.utils;

import org.exoplatform.R;

public class ExoConstants {

  public static final String PLATFORM_EDITION                      = "platformEdition";

  public static final String PLATFORM_VERSION                      = "platformVersion";

  public static final String IS_MOBILE_COMPLIANT                   = "isMobileCompliant";

  public static final String PLATFORM_CURRENT_REPO_NAME            = "currentRepoName";

  public static final String LOGIN_YES                             = "YES";

  public static final String LOGIN_NO                              = "NO";

  public static final String LOGIN_INVALID                         = "INVALID";

  public static final String LOGIN_UNREACHABLE                     = "UNREACHABLE";

  public static final String LOGIN_ERROR                           = "ERROR";

  public static final String COMPOSE_TYPE                          = "COMPOSE_TYPE";

  public static final String SETTING_TYPE                          = "SETTING_TYPE";

  public static final String WEB_VIEW_URL                          = "WEB_VIEW_URL";

  public static final String WEB_VIEW_TITLE                        = "WEB_VIEW_TITLE";

  public static final String WEB_VIEW_MIME_TYPE                    = "WEB_VIEW_MIME_TYPE";
  
  public static final String WEB_VIEW_ALLOW_JS                     = "WEB_VIEW_ALLOW_JS";

  public static final String ACTIVITY_DETAIL_EXTRA                 = "ACTIVITY_DETAIL_EXTRA";

  public static final String ACTIVITY_CURRENT_POSITION             = "ACTIVITY_CURRENT_POSITION";

  public static final int    COMPOSE_POST_TYPE                     = 0;

  public static final int    COMPOSE_COMMENT_TYPE                  = 1;

  public static final int    NUMBER_OF_ACTIVITY                    = 50;

  public static final int    NUMBER_OF_MORE_ACTIVITY               = 20;

  public static final int    NUMBER_OF_LIKES_PARAM                 = 50;

  public static final int    NUMBER_OF_COMMENTS_PARAM              = 50;

  public static final int    NUMBER_OF_MORE_COMMENTS_PARAM         = 5;

  public static final int    AVATAR_DEFAULT_SIZE                   = 130;

  public static final int    TAKE_PICTURE_WITH_CAMERA              = 7;

  public static final int    REQUEST_ADD_PHOTO                     = 8;

  public static final String PHOTO_ALBUM_IMAGE_TYPE                = "image/*";

  public static final String HTTP_PROTOCOL                         = "http";

  public static final String HTTPS_PROTOCOL                        = "https";

  public static final int    ACTIVITY_PORT                         = 80;

  public static final String ACTIVITY_PORTAL_CONTAINER             = "portal";

  public static final String ACTIVITY_REST_CONTEXT                 = "rest";

  public static final String ACTIVITY_REST_VERSION                 = "v1-alpha2";

  public static final String ACTIVITY_ORGANIZATION                 = "organization";

  public static final int    DEFAULT_AVATAR                        = R.drawable.default_avatar;

  public static final String MOBILE_FOLDER                         = "Mobile";

  public static final String ACTIVITY_ID_EXTRA                     = "ACTIVITY_ID_EXTRA";

  public static final String SELECTED_IMAGE_EXTRA                  = "SELECTED_IMAGE_EXTRA";

  public static final String SELECTED_IMAGE_MODE                   = "SELECTED_IMAGE_MODE";

  public static final String SOCIAL_LINKFY_EXTRA                   = "SOCIAL_LINKFY_EXTRA";

  public static final String SOCIAL_LIKED_LIST_EXTRA               = "SOCIAL_LIKED_LIST_EXTRA";

  public static final String SOCIAL_SPACE                          = "space";

  public static final String EXO_PREFERENCE                        = "exo_preference";

  public static final String EXO_PRF_DOMAIN                        = "exo_prf_domain";

  public static final String EXO_PRF_DOMAIN_INDEX                  = "exo_prf_domain_index";

  public static final String EXO_PRF_USERNAME                      = "exo_prf_username";

  public static final String EXO_PRF_PASSWORD                      = "exo_prf_password";

  public static final String EXO_PRF_LANGUAGE                      = "exo_prf_language";

  public static final String EXO_PRF_LOCALIZE                      = "exo_prf_localize";

  public static final String ENGLISH_LOCALIZATION                  = "en";

  public static final String FRENCH_LOCALIZATION                   = "fr";

  public static final String GERMAN_LOCALIZATION                   = "de";

  public static final String SPANISH_LOCALIZATION                  = "es";

  public static final String SETTING_SOCIAL_FILTER                 = "SETTING_SOCIAL_FILTER";

  public static final String SETTING_SOCIAL_FILTER_INDEX           = "SETTING_SOCIAL_FILTER_INDEX";

  public static final String SETTING_DOCUMENT_SHOW_HIDDEN_FILE     = "SETTING_DOCUMENT_SHOW_HIDDEN_FILE";

  public static final String DOCUMENT_REPOSITORY                   = "repository";

  public static final String DOCUMENT_COLLABORATION                = "collaboration";

  public static final String DOCUMENT_JCR_PATH                     = "/rest/private/jcr";

  public static final String DOCUMENT_USERS                        = "Users";

  public static final String DOCUMENT_DRIVE_PATH_REST              = "/rest/managedocument/getDrives?driveType=";

  public static final String DOCUMENT_FILE_PATH_REST               = "/rest/managedocument/getFoldersAndFiles?driveName=";

  public static final String DOCUMENT_WORKSPACE_NAME               = "&workspaceName=";

  public static final String DOCUMENT_CURRENT_FOLDER               = "&currentFolder=";

  public static final String DASHBOARD_PATH                        = "/rest/private/dashboards";

  public static final String DOMAIN_SUFFIX                         = "/portal/private/intranet";

  public static final String DOMAIN_PLATFORM_VERSION               = "/rest/private/platform/info";

  public static final String IMAGE_TYPE                            = "image/png";

  public static final String SPECIAL_CHAR_NAME_SET                 = "[\\[\\]\\/\\&\\~\\?\\*\\|\\<\\>\\\"\\;\\:\\+\\\\]";

  public static final String SPECIAL_CHAR_URL_SET                  = "[\\[\\]\\&\\~\\?\\*\\|\\<\\>\\\"\\;\\+\\\\]";

  public static final int    HOME_AVATAR_BORDER_COLOR              = 0x44444444;

  public static final int    HOME_SOCIAL_MAX_NUMBER                = 10;

  public static final String SOCIAL_FILE_CACHE                     = "SocialCache";

  public static final String DOCUMENT_FILE_CACHE                   = "DocumentCache";

  public static final String DOCUMENT_PERSONAL_DRIVER              = "personal";

  public static final String DOCUMENT_PERSONAL_DRIVER_SHOW_PRIVATE = "&showPrivate=";

  public static final String DOCUMENT_GROUP_DRIVER                 = "group";

  public static final String DOCUMENT_GENERAL_DRIVER               = "general";

  public static final String USERNAME                              = "username";

  public static final String TENANT                                = "tenant";

  /* Http response status */
  public static final int    UNKNOWN                               = 309;

  /* Server setting */
//  public static final String SETTING_ADDING_SERVER                 = "SETTING_ADDING_SERVER";

  public static final String EXO_MASTER_PASSWORD                   = "EXO_MASTER_PASSWORD";

  public static final String EXO_SERVER_SETTING_FILE               = "ServerList.xml";

  public static final String EXO_OLD_SERVER_SETTING_FILE           = "DefaultServerList.xml";

  public static final String EXO_SERVER_OBJ                        = "EXO_SERVER_OBJ";

  /* eXo URL scheme */
  public static final String EXO_URL_USERNAME                      = "username";

  public static final String EXO_URL_SERVER                        = "serverUrl";

  public static final String EXO_REMEMBER_ME                       = "rememberMe";

  public static final String EXO_AUTOLOGIN                         = "autoLogin";

  /** Account setting */
  public static final String ACCOUNT_SETTING                       = "ACCOUNT_SETTING";

  public static final String SERVER_SETTING_HELPER                 = "SERVER_SETTING_HELPER";

  /** email used for sign in */
  public static final String EXO_EMAIL                             = "EXO_EMAIL";
}
