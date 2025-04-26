import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 路线地图视图
 * 简单模拟展示路线的地图组件
 */
public class RouteMapView extends Pane {
    
    private Canvas canvas;
    private Map<String, Point> cityLocations;
    private Random random = new Random();
    private RoadNetwork roadNetwork;
    
    public RouteMapView(RoadNetwork roadNetwork, int width, int height) {
        this.roadNetwork = roadNetwork;
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        
        canvas = new Canvas(width, height);
        getChildren().add(canvas);
        
        // 当面板大小改变时调整Canvas大小
        widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            redraw();
        });
        
        heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            redraw();
        });
        
        // 初始化城市位置（实际应用中应使用真实坐标）
        initCityLocations();
    }
    
    // 添加一个方法来设置路线
    public void setRoute(List<String> route) {
        if (route == null || route.size() < 2) return;
        
        String startCity = route.get(0);
        String endCity = route.get(route.size() - 1);
        List<String> waypoints = route.subList(1, route.size() - 1);
        
        showRoute(startCity, endCity, waypoints);
    }
    
    /**
     * 初始化城市位置
     */
    private void initCityLocations() {
        cityLocations = new HashMap<>();
        // 模拟城市位置（实际应用中应使用真实坐标）
        cityLocations.put("北京", new Point(400, 100));
        cityLocations.put("上海", new Point(450, 250));
        cityLocations.put("广州", new Point(350, 450));
        cityLocations.put("深圳", new Point(370, 470));
        cityLocations.put("成都", new Point(200, 300));
        cityLocations.put("杭州", new Point(420, 280));
        cityLocations.put("武汉", new Point(320, 280));
        cityLocations.put("西安", new Point(250, 200));
        cityLocations.put("南京", new Point(400, 230));
        cityLocations.put("重庆", new Point(220, 330));
        
        // 添加从RoadNetwork中获取的城市
        if (roadNetwork != null) {
            int width = (int) canvas.getWidth() - 50;
            int height = (int) canvas.getHeight() - 50;
            
            for (City city : roadNetwork.getAllCities()) {
                String cityName = city.getFullName();
                if (!cityLocations.containsKey(cityName)) {
                    int x = 25 + random.nextInt(width);
                    int y = 25 + random.nextInt(height);
                    cityLocations.put(cityName, new Point(x, y));
                }
            }
        }
    }
    
    /**
     * 显示路线
     * @param startCity 起点城市
     * @param endCity 终点城市
     * @param waypoints 途经点
     */
    public void showRoute(String startCity, String endCity, List<String> waypoints) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearCanvas(gc);
        
        // 绘制所有城市点
        for (Map.Entry<String, Point> entry : cityLocations.entrySet()) {
            String city = entry.getKey();
            Point point = entry.getValue();
            
            // 设置颜色
            if (city.equals(startCity)) {
                gc.setFill(Color.GREEN);
            } else if (city.equals(endCity)) {
                gc.setFill(Color.RED);
            } else if (waypoints.contains(city)) {
                gc.setFill(Color.BLUE);
            } else {
                gc.setFill(Color.GRAY);
            }
            
            // 绘制城市点
            gc.fillOval(point.x - 5, point.y - 5, 10, 10);
            
            // 绘制城市名称
            gc.setFill(Color.BLACK);
            gc.fillText(city, point.x + 10, point.y);
        }
        
        // 绘制路线
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        
        // 如果有起点和终点，并且它们存在于我们的地图上
        if (cityLocations.containsKey(startCity) && cityLocations.containsKey(endCity)) {
            Point start = cityLocations.get(startCity);
            Point current = start;
            
            // 绘制到每个途经点的路线
            for (String waypoint : waypoints) {
                if (cityLocations.containsKey(waypoint)) {
                    Point next = cityLocations.get(waypoint);
                    drawRouteLine(gc, current, next);
                    current = next;
                }
            }
            
            // 从最后一个途经点（或起点）到终点的路线
            Point end = cityLocations.get(endCity);
            drawRouteLine(gc, current, end);
            
            // 标记起点和终点
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setFill(Color.GREEN);
            gc.fillText("起点: " + startCity, 20, 20);
            gc.setFill(Color.RED);
            gc.fillText("终点: " + endCity, 20, 40);
        }
    }
    
    /**
     * 绘制两点间的路线
     */
    private void drawRouteLine(GraphicsContext gc, Point from, Point to) {
        // 基本直线
        gc.strokeLine(from.x, from.y, to.x, to.y);
        
        // 添加方向箭头
        double angle = Math.atan2(to.y - from.y, to.x - from.x);
        double arrowLength = 10;
        double arrowWidth = 5;
        
        double midX = (from.x + to.x) / 2;
        double midY = (from.y + to.y) / 2;
        
        double x1 = midX - arrowLength * Math.cos(angle - Math.PI/6);
        double y1 = midY - arrowLength * Math.sin(angle - Math.PI/6);
        double x2 = midX;
        double y2 = midY;
        double x3 = midX - arrowLength * Math.cos(angle + Math.PI/6);
        double y3 = midY - arrowLength * Math.sin(angle + Math.PI/6);
        
        gc.strokeLine(x1, y1, x2, y2);
        gc.strokeLine(x3, y3, x2, y2);
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
        // 只是清除，不绘制路线
        clearCanvas(canvas.getGraphicsContext2D());
    }
    
    /**
     * 表示点坐标的内部类
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