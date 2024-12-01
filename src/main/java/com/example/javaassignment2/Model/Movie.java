package com.example.javaassignment2.Model;

public class Movie {

    private String id;

    private String title;

    private String year;

    private String posterUrl;

    private String genre;

    private String description;

    private String starring;

    private String length;

    private String rating;

    public Movie() {
    }

    // Getter and setter methods for each variable

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public void setTitle(String title) { this.title = title; }

    public String getTitle() { return title; }

    public void setYear(String year) { this.year = year; }

    public String getYear() { return year; }

    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getPosterUrl() { return posterUrl; }

    public void setGenre(String genre) { this.genre = genre; }

    public String getGenre() { return genre; }

    public void setDescription(String description) { this.description = description; }

    public String getDescription() { return description; }

    public void setStarring(String starring) { this.starring = starring; }

    public String getStarring() { return starring; }

    public void setLength(String length) { this.length = length; }

    public String getLength() { return length; }

    public void setRating(String rating) { this.rating = rating; }

    public String getRating() { return rating; }
}
