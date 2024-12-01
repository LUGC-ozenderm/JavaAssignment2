package com.example.javaassignment2.Controller;

import com.example.javaassignment2.Model.Movie;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.*;

public class DetailsController {

    @FXML
    private ImageView posterImageView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label genreLabel;

    @FXML
    private Label starringLabel;

    @FXML
    private Label lengthLabel;

    @FXML
    private Label ratingLabel;

    @FXML
    private Text descriptionText;

    // The movie object holding all movie details
    private Movie movie;

    // Sets the movie object to work with
    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    // Loads movie details from the API
    public void loadMovieDetails() {
        String apiKey = "bd9816b3";
        String url = "http://www.omdbapi.com/?apikey=" + apiKey + "&i=" + movie.getId() + "&plot=full";

        // Creates an HTTP client
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        // Builds the HTTP request
        HttpRequest requestDetails = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        // Sends the request asynchronously
        client.sendAsync(requestDetails, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int statusCode = response.statusCode();
                    if (statusCode == 200) {
                        // Parses the movie details if response is OK
                        parseMovieDetails(response.body());
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

    // Parses the movie details from the API response
    private void parseMovieDetails(String responseBody) {
        Gson gson = new Gson();
        try {
            // Converts the response string to a JSON object
            JsonObject data = gson.fromJson(responseBody, JsonObject.class);

            // Checks if the response is successful
            if (data.has("Response") && data.get("Response").getAsString().equals("True")) {
                // Extracts movie details from the JSON data
                String posterUrl = data.has("Poster") ? data.get("Poster").getAsString() : "";
                String description = data.has("Plot") ? data.get("Plot").getAsString() : "No description available";
                String rating = data.has("imdbRating") ? data.get("imdbRating").getAsString() : "N/A";
                String runtime = data.has("Runtime") ? data.get("Runtime").getAsString() : "N/A";
                String genre = data.has("Genre") ? data.get("Genre").getAsString() : "N/A";
                String starring = data.has("Actors") ? data.get("Actors").getAsString() : "N/A";

                // Sets the details in the movie object
                movie.setPosterUrl(posterUrl);
                movie.setDescription(description);
                movie.setLength(runtime);
                movie.setRating(rating);
                movie.setGenre(genre);
                movie.setStarring(starring);

                // Updates the user interface with the new details
                updateUI();
            } else {
                // Shows an error message if the response is unsuccessful
                String errorMessage = data.has("Error") ? data.get("Error").getAsString() : "Unknown error.";
                System.err.println("API Error: " + errorMessage);
                showAlert("API Error", errorMessage);
            }
        } catch (JsonSyntaxException e) {
            // Handles parsing errors
            e.printStackTrace();
            System.err.println("Failed to parse API response. The response might not be valid JSON.");
        }
    }

    // Updates the user interface with movie details
    private void updateUI() {
        Platform.runLater(() -> {
            // Sets the poster image if available
            if (movie.getPosterUrl() != null && !movie.getPosterUrl().equals("N/A")) {
                posterImageView.setImage(new Image(movie.getPosterUrl()));
            }
            // Sets the text for labels and description
            titleLabel.setText(movie.getTitle() + " (" + movie.getYear() + ")");
            genreLabel.setText("Genre: " + movie.getGenre());
            starringLabel.setText("Starring: " + movie.getStarring());
            lengthLabel.setText("Length: " + movie.getLength());
            ratingLabel.setText("Rating: " + movie.getRating());
            descriptionText.setText(movie.getDescription());
        });
    }

    // Handles error responses from the API
    private void handleErrorResponse(int statusCode, String responseBody) {
        System.err.println("Error: Received HTTP status code " + statusCode);
        System.err.println("API Response: " + responseBody);
        Platform.runLater(() -> {
            // Shows an alert with the error message
            showAlert("API Error", "Error " + statusCode + ": Unable to fetch movie details.");
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

    // Handles the action when the "Return" button is clicked
    @FXML
    private void handleReturnButton() {
        try {
            // Loads the search scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaassignment2/Search.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Movie Finder");
            URL iconUrl = getClass().getResource("/MovieIcon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new Image(iconUrl.toExternalForm()));
            }
            stage.show();
            // Closes the current details window
            Stage currentStage = (Stage) descriptionText.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
