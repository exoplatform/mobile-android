package org.exoplatform.controller.profile;

import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.utils.URLAnalyzer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class UserProfile extends RestProfile{

	private static final long serialVersionUID = 1L;
	public static String USER_ID = "userId";
	private static String ID = "id";
	private static String REMOTE_ID = "remoteId";
	private static String EMAIL = "email";
	private static String FULL_NAME = "fullName";
	private static String AVATAR_URL = "avatarURL";
	private static String PRO_AVATAR_URL = "avatarUrl";
	private static String PROFILE = "profile";
	private static String SKYPE = "skype";
	private static String PHONE = "phone";
	private static String POSITION = "position";
	//private static String PROFILE_URL = "profileUrl";
	private static String RELATIONSHIP = "relationshipType";
	private static String LAST_ACTIVITY = "activityTitle";

	private String jobTitle;
	private String email;
	private String lastActivity ;
	private String connectionStt ;
	private String skype;
	private String phone;

	public UserProfile(String...strings){

	}
	public UserProfile(JSONObject obj){
		if(obj.get(ID) != null)setIdentityId(obj.get(ID).toString());
		if(obj.get(REMOTE_ID) != null)setRemoteName(obj.get(REMOTE_ID).toString());
		if(obj.get(PROFILE) != null) {
			JSONObject profileObj = (JSONObject)JSONValue.parse(obj.get(PROFILE).toString());
			if (profileObj != null) {
				if(profileObj.get(FULL_NAME) != null)setFullName(profileObj.get(FULL_NAME).toString());
				if(profileObj.get(PRO_AVATAR_URL) != null)setAvatarUrl(URLAnalyzer.encodeUrl(profileObj.get(PRO_AVATAR_URL).toString()));
				if(profileObj.get(EMAIL) != null) setEmail(profileObj.get(EMAIL).toString());
			}
		}  
		if(obj.get(FULL_NAME) != null)setFullName(obj.get(FULL_NAME).toString());
		if(obj.get(AVATAR_URL) != null)setAvatarUrl(obj.get(AVATAR_URL).toString());
		if(obj.get(POSITION) != null)setJobTitle(obj.get(POSITION).toString());
		if(obj.get(EMAIL) != null) setEmail(obj.get(EMAIL).toString());
		if(obj.get(LAST_ACTIVITY) != null)setLastActivity(obj.get(LAST_ACTIVITY).toString());
		if(obj.get(RELATIONSHIP) != null)setConnectionStt(obj.get(RELATIONSHIP).toString()); 
		if(obj.get(SKYPE) != null)setSkype(obj.get(SKYPE).toString());
		if(obj.get(PHONE) != null)setPhone(obj.get(PHONE).toString()); 
	}

	public void setLastActivity(String string) {
		 lastActivity = string;

	}
	public void setRemoteName(String string) {


	}
	public String getLastActivity() {
		return lastActivity;
	}
	public String getJobTitle() {
		return jobTitle;
	}
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	public String getConnectionStt() {
		return connectionStt;
	}
	public void setConnectionStt(String connectionStt) {
		this.connectionStt = connectionStt;
	}
	public String getSkype() {
		return skype;
	}
	public String getPhone() {
		return phone;
	}
	public void setSkype(String skype) {
		this.skype = skype;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
