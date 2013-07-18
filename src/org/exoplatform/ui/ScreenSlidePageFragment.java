package org.exoplatform.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.exoplatform.R;
import org.exoplatform.utils.AssetUtils;

import java.util.Date;

public class ScreenSlidePageFragment extends Fragment {

  public  static final int      NUM_PAGES      = 5;

  private static final String   TAG            = "eXoScreenSlidePageFragment";

  public  static final int[]    SLIDER_IMGS    = {
      R.drawable.activity_stream_slide,
      R.drawable.activity_details_slide,
      R.drawable.apps_slide,
      R.drawable.documents_slide  };

  public  static final int[]    SLIDER_IMGS_PORTRAIT  = {
      R.drawable.activity_stream_portrait_slide,
      R.drawable.activity_details_portrait_slide,
      R.drawable.apps_portrait_slide,
      R.drawable.documents_portrait_slide  };

  public  static final int[]    SLIDER_IMGS_LANDSCAPE = {
      R.drawable.activity_stream_landscape_slide,
      R.drawable.activity_details_landscape_slide,
      R.drawable.apps_landscape_slide,
      R.drawable.documents_landscape_slide  };

  /** default slider image use normal images */
  public  static       int[]    sSliderImgs    = SLIDER_IMGS;

  public  static final Bitmap[] SLIDER_BITMAPS = new Bitmap[NUM_PAGES - 1];

  private static final int[]    SLIDER_DESC    = {
      R.string.SliderDesc1,
      R.string.SliderDesc2,
      R.string.SliderDesc3,
      R.string.SliderDesc4   };

  /**
   * The argument key for the page number this fragment represents.
   */
  public static final String ARG_PAGE = "page";

  /**
   * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
   */
  private int mPageNumber;

  /**
   * Factory method for this fragment class. Constructs a new fragment for the given page number.
   */
  public static ScreenSlidePageFragment create(int pageNumber) {
    Log.i(TAG, "ScreenSlidePageFragment create - page number: " + pageNumber);
    ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_PAGE, pageNumber);
    fragment.setArguments(args);
    return fragment;
  }

  public ScreenSlidePageFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate: " + mPageNumber);
    mPageNumber = getArguments().getInt(ARG_PAGE);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.i(TAG, "onCreateView");

    long start = new Date().getTime();
    Log.i(TAG, "start time: " + start);

    ViewGroup mRootView;

    if (mPageNumber != 0) {
      // Inflate the layout containing a title and body text - need to optimize this one
      mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page, container, false);

      long middle = new Date().getTime();
      Log.i(TAG, "time inflate layout: " + (middle - start));  // 235 ms

      //Bitmap img = BitmapFactory.decodeResource(getResources(), SLIDER_IMGS[mPageNumber]);
      //Log.i(TAG, "time  image: " + (new Date().getTime() - middle));

      ((ImageView) mRootView.findViewById(R.id.slider_img)).setImageBitmap(SLIDER_BITMAPS[mPageNumber - 1]);
      Log.i(TAG, "time set image: " + (new Date().getTime() - middle));

      ((TextView) mRootView.findViewById(R.id.slider_txt_description)).setText(
          getResources().getText(SLIDER_DESC[mPageNumber - 1]));

      long end = new Date().getTime();
      Log.i(TAG, "end time: " + end);
      Log.i(TAG, "diff: " + (end - start));   // 236ms
    }
    else {
      // first slide
      mRootView = (ViewGroup) inflater.inflate(R.layout.exo_intranet_slide, container, false);
    }

    AssetUtils.setTypeFace(AssetUtils.getCustomTypeface(AssetUtils.ROBOTO_BLACK), mRootView);
    return mRootView;
  }

  /**
   * Returns the page number represented by this fragment object.
   */
  public int getPageNumber() {
    return mPageNumber;
  }
}
