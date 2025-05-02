package org.example.cw;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class RoadTripPlannerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RoadTripPlannerApplication.class.getResource("road-trip-planner-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("Road Trip Planner | 道路旅行规划器");
        
        // 设置最小窗口大小，避免UI元素拥挤
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
} 