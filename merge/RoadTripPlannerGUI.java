import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 道路旅行规划的JavaFX图形界面应用
 */
public class RoadTripPlannerGUI extends Application {
    
    private RoadNetwork roadNetwork;
    private RoutePlanner routePlanner;
    private RouteMapView mapView;
    
    // UI组件
    private ComboBox<String> startCityComboBox;
    private ComboBox<String> endCityComboBox;
    private ListView<String> attractionsListView;
    private ListView<String> selectedAttractionsListView;
    private TextArea resultTextArea;
    private Button calculateButton;
    private Button addAttractionButton;
    private Button removeAttractionButton;
    private TextField searchCityField;
    private TextField searchAttractionField;
    
    /**
     * 应用程序入口点
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        // 初始化数据
        initialize();
        
        // 创建界面
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // 顶部标题
        Label titleLabel = new Label("道路旅行规划器");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(titleBox);
        
        // 主内容区域
        SplitPane splitPane = new SplitPane();
        
        // 左侧：输入面板
        VBox inputPanel = createInputPanel();
        
        // 右侧：结果和地图面板
        VBox resultPanel = createResultPanel();
        
        // 设置分割比例
        splitPane.getItems().addAll(inputPanel, resultPanel);
        splitPane.setDividerPositions(0.4);
        
        root.setCenter(splitPane);
        
        // 设置场景
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setTitle("道路旅行规划器");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // 加载初始数据
        loadCities();
        loadAttractions();
    }
    
    /**
     * 初始化数据和业务逻辑
     */
    private void initialize() {
        roadNetwork = new RoadNetwork();
        roadNetwork.loadData("CW3_Data_Files/roads.csv", "CW3_Data_Files/attractions.csv");
        routePlanner = new RoutePlanner(roadNetwork);
    }
    
    /**
     * 创建输入面板，包含城市和景点选择
     */
    private VBox createInputPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        // 城市选择部分
        VBox citySelectionBox = createCitySelectionPanel();
        
        // 景点选择部分
        VBox attractionSelectionBox = createAttractionSelectionPanel();
        
        // 计算按钮
        calculateButton = new Button("计算最佳路线");
        calculateButton.setPrefWidth(150);
        calculateButton.setOnAction(e -> calculateRoute());
        HBox buttonBox = new HBox(calculateButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        panel.getChildren().addAll(citySelectionBox, attractionSelectionBox, buttonBox);
        
        return panel;
    }
    
    /**
     * 创建城市选择面板
     */
    private VBox createCitySelectionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        Label panelTitle = new Label("城市选择");
        panelTitle.setStyle("-fx-font-weight: bold;");
        
        // 搜索城市
        Label searchLabel = new Label("搜索城市:");
        searchCityField = new TextField();
        searchCityField.setPromptText("输入城市名或州缩写");
        searchCityField.textProperty().addListener((obs, oldText, newText) -> {
            filterCities(newText);
        });
        
        // 起始城市
        Label startCityLabel = new Label("起始城市:");
        startCityComboBox = new ComboBox<>();
        startCityComboBox.setPromptText("选择起始城市");
        startCityComboBox.setEditable(true);
        startCityComboBox.setPrefWidth(300);
        
        // 目的地城市
        Label endCityLabel = new Label("目的地城市:");
        endCityComboBox = new ComboBox<>();
        endCityComboBox.setPromptText("选择目的地城市");
        endCityComboBox.setEditable(true);
        endCityComboBox.setPrefWidth(300);
        
        HBox searchBox = new HBox(10, searchLabel, searchCityField);
        HBox startCityBox = new HBox(10, startCityLabel, startCityComboBox);
        HBox endCityBox = new HBox(10, endCityLabel, endCityComboBox);
        
