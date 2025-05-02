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
    
    @FXML
    private CheckBox useAStarCheckBox;
    
    @FXML
    private CheckBox orderedAttractionsCheckBox;
    
    @FXML
    private Button compareAlgorithmsButton;
    
    @FXML
    private Button calculateRouteButton;
    
    @FXML
    private Button addAttractionButton;
    
    @FXML
    private Button removeAttractionButton;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label citySelectionLabel;
    
    @FXML
    private Label searchCityLabel;
    
    @FXML
    private Label startCityLabel;
    
    @FXML
    private Label endCityLabel;
    
    @FXML
    private Label attractionSelectionLabel;
    
    @FXML
    private Label searchAttractionLabel;
    
    @FXML
    private Label availableAttractionsLabel;
    
    @FXML
    private Label selectedAttractionsLabel;
    
    @FXML
    private Label algorithmOptionsLabel;
    
    @FXML
    private Label resultsLabel;
    
    @FXML
    private Label routeMapLabel;
    
    @FXML
    private Button languageSwitchButton;
    
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
        
        // 设置默认选项
        orderedAttractionsCheckBox.setSelected(true);  // 默认按顺序访问
        useAStarCheckBox.setSelected(false);  // 默认使用Dijkstra算法
        
        // 设置搜索监听器
        searchCityField.textProperty().addListener((obs, oldText, newText) -> {
            filterCities(newText);
        });
        
        searchAttractionField.textProperty().addListener((obs, oldText, newText) -> {
            filterAttractions(newText);
        });
        
        // 添加语言切换按钮
        if (languageSwitchButton != null) {
            languageSwitchButton.setText(LanguageManager.getText("languageSwitch"));
            languageSwitchButton.setOnAction(e -> {
                LanguageManager.toggleLanguage();
                updateUILanguage();
            });
        }
        
        // 初始化UI文本
        updateUILanguage();
    }
    
    /**
     * 更新UI上的文本为当前语言
     */
    private void updateUILanguage() {
        if (titleLabel != null) {
            titleLabel.setText(LanguageManager.getText("appTitle"));
        }
        if (citySelectionLabel != null) {
            citySelectionLabel.setText(LanguageManager.getText("citySelection"));
        }
        if (searchCityLabel != null) {
            searchCityLabel.setText(LanguageManager.getText("searchCity"));
        }
        if (startCityLabel != null) {
            startCityLabel.setText(LanguageManager.getText("startCity"));
        }
        if (endCityLabel != null) {
            endCityLabel.setText(LanguageManager.getText("endCity"));
        }
        if (attractionSelectionLabel != null) {
            attractionSelectionLabel.setText(LanguageManager.getText("attractionSelection"));
        }
        if (searchAttractionLabel != null) {
            searchAttractionLabel.setText(LanguageManager.getText("searchAttraction"));
        }
        if (availableAttractionsLabel != null) {
            availableAttractionsLabel.setText(LanguageManager.getText("availableAttractions"));
        }
        if (selectedAttractionsLabel != null) {
            selectedAttractionsLabel.setText(LanguageManager.getText("selectedAttractions"));
        }
        if (algorithmOptionsLabel != null) {
            algorithmOptionsLabel.setText(LanguageManager.getText("algorithmOptions"));
        }
        if (resultsLabel != null) {
            resultsLabel.setText(LanguageManager.getText("results"));
        }
        if (routeMapLabel != null) {
            routeMapLabel.setText(LanguageManager.getText("routeMap"));
        }
        if (useAStarCheckBox != null) {
            useAStarCheckBox.setText(LanguageManager.getText("useAStar"));
        }
        if (orderedAttractionsCheckBox != null) {
            orderedAttractionsCheckBox.setText(LanguageManager.getText("useOrderedAttractions"));
        }
        if (calculateRouteButton != null) {
            calculateRouteButton.setText(LanguageManager.getText("calculateRoute"));
        }
        if (compareAlgorithmsButton != null) {
            compareAlgorithmsButton.setText(LanguageManager.getText("compareAlgorithms"));
        }
        if (addAttractionButton != null) {
            addAttractionButton.setText(LanguageManager.getText("add"));
        }
        if (removeAttractionButton != null) {
            removeAttractionButton.setText(LanguageManager.getText("remove"));
        }
        if (languageSwitchButton != null) {
            languageSwitchButton.setText(LanguageManager.getText("languageSwitch"));
        }
        
        // 更新提示文本
        searchCityField.setPromptText(LanguageManager.getText("cityPrompt"));
        searchAttractionField.setPromptText(LanguageManager.getText("attractionPrompt"));
        startCityComboBox.setPromptText(LanguageManager.getText("selectStartCity"));
        endCityComboBox.setPromptText(LanguageManager.getText("selectEndCity"));
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
            showAlert(LanguageManager.getText("inputError"), LanguageManager.getText("selectCities"));
            return;
        }
        
        // 获取选中的景点
        List<String> selectedAttractions = new ArrayList<>(selectedAttractionsListView.getItems());
        
        try {
            // 获取算法选项
            boolean useAstar = useAStarCheckBox.isSelected();
            boolean orderedAttractions = orderedAttractionsCheckBox.isSelected();
            
            // 计算路线
            List<String> route = routePlanner.route(startingCity, endingCity, selectedAttractions, 
                                                 useAstar, orderedAttractions);
            int totalDistance = routePlanner.calculateTotalDistance(route);
            
            // 显示结果
            StringBuilder result = new StringBuilder();
            result.append(LanguageManager.getText("from")).append(" ").append(startingCity)
                  .append(" ").append(LanguageManager.getText("to")).append(" ").append(endingCity).append("\n");
            result.append(LanguageManager.getText("algorithm")).append(" ")
                  .append(useAstar ? "A*" : "Dijkstra").append("\n");
            result.append(LanguageManager.getText("attractionVisit")).append(" ")
                  .append(orderedAttractions ? LanguageManager.getText("ordered") : LanguageManager.getText("optimized")).append("\n");
            result.append(LanguageManager.getText("totalDistance")).append(" ")
                  .append(totalDistance).append(" ").append(LanguageManager.getText("kilometers")).append("\n\n");
            
            result.append(LanguageManager.getText("route")).append("\n");
            for (int i = 0; i < route.size(); i++) {
                String city = route.get(i);
                result.append(i + 1).append(". ").append(city);
                
                String attraction = roadNetwork.getAttractionInCity(city);
                if (attraction != null && selectedAttractions.contains(attraction)) {
                    result.append(" [").append(LanguageManager.getText("attraction")).append(" ")
                          .append(attraction).append("]");
                }
                
                result.append("\n");
            }
            
            resultTextArea.setText(result.toString());
            
            // 在地图上显示路线
            mapView.setRoute(route);
            
        } catch (Exception e) {
            showAlert(LanguageManager.getText("routeError"), 
                    LanguageManager.getText("routeErrorDesc") + " " + e.getMessage());
        }
    }
    
    /**
     * 比较两种算法
     */
    @FXML
    private void compareAlgorithms() {
        String startingCity = startCityComboBox.getValue();
        String endingCity = endCityComboBox.getValue();
        
        if (startingCity == null || endingCity == null) {
            showAlert(LanguageManager.getText("inputError"), LanguageManager.getText("selectCities"));
            return;
        }
        
        // 获取选中的景点
        List<String> selectedAttractions = new ArrayList<>(selectedAttractionsListView.getItems());
        boolean orderedAttractions = orderedAttractionsCheckBox.isSelected();
        
        try {
            // 使用Dijkstra算法
            long startTime = System.nanoTime();
            List<String> routeDijkstra = routePlanner.route(startingCity, endingCity, selectedAttractions, 
                                                        false, orderedAttractions);
            int distanceDijkstra = routePlanner.calculateTotalDistance(routeDijkstra);
            long endTimeDijkstra = System.nanoTime();
            double timeDijkstra = (endTimeDijkstra - startTime) / 1_000_000.0; // 转换为毫秒
            
            // 使用A*算法
            startTime = System.nanoTime();
            List<String> routeAStar = routePlanner.route(startingCity, endingCity, selectedAttractions, 
                                                     true, orderedAttractions);
            int distanceAStar = routePlanner.calculateTotalDistance(routeAStar);
            long endTimeAStar = System.nanoTime();
            double timeAStar = (endTimeAStar - startTime) / 1_000_000.0; // 转换为毫秒
            
            // 显示结果
            StringBuilder result = new StringBuilder();
            result.append(LanguageManager.getText("performanceComparison")).append("\n\n");
            result.append(LanguageManager.getText("from")).append(" ").append(startingCity)
                  .append(" ").append(LanguageManager.getText("to")).append(" ").append(endingCity).append("\n");
            result.append(LanguageManager.getText("attractionVisit")).append(" ")
                  .append(orderedAttractions ? LanguageManager.getText("ordered") : LanguageManager.getText("optimized")).append("\n\n");
            
            result.append(LanguageManager.getText("dijkstraTime")).append(" ")
                  .append(String.format("%.2f", timeDijkstra)).append(" ")
                  .append(LanguageManager.getText("milliseconds")).append("\n");
            result.append(LanguageManager.getText("dijkstraDistance")).append(" ")
                  .append(distanceDijkstra).append(" ")
                  .append(LanguageManager.getText("kilometers")).append("\n\n");
            
            result.append(LanguageManager.getText("astarTime")).append(" ")
                  .append(String.format("%.2f", timeAStar)).append(" ")
                  .append(LanguageManager.getText("milliseconds")).append("\n");
            result.append(LanguageManager.getText("astarDistance")).append(" ")
                  .append(distanceAStar).append(" ")
                  .append(LanguageManager.getText("kilometers")).append("\n\n");
            
            // 显示加速比
            double speedup = timeDijkstra / timeAStar;
            result.append("A* ").append(String.format("%.2f", speedup))
                  .append("x ").append(speedup > 1 ? "faster" : "slower").append("\n");
            
            resultTextArea.setText(result.toString());
            
        } catch (Exception e) {
            showAlert(LanguageManager.getText("routeError"), 
                    LanguageManager.getText("routeErrorDesc") + " " + e.getMessage());
        }
    }
    
    /**
     * 显示警告框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 