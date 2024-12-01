package com.example.javaassignment2.Controller;

import com.example.javaassignment2.Model.Movie;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class SearchController {

    @FXML
    private ListView<Movie> movieListView;

    // Observable list to hold movie data
    private ObservableList<Movie> movies;

    // Initializes the controller after the root element has been processed
    public void initialize() {
        movies = FXCollections.observableArrayList();
        // Fetches movies from the API
        fetchMovies();
        movieListView.setItems(movies);
        // Sets a custom cell factory to display images and text
        movieListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Movie> call(ListView<Movie> param) {
                return new ListCell<>() {
                    private ImageView imageView = new ImageView();

                    @Override
                    protected void updateItem(Movie movie, boolean empty) {
                        super.updateItem(movie, empty);
                        if (empty || movie == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Sets the poster image if available
                            if (movie.getPosterUrl() != null && !movie.getPosterUrl().equals("N/A")) {
                                imageView.setImage(new Image(movie.getPosterUrl(), 50, 75, true, true));
                            } else {
                                imageView.setImage(null);
                            }
                            // Sets the movie title and year
                            setText(movie.getTitle() + " (" + movie.getYear() + ")");
                            setGraphic(imageView);
                        }
                    }
                };
            }
        });
        // Adds a listener for double-click events on the list items
        movieListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                // Opens the details view for the selected movie
                Movie selectedMovie = movieListView.getSelectionModel().getSelectedItem();
                if (selectedMovie != null) {
                    openDetails(selectedMovie);
                }
            }
        });
    }

    // Fetches movies from the API using HTTP requests
    private void fetchMovies() {
        String apiKey = "bd9816b3";
        String searchKeyword = URLEncoder.encode("movie", StandardCharsets.UTF_8);
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        // Fetches movies from multiple pages to get about 30 movies
        for (int page = 1; page <= 3; page++) {
            String url = "http://www.omdbapi.com/?apikey=" + apiKey + "&s=" + searchKeyword + "&type=movie&page=" + page;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int statusCode = response.statusCode();
                        if (statusCode == 200) {
                            // Parses the movies if response is OK
                            parseMovies(response.body());
                        } else {
                            // Handles error if response is not OK
                            handleErrorResponse(statusCode, response.body());
                        }
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        }
    }

    // Parses the list of movies from the API response
    private void parseMovies(String responseBody) {
        Gson gson = new Gson();
        try {
            // Converts the response string to a JSON object
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

            // Checks if the response is successful
            if (jsonObject.has("Response") && jsonObject.get("Response").getAsString().equals("True")) {
                // Gets the array of movies
                JsonArray searchArray = jsonObject.getAsJsonArray("Search");
                if (searchArray == null || searchArray.size() == 0) {
                    System.err.println("No movies found in API response.");
                    return;
                }

                List<Movie> movieList = new ArrayList<>();
                // Extracts movie details from each JSON object
                for (JsonElement element : searchArray) {
                    JsonObject movieJson = element.getAsJsonObject();
                    Movie movie = new Movie();
                    movie.setId(movieJson.get("imdbID").getAsString());
                    movie.setTitle(movieJson.get("Title").getAsString());
                    movie.setYear(movieJson.get("Year").getAsString());
                    movie.setPosterUrl(movieJson.get("Poster").getAsString());
                    movieList.add(movie);
                }

                // Updates the movies list on the JavaFX Application Thread
                Platform.runLater(() -> movies.addAll(movieList));
            } else {
                // Shows an error message if the response is unsuccessful
                String errorMessage = jsonObject.has("Error") ? jsonObject.get("Error").getAsString() : "Unknown error.";
                System.err.println("API Error: " + errorMessage);
                showAlert("API Error", errorMessage);
            }
        } catch (JsonSyntaxException e) {
            // Handles parsing errors
            e.printStackTrace();
            System.err.println("Failed to parse API response. The response might not be valid JSON.");
        }
    }

    // Opens the details view for the selected movie
    private void openDetails(Movie movie) {
        try {
            // Loads the details scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaassignment2/Details.fxml"));
            Parent root = loader.load();
            DetailsController controller = loader.getController();
            controller.setMovie(movie);
            controller.loadMovieDetails();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(movie.getTitle());
            URL iconUrl = getClass().getResource("/MovieIcon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new Image(iconUrl.toExternalForm()));
            }
            stage.show();
            // Closes the current search window
            Stage currentStage = (Stage) movieListView.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handles error responses from the API
    private void handleErrorResponse(int statusCode, String responseBody) {
        System.err.println("Error: Received HTTP status code " + statusCode);
        System.err.println("API Response: " + responseBody);
        Platform.runLater(() -> {
            // Shows an alert with the error message
            showAlert("API Error", "Error " + statusCode + ": Unable to fetch movies.");
        });
    }

    // Shows an alert dialog with a given title and message
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
