package com.ericsender.android_nanodegree.popmovie.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Eric K. Sender on 9/15/2015.
 */
public class TrailerListObj implements Parcelable, Serializable {
    public final String youtube_key;
    public final String trailer_title;
    public final String movie_title;

    protected TrailerListObj(Parcel in) {
        youtube_key = in.readString();
        trailer_title = in.readString();
        movie_title = in.readString();
    }

    public TrailerListObj(String youtube_key, String title, String movie_title) {
        this.youtube_key = youtube_key;
        this.trailer_title = title;
        this.movie_title = movie_title;
    }

    public static final Creator<TrailerListObj> CREATOR = new Creator<TrailerListObj>() {
        @Override
        public TrailerListObj createFromParcel(Parcel in) {
            return new TrailerListObj(in);
        }

        @Override
        public TrailerListObj[] newArray(int size) {
            return new TrailerListObj[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(youtube_key);
        dest.writeString(trailer_title);
        dest.writeString(movie_title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrailerListObj that = (TrailerListObj) o;

        if (youtube_key != null ? !youtube_key.equals(that.youtube_key) : that.youtube_key != null)
            return false;
        if (trailer_title != null ? !trailer_title.equals(that.trailer_title) : that.trailer_title != null)
            return false;
        return !(movie_title != null ? !movie_title.equals(that.movie_title) : that.movie_title != null);

    }

    @Override
    public int hashCode() {
        int result = youtube_key != null ? youtube_key.hashCode() : 0;
        result = 31 * result + (trailer_title != null ? trailer_title.hashCode() : 0);
        result = 31 * result + (movie_title != null ? movie_title.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TrailerListObj{" +
                "movie_title='" + movie_title + '\'' +
                ", youtube_key='" + youtube_key + '\'' +
                ", trailer_title='" + trailer_title + '\'' +
                '}';
    }
}
