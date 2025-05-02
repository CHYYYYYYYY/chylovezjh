package org.example.cw;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;

/**
 * 路线地图视图
 * 简单模拟展示路线的地图组件
 */
public class RouteMapView extends Pane {
    
    private Canvas canvas;
    private Map<String, Point> cityLocations;
    private Random random = new Random();
    private RoadNetwork roadNetwork;
    private String startCity;
    private String endCity;
    private List<String> waypoints;
    private Button languageToggleButton; // 语言切换按钮
    
    // 定义更美观的颜色方案
    private static final Color BACKGROUND_COLOR = Color.web("#f8f9fa");
    private static final Color CITY_COLOR = Color.web("#3498db");
    private static final Color START_CITY_COLOR = Color.web("#2ecc71");
    private static final Color END_CITY_COLOR = Color.web("#e74c3c");
    private static final Color WAYPOINT_COLOR = Color.web("#f39c12");
    private static final Color ROAD_COLOR = Color.web("#95a5a6");
    private static final Color ROUTE_COLOR = Color.web("#9b59b6");
    private static final Color TEXT_COLOR = Color.web("#2c3e50");
    
    public RouteMapView(RoadNetwork roadNetwork, int width, int height) {
        this.roadNetwork = roadNetwork;
        setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, new CornerRadii(8), Insets.EMPTY)));
        
        // 添加边框效果
        setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        
        canvas = new Canvas(width, height);
        getChildren().add(canvas);
        
        // 创建语言切换按钮并设置样式
        languageToggleButton = new Button(LanguageManager.getText("languageSwitch"));
        languageToggleButton.setLayoutX(width - 150);
        languageToggleButton.setLayoutY(10);
        languageToggleButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                     "-fx-background-radius: 4; -fx-font-weight: bold;");
        languageToggleButton.setOnAction(e -> {
            LanguageManager.toggleLanguage();
            languageToggleButton.setText(LanguageManager.getText("languageSwitch"));
            redraw();
        });
        getChildren().add(languageToggleButton);
        
        // 当面板大小改变时调整Canvas大小
        widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            // 调整语言按钮位置
            languageToggleButton.setLayoutX(newVal.doubleValue() - 150);
            // 重新初始化城市位置以适应新尺寸
            if (roadNetwork != null) {
                initCityLocations();
                displayAllCities(); // 显示所有城市
                if (startCity != null && endCity != null) {
                    showRoute(startCity, endCity, waypoints);
                }
            }
        });
        
        heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            // 重新初始化城市位置以适应新尺寸
            if (roadNetwork != null) {
                initCityLocations();
                displayAllCities(); // 显示所有城市
                if (startCity != null && endCity != null) {
                    showRoute(startCity, endCity, waypoints);
                }
            }
        });
        
        // 初始化城市位置
        initCityLocations();
        // 显示所有城市
        displayAllCities();
    }
    
    // 添加一个方法来设置路线
    public void setRoute(List<String> route) {
        if (route == null || route.size() < 2) return;
        
        this.startCity = route.get(0);
        this.endCity = route.get(route.size() - 1);
        this.waypoints = route.subList(1, route.size() - 1);
        
        showRoute(startCity, endCity, waypoints);
    }
    
    /**
     * 初始化城市位置，使用城市间实际距离计算位置
     */
    private void initCityLocations() {
        cityLocations = new HashMap<>();
        
        // 获取画布尺寸
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        
        if (roadNetwork == null || width == 0 || height == 0) {
            return;
        }
        
        // 计算边距
        double marginX = width * 0.1;
        double marginY = height * 0.1;
        double availableWidth = width - 2 * marginX;
        double availableHeight = height - 2 * marginY;
        
        // 获取所有城市
        Collection<City> cities = roadNetwork.getAllCities();
        if (cities.isEmpty()) {
            return;
        }
        
        // 使用MDS (Multidimensional Scaling)方法简化版本
        // 第一步：选择一个基准城市作为起点(0,0)
        City[] cityArray = cities.toArray(new City[0]);
        City baseCity = cityArray[0]; // 使用第一个城市作为基准点
        
        // 创建城市距离矩阵
        int n = cityArray.length;
        double[][] distances = new double[n][n];
        
        // 初始化距离矩阵
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    // 查找两城市间的直接距离
                    Integer directDistance = cityArray[i].getConnections().get(cityArray[j]);
                    if (directDistance != null) {
                        distances[i][j] = directDistance;
                    } else {
                        // 如果没有直接连接，给一个较大值
                        distances[i][j] = Double.MAX_VALUE;
                    }
                }
            }
        }
        
        // 使用Floyd-Warshall算法计算所有城市对之间的最短距离
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (distances[i][k] != Double.MAX_VALUE && 
                        distances[k][j] != Double.MAX_VALUE &&
                        distances[i][k] + distances[k][j] < distances[i][j]) {
                        distances[i][j] = distances[i][k] + distances[k][j];
                    }
                }
            }
        }
        
        // 使用简化版本的力导向算法来确定位置
        double[][] positions = new double[n][2];
        
        // 初始化位置（随机分配初始位置）
        for (int i = 0; i < n; i++) {
            positions[i][0] = random.nextDouble() * availableWidth;
            positions[i][1] = random.nextDouble() * availableHeight;
        }
        
        // 执行力导向算法的简化版本
        double k = availableWidth / Math.sqrt(n * 2); // 理想距离因子
        int iterations = 50; // 迭代次数
        
        for (int iter = 0; iter < iterations; iter++) {
            // 为每个城市计算力
            double[][] forces = new double[n][2];
            
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        // 计算两城市间的实际距离
                        double dx = positions[j][0] - positions[i][0];
                        double dy = positions[j][1] - positions[i][1];
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        
                        if (distance > 0) {
                            // 计算理想距离（将实际距离映射到画布尺寸）
                            double idealDistance = (distances[i][j] == Double.MAX_VALUE) ? 
                                k * 2 : k * Math.min(distances[i][j], availableWidth / 2) / 100;
                            
                            // 计算力（吸引或排斥）
                            double force = (distance - idealDistance) / distance;
                            
                            // 应用力
                            forces[i][0] += dx * force;
                            forces[i][1] += dy * force;
                        }
                    }
                }
            }
            
            // 更新位置
            double damping = 0.9 / (iter + 1); // 减少振荡
            for (int i = 0; i < n; i++) {
                positions[i][0] += forces[i][0] * damping;
                positions[i][1] += forces[i][1] * damping;
            }
        }
        
        // 将位置归一化到可用区域内
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        
        for (int i = 0; i < n; i++) {
            minX = Math.min(minX, positions[i][0]);
            minY = Math.min(minY, positions[i][1]);
            maxX = Math.max(maxX, positions[i][0]);
            maxY = Math.max(maxY, positions[i][1]);
        }
        
        // 调整位置到画布边界内
        for (int i = 0; i < n; i++) {
            double normalizedX, normalizedY;
            
            if (maxX > minX) {
                normalizedX = marginX + ((positions[i][0] - minX) / (maxX - minX)) * availableWidth;
            } else {
                normalizedX = marginX + availableWidth / 2;
            }
            
            if (maxY > minY) {
                normalizedY = marginY + ((positions[i][1] - minY) / (maxY - minY)) * availableHeight;
            } else {
                normalizedY = marginY + availableHeight / 2;
            }
            
            cityLocations.put(cityArray[i].getFullName(), new Point(normalizedX, normalizedY));
        }
    }
    
    /**
     * 显示所有城市（不包含路线）
     */
    public void displayAllCities() {
        if (cityLocations == null || cityLocations.isEmpty()) {
            return;
        }
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearCanvas(gc);
        
        // 先绘制所有道路
        for (City city : roadNetwork.getAllCities()) {
            Point fromPoint = cityLocations.get(city.getFullName());
            if (fromPoint == null) continue;
            
            // 获取此城市的所有连接
            Map<City, Integer> connections = city.getConnections();
            for (Map.Entry<City, Integer> connection : connections.entrySet()) {
                Point toPoint = cityLocations.get(connection.getKey().getFullName());
                if (toPoint == null) continue;
                
                // 绘制道路线条
                gc.setStroke(ROAD_COLOR);
                gc.setLineWidth(1.0);
                gc.strokeLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
            }
        }
        
        // 然后绘制所有城市
        double cityRadius = 5.0;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        for (Map.Entry<String, Point> entry : cityLocations.entrySet()) {
            String cityName = entry.getKey();
            Point point = entry.getValue();
            
            // 绘制城市圆点
            gc.setFill(CITY_COLOR);
            gc.fillOval(point.x - cityRadius, point.y - cityRadius, 
                        cityRadius * 2, cityRadius * 2);
            
            // 绘制城市名称
            gc.setFill(TEXT_COLOR);
            gc.fillText(cityName, point.x + cityRadius + 2, point.y + 4);
        }
    }
    
    /**
     * 显示路线
     * @param startCity 起点城市
     * @param endCity 终点城市
     * @param waypoints 途经点
     */
    public void showRoute(String startCity, String endCity, List<String> waypoints) {
        this.startCity = startCity;
        this.endCity = endCity;
        this.waypoints = waypoints;
        
        if (startCity == null || endCity == null) {
            return;
        }
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearCanvas(gc);
        
        // 先绘制背景路网
        displayAllCities();
        
        // 获取起点和终点位置
        Point startPoint = cityLocations.get(startCity);
        Point endPoint = cityLocations.get(endCity);
        
        if (startPoint == null || endPoint == null) {
            return;
        }
        
        // 创建一个路线点列表
        List<Point> routePoints = new ArrayList<>();
        routePoints.add(startPoint);
        
        // 添加所有途经点
        if (waypoints != null) {
            for (String waypoint : waypoints) {
                Point waypointPoint = cityLocations.get(waypoint);
                if (waypointPoint != null) {
                    routePoints.add(waypointPoint);
                }
            }
        }
        
        // 添加终点
        routePoints.add(endPoint);
        
        // 绘制路线连线
        for (int i = 0; i < routePoints.size() - 1; i++) {
            Point from = routePoints.get(i);
            Point to = routePoints.get(i + 1);
            drawRouteLine(gc, from, to);
        }
        
        // 突出显示起点和终点
        double specialCityRadius = 8.0;
        
        // 起点标记
        gc.setFill(START_CITY_COLOR);
        gc.fillOval(startPoint.x - specialCityRadius, startPoint.y - specialCityRadius, 
                    specialCityRadius * 2, specialCityRadius * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(startPoint.x - specialCityRadius, startPoint.y - specialCityRadius, 
                     specialCityRadius * 2, specialCityRadius * 2);
        
        // 终点标记
        gc.setFill(END_CITY_COLOR);
        gc.fillOval(endPoint.x - specialCityRadius, endPoint.y - specialCityRadius, 
                    specialCityRadius * 2, specialCityRadius * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(endPoint.x - specialCityRadius, endPoint.y - specialCityRadius, 
                     specialCityRadius * 2, specialCityRadius * 2);
        
        // 途经点标记
        if (waypoints != null) {
            double waypointRadius = 6.0;
            for (String waypoint : waypoints) {
                Point waypointPoint = cityLocations.get(waypoint);
                if (waypointPoint != null) {
                    gc.setFill(WAYPOINT_COLOR);
                    gc.fillOval(waypointPoint.x - waypointRadius, waypointPoint.y - waypointRadius, 
                                waypointRadius * 2, waypointRadius * 2);
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(1.0);
                    gc.strokeOval(waypointPoint.x - waypointRadius, waypointPoint.y - waypointRadius, 
                                 waypointRadius * 2, waypointRadius * 2);
                }
            }
        }
        
        // 添加图例
        drawLegend(gc);
    }
    
    /**
     * 绘制路线连线
     */
    private void drawRouteLine(GraphicsContext gc, Point from, Point to) {
        // 设置线条样式
        gc.setStroke(ROUTE_COLOR);
        gc.setLineWidth(3.0);
        
        // 绘制路线
        gc.strokeLine(from.x, from.y, to.x, to.y);
        
        // 绘制箭头
        double arrowLength = 15.0;
        double arrowWidth = 6.0;
        
        // 计算线条方向角度
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double angle = Math.atan2(dy, dx);
        
        // 计算箭头端点
        double midX = (from.x + to.x) / 2;
        double midY = (from.y + to.y) / 2;
        
        // 计算箭头两个端点
        double x1 = midX - arrowLength * Math.cos(angle - Math.PI/6);
        double y1 = midY - arrowLength * Math.sin(angle - Math.PI/6);
        double x2 = midX - arrowLength * Math.cos(angle + Math.PI/6);
        double y2 = midY - arrowLength * Math.sin(angle + Math.PI/6);
        
        // 绘制箭头
        gc.setFill(ROUTE_COLOR);
        double[] xPoints = {midX, x1, x2};
        double[] yPoints = {midY, y1, y2};
        gc.fillPolygon(xPoints, yPoints, 3);
    }
    
    /**
     * 绘制图例
     */
    private void drawLegend(GraphicsContext gc) {
        double baseX = 20;
        double baseY = 20;
        double lineHeight = 25;
        double iconSize = 10;
        double textOffset = 20;
        
        // 设置字体
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(TEXT_COLOR);
        gc.fillText(LanguageManager.getText("legendTitle"), baseX, baseY);
        
        gc.setFont(Font.font("Arial", 12));
        
        // 起点图例
        baseY += lineHeight;
        gc.setFill(START_CITY_COLOR);
        gc.fillOval(baseX, baseY - iconSize/2, iconSize, iconSize);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(baseX, baseY - iconSize/2, iconSize, iconSize);
        gc.setFill(TEXT_COLOR);
        gc.fillText(LanguageManager.getText("startCity"), baseX + textOffset, baseY + 5);
        
        // 终点图例
        baseY += lineHeight;
        gc.setFill(END_CITY_COLOR);
        gc.fillOval(baseX, baseY - iconSize/2, iconSize, iconSize);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(baseX, baseY - iconSize/2, iconSize, iconSize);
        gc.setFill(TEXT_COLOR);
        gc.fillText(LanguageManager.getText("endCity"), baseX + textOffset, baseY + 5);
        
        // 途经点图例
        baseY += lineHeight;
        gc.setFill(WAYPOINT_COLOR);
        gc.fillOval(baseX, baseY - iconSize/2, iconSize, iconSize);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(baseX, baseY - iconSize/2, iconSize, iconSize);
        gc.setFill(TEXT_COLOR);
        gc.fillText(LanguageManager.getText("waypoint"), baseX + textOffset, baseY + 5);
        
        // 普通城市图例
        baseY += lineHeight;
        gc.setFill(CITY_COLOR);
        gc.fillOval(baseX, baseY - iconSize/2, iconSize, iconSize);
        gc.setFill(TEXT_COLOR);
        gc.fillText(LanguageManager.getText("city"), baseX + textOffset, baseY + 5);
        
        // 路线图例
        baseY += lineHeight;
        gc.setStroke(ROUTE_COLOR);
        gc.setLineWidth(3.0);
        gc.strokeLine(baseX, baseY, baseX + iconSize, baseY);
        gc.setFill(TEXT_COLOR);
        gc.fillText(LanguageManager.getText("route"), baseX + textOffset, baseY + 5);
    }
    
    /**
     * 清除画布
     */
    private void clearCanvas(GraphicsContext gc) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    /**
     * 重绘地图
     */
    private void redraw() {
        if (startCity != null && endCity != null) {
            showRoute(startCity, endCity, waypoints);
        } else {
            displayAllCities();
        }
    }
    
    /**
     * 简单的点类，表示坐标
     */
    private static class Point {
        double x;
        double y;
        
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
} 