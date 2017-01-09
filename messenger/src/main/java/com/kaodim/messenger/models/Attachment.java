package com.kaodim.messenger.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Kanskiy on 09/01/2017.
 */

    public class Attachment implements Parcelable {
        public String id;
        public String file_id;
        public String file_filename;
        public int file_size;
        public String url;
        public Date created_at;
        public Date updated_at;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.file_id);
            dest.writeString(this.file_filename);
            dest.writeInt(this.file_size);
            dest.writeString(this.url);
            dest.writeLong(this.created_at != null ? this.created_at.getTime() : -1);
            dest.writeLong(this.updated_at != null ? this.updated_at.getTime() : -1);
        }

        public Attachment() {
        }

        protected Attachment(Parcel in) {
            this.id = in.readString();
            this.file_id = in.readString();
            this.file_filename = in.readString();
            this.file_size = in.readInt();
            this.url = in.readString();
            long tmpCreated_at = in.readLong();
            this.created_at = tmpCreated_at == -1 ? null : new Date(tmpCreated_at);
            long tmpUpdated_at = in.readLong();
            this.updated_at = tmpUpdated_at == -1 ? null : new Date(tmpUpdated_at);
        }

        public static final Parcelable.Creator<Attachment> CREATOR = new Parcelable.Creator<Attachment>() {
            @Override
            public Attachment createFromParcel(Parcel source) {
                return new Attachment(source);
            }

            @Override
            public Attachment[] newArray(int size) {
                return new Attachment[size];
            }
        };
    }
