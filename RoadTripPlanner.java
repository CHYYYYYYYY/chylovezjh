import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 道路旅行规划的主应用程序类
 */
public class RoadTripPlanner {
    private static RoadNetwork roadNetwork;
    private static RoutePlanner routePlanner;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        // 初始化道路网络和路线规划器
        roadNetwork = new RoadNetwork();
        roadNetwork.loadData("CW3_Data_Files/roads.csv", "CW3_Data_Files/attractions.csv");
        routePlanner = new RoutePlanner(roadNetwork);
        
        // 先运行测试用例
        runTestCases();
        
        // 继续交互模式
        scanner = new Scanner(System.in);
        boolean keepRunning = true;
        
        System.out.println("\n=== 交互式道路旅行规划器 ===\n");
        
        // 打印出所有可用的城市
        System.out.println("数据集中的可用城市:");
        printAvailableCities();
        
        // 打印出所有可用的景点
        System.out.println("\n数据集中的可用景点:");
        printAvailableAttractions();
        System.out.println();
        
        while (keepRunning) {
            try {
                // 获取用户输入
                System.out.print("输入起始城市 (例如, New York NY): ");
                String startingCity = scanner.nextLine().trim();
                
                // 如果用户输入"list cities"，显示城市列表
                if (startingCity.equalsIgnoreCase("list cities")) {
                    printAvailableCities();
                    continue;
                }
                
                // 如果用户输入"list attractions"，显示景点列表
                if (startingCity.equalsIgnoreCase("list attractions")) {
                    printAvailableAttractions();
                    continue;
                }
                
                if (!roadNetwork.cityExists(startingCity)) {
                    System.out.println("错误: 找不到起始城市: " + startingCity);
                    System.out.println("提示: 输入 'list cities' 查看所有可用城市");
                    continue;
                }
                
                System.out.print("输入目的地城市 (例如, Philadelphia PA): ");
                String endingCity = scanner.nextLine().trim();
                
                if (!roadNetwork.cityExists(endingCity)) {
                    System.out.println("错误: 找不到目的地城市: " + endingCity);
                    System.out.println("提示: 输入 'list cities' 查看所有可用城市");
                    continue;
                }
                
                System.out.print("输入景点 (以逗号分隔, 例如, Hollywood Sign): ");
                String attractionsInput = scanner.nextLine().trim();
                
                // 如果用户输入"list attractions"，显示景点列表
                if (attractionsInput.equalsIgnoreCase("list attractions")) {
                    printAvailableAttractions();
                    continue;
                }
                
                List<String> attractions = new ArrayList<>();
                if (!attractionsInput.isEmpty()) {
                    String[] parts = attractionsInput.split(",");
                    for (String part : parts) {
                        String attraction = part.trim();
                        if (!roadNetwork.attractionExists(attraction)) {
                            System.out.println("警告: 找不到景点: " + attraction);
                            System.out.println("提示: 输入 'list attractions' 查看所有可用景点");
                            // 不添加不存在的景点
                        } else {
                            attractions.add(attraction);
                        }
                    }
                }
                
                // 计算路线
                List<String> route = routePlanner.route(startingCity, endingCity, attractions);
                int totalDistance = routePlanner.calculateTotalDistance(route);
                
                // 显示结果
                System.out.println("\n结果:");
                System.out.println("起点: " + startingCity);
                System.out.println("终点: " + endingCity);
                System.out.println("景点: " + attractions);
                System.out.println("最佳路线: " + route);
                System.out.println("总距离: " + totalDistance + " 英里");
                
                // 询问用户是否继续
                System.out.print("\n您想规划另一次旅行吗? (是/否): ");
                String answer = scanner.nextLine().trim().toLowerCase();
                keepRunning = answer.equals("是") || answer.equals("y") || answer.equals("yes");
                
            } catch (IllegalArgumentException e) {
                System.out.println("错误: " + e.getMessage());
                System.out.println("请尝试使用有效的输入。");
            } catch (RuntimeException e) {
                System.out.println("错误: " + e.getMessage());
                System.out.println("请尝试使用不同的起点、终点或景点组合。");
            }
        }
        
        scanner.close();
        System.out.println("感谢使用道路旅行规划器！");
    }
    
    /**
     * 打印所有可用的城市
     */
    private static void printAvailableCities() {
        Collection<City> cities = roadNetwork.getAllCities();
        List<String> cityNames = cities.stream()
                .map(City::getFullName)
                .sorted()
                .collect(Collectors.toList());
        
        int count = 0;
        for (String cityName : cityNames) {
            System.out.print(cityName);
            count++;
            if (count % 4 == 0) {
                System.out.println();
            } else {
                System.out.print("\t");
            }
        }
        if (count % 4 != 0) {
            System.out.println();
        }
    }
    
    /**
     * 打印所有可用的景点
     */
    private static void printAvailableAttractions() {
        Collection<Attraction> attractions = roadNetwork.getAllAttractions();
        List<String> attractionNames = attractions.stream()
                .map(attraction -> attraction.getName() + " (" + attraction.getLocation() + ")")
                .sorted()
                .collect(Collectors.toList());
        
        for (String attractionName : attractionNames) {
            System.out.println(attractionName);
        }
    }
    
    /**
     * Task B的测试方法，运行所需的测试用例
     */
    public static void runTestCases() {
        System.out.println("=== 运行Task B的测试用例 ===");
        
        // 测试用例1: 休斯顿到费城，不经过任何景点
        List<String> route1 = routePlanner.route("Houston TX", "Philadelphia PA", new ArrayList<>());
        int distance1 = routePlanner.calculateTotalDistance(route1);
        System.out.println("测试用例1: 休斯顿到费城，不经过任何景点");
        System.out.println("路线: " + route1);
        System.out.println("总距离: " + distance1 + " 英里");
        System.out.println();
        
        // 测试用例2: 费城到圣安东尼奥，经过好莱坞标志
        List<String> route2 = routePlanner.route("Philadelphia PA", "San Antonio TX", 
                                                Arrays.asList("Hollywood Sign"));
        int distance2 = routePlanner.calculateTotalDistance(route2);
        System.out.println("测试用例2: 费城到圣安东尼奥，经过好莱坞标志");
        System.out.println("路线: " + route2);
        System.out.println("总距离: " + distance2 + " 英里");
        System.out.println();
        
        // 测试用例3: 圣何塞到凤凰城，经过自由钟和千禧公园
        List<String> route3 = routePlanner.route("San Jose CA", "Phoenix AZ", 
                                               Arrays.asList("Liberty Bell", "Millennium Park"));
        int distance3 = routePlanner.calculateTotalDistance(route3);
        System.out.println("测试用例3: 圣何塞到凤凰城，经过自由钟和千禧公园");
        System.out.println("路线: " + route3);
        System.out.println("总距离: " + distance3 + " 英里");
        System.out.println();
    }
} 