package com.eveningoutpost.dexdrip.tidepool;

// jamorham

import com.eveningoutpost.dexdrip.UtilityModels.Pref;

import okhttp3.Credentials;

import static com.eveningoutpost.dexdrip.utils.validation.StringTools.isEmpty;

public class MAuthRequest extends BaseMessage {

    public static String getAuthRequestHeader() {

        final String username = Pref.getString("tidepool_username", null);
        final String password = Pref.getString("tidepool_password", null);

        if (isEmpty(username) || isEmpty(password)) return null;
        return Credentials.basic(username.trim(), password);
    }
}



