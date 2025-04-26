/**
 * 道路旅行规划器应用程序启动类
 * 用于启动JavaFX界面
 */
public class RoadTripPlannerApp extends javafx.application.Application {
    
    /**
     * 主入口点
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(javafx.stage.Stage primaryStage) {
        // 创建并启动GUI界面
        RoadTripPlannerGUI gui = new RoadTripPlannerGUI();
        gui.start(primaryStage);
    }
} 