        panel.getChildren().addAll(panelTitle, searchBox, startCityBox, endCityBox);
        return panel;
    }
    
    /**
     * 创建景点选择面板
     */
    private VBox createAttractionSelectionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        Label panelTitle = new Label("景点选择");
        panelTitle.setStyle("-fx-font-weight: bold;");
        
        // 搜索景点
        Label searchLabel = new Label("搜索景点:");
        searchAttractionField = new TextField();
        searchAttractionField.setPromptText("输入景点名称");
        searchAttractionField.textProperty().addListener((obs, oldText, newText) -> {
            filterAttractions(newText);
        });
        
        // 可选景点列表
        Label availableAttractionsLabel = new Label("可选景点:");
        attractionsListView = new ListView<>();
        attractionsListView.setPrefHeight(150);
        
        // 已选景点列表
        Label selectedAttractionsLabel = new Label("已选景点:");
        selectedAttractionsListView = new ListView<>();
        selectedAttractionsListView.setPrefHeight(100);
        
        // 添加/移除按钮
        addAttractionButton = new Button("添加 >");
        addAttractionButton.setOnAction(e -> addSelectedAttraction());
        
        removeAttractionButton = new Button("< 移除");
        removeAttractionButton.setOnAction(e -> removeSelectedAttraction());
        
        VBox buttonsBox = new VBox(10, addAttractionButton, removeAttractionButton);
        buttonsBox.setPadding(new Insets(50, 0, 0, 0));
        
        // 布局
        HBox searchBox = new HBox(10, searchLabel, searchAttractionField);
        
        HBox attractionsSelectionBox = new HBox(10);
        VBox availableAttractionsBox = new VBox(5, availableAttractionsLabel, attractionsListView);
        availableAttractionsBox.setPrefWidth(300);
        VBox selectedAttractionsBox = new VBox(5, selectedAttractionsLabel, selectedAttractionsListView);
        selectedAttractionsBox.setPrefWidth(300);
        
        attractionsSelectionBox.getChildren().addAll(availableAttractionsBox, buttonsBox, selectedAttractionsBox);
        
        panel.getChildren().addAll(panelTitle, searchBox, attractionsSelectionBox);
        return panel;
    }
    
    /**
     * 创建结果和地图面板
     */
    private VBox createResultPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        // 地图视图
        Label mapTitle = new Label("路线地图");
        mapTitle.setStyle("-fx-font-weight: bold;");
        
        mapView = new RouteMapView(roadNetwork, 600, 400);
        
        // 文本结果区域
        Label resultTitle = new Label("路线详情");
        resultTitle.setStyle("-fx-font-weight: bold;");
        
        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setPrefHeight(200);
        
        // 图例说明
        TitledPane legendPane = createMapLegend();
        
        // 添加到面板
        panel.getChildren().addAll(mapTitle, mapView, resultTitle, resultTextArea, legendPane);
        VBox.setVgrow(mapView, Priority.ALWAYS);
        
        return panel;
    }
    
    /**
     * 创建地图图例说明
     */
    private TitledPane createMapLegend() {
        VBox legendContent = new VBox(5);
        legendContent.setPadding(new Insets(5));
        
        legendContent.getChildren().addAll(
            new Label("● 蓝色: 城市"),
            new Label("● 绿色外圈: 起点城市"),
            new Label("● 红色外圈: 终点城市"),
            new Label("● 绿色标记: 景点"),
            new Label("― 红色线: 路线")
        );
        
        TitledPane legendPane = new TitledPane("地图图例", legendContent);
        legendPane.setCollapsible(true);
        legendPane.setExpanded(false);
        
        return legendPane;
    }
    
    /**
     * 加载城市数据到下拉框
     */
    private void loadCities() {
        List<String> cityNames = roadNetwork.getAllCities().stream()
                .map(City::getFullName)
                .sorted()
                .collect(Collectors.toList());
        
        startCityComboBox.setItems(FXCollections.observableArrayList(cityNames));
        endCityComboBox.setItems(FXCollections.observableArrayList(cityNames));
    }
    
    /**
     * 加载景点数据到列表
     */
    private void loadAttractions() {
        List<String> attractionNames = roadNetwork.getAllAttractions().stream()
                .map(attraction -> attraction.getName() + " (" + attraction.getLocation() + ")")
                .sorted()
                .collect(Collectors.toList());
        
        attractionsListView.setItems(FXCollections.observableArrayList(attractionNames));
    }
    
    /**
     * 根据输入过滤城市
     */
    private void filterCities(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            loadCities();
            return;
        }
        
        List<String> filteredCities = roadNetwork.findCitiesByFuzzyName(filter);
        startCityComboBox.setItems(FXCollections.observableArrayList(filteredCities));
        endCityComboBox.setItems(FXCollections.observableArrayList(filteredCities));
    }
    
    /**
     * 根据输入过滤景点
     */
    private void filterAttractions(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            loadAttractions();
            return;
        }
        
        List<String> matchedAttractions = roadNetwork.findAttractionsByFuzzyName(filter);
        
        List<String> attractionFullNames = matchedAttractions.stream()
                .map(name -> {
                    Attraction attraction = roadNetwork.getAttraction(name);
                    return attraction.getName() + " (" + attraction.getLocation() + ")";
                })
                .collect(Collectors.toList());
        
        attractionsListView.setItems(FXCollections.observableArrayList(attractionFullNames));
    }
    
    /**
     * 添加所选景点到已选列表
     */
    private void addSelectedAttraction() {
        String selected = attractionsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ObservableList<String> items = selectedAttractionsListView.getItems();
            if (items == null) {
                items = FXCollections.observableArrayList();
            }
            
            if (!items.contains(selected)) {
                items.add(selected);
                selectedAttractionsListView.setItems(items);
            }
        }
    }
    
    /**
     * 从已选列表移除所选景点
     */
    private void removeSelectedAttraction() {
        String selected = selectedAttractionsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ObservableList<String> items = selectedAttractionsListView.getItems();
            items.remove(selected);
        }
    }
    
    /**
     * 计算路线
     */
    private void calculateRoute() {
        try {
            String startCity = startCityComboBox.getValue();
            String endCity = endCityComboBox.getValue();
            
            if (startCity == null || startCity.trim().isEmpty()) {
                showAlert("错误", "请选择起始城市");
                return;
            }
            
            if (endCity == null || endCity.trim().isEmpty()) {
                showAlert("错误", "请选择目的地城市");
                return;
            }
            
            // 验证城市是否有效
            if (!roadNetwork.cityExists(startCity)) {
                startCity = roadNetwork.findCityByFuzzyName(startCity);
                if (startCity == null) {
                    showAlert("错误", "无效的起始城市");
                    return;
                }
            }
            
            if (!roadNetwork.cityExists(endCity)) {
                endCity = roadNetwork.findCityByFuzzyName(endCity);
                if (endCity == null) {
                    showAlert("错误", "无效的目的地城市");
                    return;
                }
            }
            
            // 提取景点名称（去掉城市部分）
            List<String> selectedAttractions = new ArrayList<>();
            if (selectedAttractionsListView.getItems() != null) {
                for (String item : selectedAttractionsListView.getItems()) {
                    String attractionName = item.split("\\(")[0].trim();
                    selectedAttractions.add(attractionName);
                }
            }
            
            // 计算路线
            List<String> route = routePlanner.route(startCity, endCity, selectedAttractions);
            int totalDistance = routePlanner.calculateTotalDistance(route);
            
            // 更新地图视图
            mapView.setRoute(route);
            
            // 显示文本结果
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append("起点: ").append(startCity).append("\n");
            resultBuilder.append("终点: ").append(endCity).append("\n");
            resultBuilder.append("景点: ").append(selectedAttractions).append("\n\n");
            resultBuilder.append("最佳路线:\n");
            
            for (int i = 0; i < route.size(); i++) {
                resultBuilder.append(i + 1).append(". ").append(route.get(i));
                if (i < route.size() - 1) {
                    resultBuilder.append("\n  |\n  v\n");
                }
            }
            
            resultBuilder.append("\n\n总距离: ").append(totalDistance).append(" 英里");
            
            resultTextArea.setText(resultBuilder.toString());
            
        } catch (Exception e) {
            showAlert("错误", "计算路线时出错: " + e.getMessage());
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