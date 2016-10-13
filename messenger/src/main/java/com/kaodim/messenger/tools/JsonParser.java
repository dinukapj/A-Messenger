package com.kaodim.messenger.tools;

import android.os.Parcel;
import android.os.Parcelable;

import com.kaodim.messenger.models.ConversationModel;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Kanskiy on 11/10/2016.
 */

public abstract class JsonParser implements Parcelable {
    public abstract ArrayList<ConversationModel> fromJsonToConversationArray(String json);

    public static final Parcelable.Creator<JsonParser> CREATOR = new Parcelable.Creator<JsonParser>() {
        public JsonParser createFromParcel(Parcel in) {
            return new JsonParser(in) {
                @Override
                public ArrayList<ConversationModel> fromJsonToConversationArray(String json) {
                    return null;
                }
            };
        }
        public JsonParser[] newArray(int size) {
            return new JsonParser[size];
        }
    };


    public JsonParser(){

    }

    private JsonParser(Parcel in) {
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
    @Override
    public int describeContents() {
        return 0;
    }
}
