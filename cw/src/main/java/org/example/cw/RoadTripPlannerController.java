package org.example.cw;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 道路旅行规划应用程序的控制器类
 */
public class RoadTripPlannerController {

    private RoadNetwork roadNetwork;
    private RoutePlanner routePlanner;
    private RouteMapView mapView;

    @FXML
    private ComboBox<String> startCityComboBox;
    
    @FXML
    private ComboBox<String> endCityComboBox;
    
    @FXML
    private ListView<String> attractionsListView;
    
    @FXML
    private ListView<String> selectedAttractionsListView;
    
    @FXML
    private TextArea resultTextArea;
    
    @FXML
    private TextField searchCityField;
    
    @FXML
    private TextField searchAttractionField;
    
    @FXML
    private VBox mapContainer;
    
    /**
     * 初始化控制器
     */
    @FXML
    private void initialize() {
        // 初始化数据
        roadNetwork = new RoadNetwork();
        roadNetwork.loadData("/data/roads.csv", "/data/attractions.csv");
        routePlanner = new RoutePlanner(roadNetwork);
        
        // 初始化地图视图
        mapView = new RouteMapView(roadNetwork, 800, 600);
        mapContainer.getChildren().add(mapView);
        
        // 加载初始数据
        loadCities();
        loadAttractions();
        
        // 设置搜索监听器
        searchCityField.textProperty().addListener((obs, oldText, newText) -> {
            filterCities(newText);
        });
        
        searchAttractionField.textProperty().addListener((obs, oldText, newText) -> {
            filterAttractions(newText);
        });
    }
    
    /**
     * 加载城市列表
     */
    private void loadCities() {
        List<String> cityNames = roadNetwork.getAllCityNames();
        ObservableList<String> cities = FXCollections.observableArrayList(cityNames);
        startCityComboBox.setItems(cities);
        endCityComboBox.setItems(cities);
    }
    
    /**
     * 加载景点列表
     */
    private void loadAttractions() {
        List<String> attractionNames = roadNetwork.getAllAttractionNames();
        ObservableList<String> attractions = FXCollections.observableArrayList(attractionNames);
        attractionsListView.setItems(attractions);
    }
    
    /**
     * 根据输入过滤城市列表
     */
    private void filterCities(String filter) {
        List<String> filteredCities = roadNetwork.findCitiesByFuzzyName(filter);
        ObservableList<String> cities = FXCollections.observableArrayList(filteredCities);
        startCityComboBox.setItems(cities);
        endCityComboBox.setItems(cities);
    }
    
    /**
     * 根据输入过滤景点列表
     */
    private void filterAttractions(String filter) {
        List<String> filteredAttractions = roadNetwork.findAttractionsByFuzzyName(filter);
        
        // 排除已选景点
        List<String> selectedAttractions = selectedAttractionsListView.getItems();
        List<String> availableAttractions = filteredAttractions.stream()
                .filter(attr -> !selectedAttractions.contains(attr))
                .collect(Collectors.toList());
        
        ObservableList<String> attractions = FXCollections.observableArrayList(availableAttractions);
        attractionsListView.setItems(attractions);
    }
    
    /**
     * 添加选中的景点到已选列表
     */
    @FXML
    private void addSelectedAttraction() {
        String selectedAttraction = attractionsListView.getSelectionModel().getSelectedItem();
        if (selectedAttraction != null) {
            // 添加到已选列表
            ObservableList<String> selectedAttractions = selectedAttractionsListView.getItems();
            if (selectedAttractions == null) {
                selectedAttractions = FXCollections.observableArrayList();
                selectedAttractionsListView.setItems(selectedAttractions);
            }
            
            selectedAttractions.add(selectedAttraction);
            
            // 从可选列表中移除
            attractionsListView.getItems().remove(selectedAttraction);
        }
    }
    
    /**
     * 从已选列表中移除景点
     */
    @FXML
    private void removeSelectedAttraction() {
        String selectedAttraction = selectedAttractionsListView.getSelectionModel().getSelectedItem();
        if (selectedAttraction != null) {
            // 从已选列表中移除
            selectedAttractionsListView.getItems().remove(selectedAttraction);
            
            // 添加回可选列表
            ObservableList<String> availableAttractions = attractionsListView.getItems();
            availableAttractions.add(selectedAttraction);
            
            // 重新排序可选列表
            FXCollections.sort(availableAttractions);
        }
    }
    
    /**
     * 计算路线
     */
    @FXML
    private void calculateRoute() {
        String startingCity = startCityComboBox.getValue();
        String endingCity = endCityComboBox.getValue();
        
        if (startingCity == null || endingCity == null) {
            showAlert("输入错误", "请选择起始城市和目的地城市");
            return;
        }
        
        // 获取选中的景点
        List<String> selectedAttractions = new ArrayList<>(selectedAttractionsListView.getItems());
        
        try {
            // 计算路线
            List<String> route = routePlanner.route(startingCity, endingCity, selectedAttractions);
            int totalDistance = routePlanner.calculateTotalDistance(route);
            
            // 显示结果
            StringBuilder result = new StringBuilder();
            result.append("从 ").append(startingCity).append(" 到 ").append(endingCity).append("\n");
            result.append("总距离: ").append(totalDistance).append(" 公里\n\n");
            
            result.append("路线:\n");
            for (int i = 0; i < route.size(); i++) {
                String city = route.get(i);
                result.append(i + 1).append(". ").append(city);
                
                String attraction = roadNetwork.getAttractionInCity(city);
                if (attraction != null && selectedAttractions.contains(attraction)) {
                    result.append(" [景点: ").append(attraction).append("]");
                }
                
                result.append("\n");
            }
            
            resultTextArea.setText(result.toString());
            
            // 在地图上显示路线
            mapView.setRoute(route);
            
        } catch (Exception e) {
            showAlert("路线计算错误", "无法计算路线: " + e.getMessage());
        }
    }
    
    /**
     * 显示警告对话框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 