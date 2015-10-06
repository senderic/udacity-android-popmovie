package com.ericsender.android_nanodegree.popmovie.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Eric K. Sender on 9/17/2015.
 */
public class ReviewListObj implements Parcelable, Serializable {

    public final String content;
    public final String author;
    public final String url;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewListObj that = (ReviewListObj) o;

        if (content != null ? !content.equals(that.content) : that.content != null) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        return !(url != null ? !url.equals(that.url) : that.url != null);

    }

    @Override
    public int hashCode() {
        int result = content != null ? content.hashCode() : 0;
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    public ReviewListObj(String review, String author, String url) {
        this.content = review;
        this.author = author;
        this.url = url;
    }

    protected ReviewListObj(Parcel in) {
        content = in.readString();
        author = in.readString();
        url = in.readString();
    }

    public static final Creator<ReviewListObj> CREATOR = new Creator<ReviewListObj>() {
        @Override
        public ReviewListObj createFromParcel(Parcel in) {
            return new ReviewListObj(in);
        }

        @Override
        public ReviewListObj[] newArray(int size) {
            return new ReviewListObj[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeString(author);
        dest.writeString(url);
    }
}
