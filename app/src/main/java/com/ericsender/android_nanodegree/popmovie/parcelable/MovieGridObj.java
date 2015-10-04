package com.ericsender.android_nanodegree.popmovie.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Eric K. Sender on 8/4/2015.
 */
public class MovieGridObj implements Parcelable, Serializable {
    private static final long serialVersionUID = 1L;
    public String title;
    public Boolean adult;
    public String backdrop_path;
    public List<Double> genre_ids;
    public Long id;
    public String original_language;
    public String original_title;
    public String overview;
    public String release_date;
    public String poster_path;
    public Double popularity;
    public Boolean video;
    public Double vote_average;
    public Integer vote_count;

    public MovieGridObj() {
    }

    protected MovieGridObj(Parcel in) {
        title = in.readString();
        adult = Boolean.valueOf(in.readByte() == (byte) 1);
        backdrop_path = in.readString();
        genre_ids = in.readArrayList(ClassLoader.getSystemClassLoader());
        id = (long) in.readDouble();
        original_language = in.readString();
        original_title = in.readString();
        overview = in.readString();
        release_date = in.readString();
        poster_path = in.readString();
        popularity = in.readDouble();
        video = Boolean.valueOf(in.readByte() == (byte) 1);
        vote_average = in.readDouble();
        vote_count = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeByte((byte) (adult ? 1 : 0));
        dest.writeString(backdrop_path);
        dest.writeList(genre_ids);
        dest.writeDouble(id);
        dest.writeString(original_language);
        dest.writeString(original_title);
        dest.writeString(overview);
        dest.writeString(release_date);
        dest.writeString(poster_path);
        dest.writeDouble(popularity);
        dest.writeByte((byte) (video ? 1 : 0));
        dest.writeDouble(vote_average);
        dest.writeInt(vote_count);
    }

    public static final Creator<MovieGridObj> CREATOR = new Creator<MovieGridObj>() {
        @Override
        public MovieGridObj createFromParcel(Parcel in) {
            return new MovieGridObj(in);
        }

        @Override
        public MovieGridObj[] newArray(int size) {
            return new MovieGridObj[size];
        }
    };

    @Override
    public String toString() {
        return "MovieGridObj{" + "trailer_title='" + title + "'";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // This equals() and hashCode() ensures using a Set of MovieGridObj's will all be unique!
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovieGridObj that = (MovieGridObj) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (adult != null ? !adult.equals(that.adult) : that.adult != null) return false;
        if (backdrop_path != null ? !backdrop_path.equals(that.backdrop_path) : that.backdrop_path != null)
            return false;
        if (genre_ids != null ? !genre_ids.equals(that.genre_ids) : that.genre_ids != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (original_language != null ? !original_language.equals(that.original_language) : that.original_language != null)
            return false;
        if (original_title != null ? !original_title.equals(that.original_title) : that.original_title != null)
            return false;
        if (overview != null ? !overview.equals(that.overview) : that.overview != null)
            return false;
        if (release_date != null ? !release_date.equals(that.release_date) : that.release_date != null)
            return false;
        if (poster_path != null ? !poster_path.equals(that.poster_path) : that.poster_path != null)
            return false;
        if (popularity != null ? !popularity.equals(that.popularity) : that.popularity != null)
            return false;
        if (video != null ? !video.equals(that.video) : that.video != null) return false;
        if (vote_average != null ? !vote_average.equals(that.vote_average) : that.vote_average != null)
            return false;
        return !(vote_count != null ? !vote_count.equals(that.vote_count) : that.vote_count != null);

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (adult != null ? adult.hashCode() : 0);
        result = 31 * result + (backdrop_path != null ? backdrop_path.hashCode() : 0);
        result = 31 * result + (genre_ids != null ? genre_ids.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (original_language != null ? original_language.hashCode() : 0);
        result = 31 * result + (original_title != null ? original_title.hashCode() : 0);
        result = 31 * result + (overview != null ? overview.hashCode() : 0);
        result = 31 * result + (release_date != null ? release_date.hashCode() : 0);
        result = 31 * result + (poster_path != null ? poster_path.hashCode() : 0);
        result = 31 * result + (popularity != null ? popularity.hashCode() : 0);
        result = 31 * result + (video != null ? video.hashCode() : 0);
        result = 31 * result + (vote_average != null ? vote_average.hashCode() : 0);
        result = 31 * result + (vote_count != null ? vote_count.hashCode() : 0);
        return result;
    }
}
