package eXo.eXoPlatform.social;

import java.util.Date;

public class SocialStreamUtil {

  public static String getPostedTimeString(long postedTime) {
    // long currentTime = new Date().getTime();
    long time = (new Date().getTime() - postedTime) / 1000;
    long value;
    if (time < 60) {
      return "Less Than A Minute";
    } else {
      if (time < 120) {
        return "A Minute Ago";
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          return "About " + String.valueOf(value) + " Minutes";
        } else {
          if (time < 7200) {
            return "An Hour Ago";
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              return String.valueOf(value) + " Hours Ago";
            } else {
              if (time < 172800) {
                return "A Day Ago";
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  return String.valueOf(value) + " Days Ago";
                } else {
                  if (time < 5184000) {
                    return "A Month Ago";
                  } else {
                    value = Math.round(time / 2592000);
                    return String.valueOf(value) + " Months Ago";
                  }
                }
              }
            }
          }
        }
      }
    }
  }

}
