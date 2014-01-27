package org.exoplatform.controller.social;

//import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import org.exoplatform.poc.tabletversion.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.social.client.core.service.QueryParamsImpl;
import org.exoplatform.ui.social.AllUpdatesFragment;
import org.exoplatform.ui.social.MyConnectionsFragment;
import org.exoplatform.ui.social.MySpacesFragment;
import org.exoplatform.ui.social.MyStatusFragment;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.SocialDetailsWarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.View;


public class SocialDetailLoadTask extends AsyncTask<Boolean, Void, Integer> {
  private RestActivity                 selectedRestActivity;

  private LinkedList<SocialLikeInfo>   mLikeLinkedList    = new LinkedList<SocialLikeInfo>();

  private ArrayList<SocialCommentInfo> mSocialCommentList = new ArrayList<SocialCommentInfo>();

  //private LoaderActionBarItem          loaderItem;

  private Context                      mContext;

  private String                       youText;

  private String                       okString;

  private String                       titleString;

  private String                       detailsErrorStr;

  private SocialDetailController       detailController;

  private String                       activityType;

  private SocialActivityInfo           mActivityInfo;

  private boolean                      hasContent        = false;

  private boolean                      isLikeAction      = false;

  private int                          mActivityPosition;

  private AsyncTaskListener            mListener;


  private static final String TAG = "eXo____SocialDetailLoadTask____";


  public SocialDetailLoadTask(Context context, int pos) {
    Log.i(TAG, "SocialDetailLoadTask - pos: " + pos);
    mContext          = context;
    mActivityPosition = pos;
    changeLanguage();
  }


  @Override
  public Integer doInBackground(Boolean... params) {
    isLikeAction = params[0];

    try {
      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance().activityService;

      String activityId = SocialDetailHelper.getInstance().getActivityId();
      QueryParams queryParams = new QueryParamsImpl();
      queryParams.append(QueryParams.NUMBER_OF_LIKES_PARAM.setValue(ExoConstants.NUMBER_OF_LIKES_PARAM))
          .append(QueryParams.NUMBER_OF_COMMENTS_PARAM.setValue(ExoConstants.NUMBER_OF_COMMENTS_PARAM))
          .append(QueryParams.POSTER_IDENTITY_PARAM.setValue(true));

      selectedRestActivity = activityService.get(activityId, queryParams);
      SocialDetailHelper.getInstance().setLiked(false);

      mActivityInfo = new SocialActivityInfo();
      RestProfile restProfile = selectedRestActivity.getPosterIdentity().getProfile();
      mActivityInfo.setActivityId(selectedRestActivity.getId());
      mActivityInfo.setImageUrl(restProfile.getAvatarUrl());
      mActivityInfo.setUserName(restProfile.getFullName());
      mActivityInfo.setTitle(selectedRestActivity.getTitle());
      mActivityInfo.setBody(selectedRestActivity.getBody());
      mActivityInfo.setPostedTime(selectedRestActivity.getPostedTime());
      mActivityInfo.setLikeNumber(selectedRestActivity.getTotalNumberOfLikes());
      mActivityInfo.setCommentNumber(selectedRestActivity.getTotalNumberOfComments());
      activityType = selectedRestActivity.getType();
      mActivityInfo.setType(activityType);
      mActivityInfo.restActivityStream = selectedRestActivity.getActivityStream();
      mActivityInfo.templateParams = selectedRestActivity.getTemplateParams();

      List<RestIdentity> likeList = selectedRestActivity.getAvailableLikes();
      List<RestComment> commentList = selectedRestActivity.getAvailableComments();
      if (likeList != null) {
        for (RestIdentity like : likeList) {
          RestProfile likeProfile = like.getProfile();
          SocialLikeInfo socialLike = new SocialLikeInfo();
          socialLike.likedImageUrl = likeProfile.getAvatarUrl();
          String identity = like.getId();
          if (identity.equalsIgnoreCase(SocialServiceHelper.getInstance().userIdentity)) {
            socialLike.setLikeName(youText);
            mLikeLinkedList.addFirst(socialLike);
            SocialDetailHelper.getInstance().setLiked(true);
          } else {
            String likeName = like.getProfile().getFullName();
            socialLike.setLikeName(likeName);
            mLikeLinkedList.add(socialLike);
          }

        }
      }

      if (commentList != null) {
        for (RestComment comment : commentList) {
          SocialCommentInfo socialComment = new SocialCommentInfo();
          RestIdentity restId = comment.getPosterIdentity();

          RestProfile profile = restId.getProfile();
          socialComment.setCommentId(restId.getId());
          socialComment.setCommentName(profile.getFullName());
          socialComment.setImageUrl(profile.getAvatarUrl());
          socialComment.setCommentTitle(comment.getText());
          socialComment.setPostedTime(comment.getPostedTime());

          mSocialCommentList.add(socialComment);
        }
      }

      return 1;
    } catch (SocialClientLibException e) {
      Log.d(TAG, "SocialClientLibException: " + e.getLocalizedMessage());
      return 0;
    } catch (RuntimeException re) {
      Log.d(TAG, "RuntimeException: " + re.getLocalizedMessage());
      return -1;
    }
  }

  @Override
  public void onPostExecute(Integer result) {

    if (mListener != null) mListener.onLoadingActivityFinished(result, mActivityInfo, mSocialCommentList, mLikeLinkedList);

    /**
    if (result == 1 && isLikeAction && SocialTabsActivity.instance != null) {

      int tabId = SocialTabsActivity.instance.mPager.getCurrentItem();
      switch (tabId) {

        case SocialTabsActivity.ALL_UPDATES:
          AllUpdatesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, mActivityPosition);
          break;

        case SocialTabsActivity.MY_CONNECTIONS:
          MyConnectionsFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, mActivityPosition);
          break;

        case SocialTabsActivity.MY_SPACES:
          MySpacesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, mActivityPosition);
          break;

        case SocialTabsActivity.MY_STATUS:
          MyStatusFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, mActivityPosition);
          break;
      }
    }
     **/

  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    youText = resource.getString(R.string.You);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    detailsErrorStr = resource.getString(R.string.DetailsNotAvaiable);

  }


  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  public interface AsyncTaskListener {

    void onLoadingActivityFinished(int result, SocialActivityInfo activityInfo,
                                   ArrayList<SocialCommentInfo> socialCommentList,
                                   LinkedList<SocialLikeInfo> likeLinkedList);
  }
}
