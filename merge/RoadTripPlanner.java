import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Collection;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Console;

/**
 * 道路旅行规划的主应用程序类
 */
public class RoadTripPlanner {
    private static RoadNetwork roadNetwork;
    private static RoutePlanner routePlanner;
    private static Scanner scanner;
    
    /**
     * 程序主入口
     */
    public static void main(String[] args) {
        RoadTripPlanner planner = new RoadTripPlanner();
        planner.initialize();
        planner.run();
    }
    
    /**
     * 初始化道路网络和路线规划器
     */
    private void initialize() {
        roadNetwork = new RoadNetwork();
        roadNetwork.loadData("CW3_Data_Files/roads.csv", "CW3_Data_Files/attractions.csv");
        routePlanner = new RoutePlanner(roadNetwork);
        scanner = new Scanner(System.in);
    }
    
    /**
     * 运行交互式旅行规划器
     */
    private void run() {
        // 先运行测试用例
        runTestCases();
        
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
                // 获取旅行信息
                TripData tripData = collectTripData();
                
                try {
                    // 计算路线
                    List<String> route = routePlanner.route(tripData.getStartingCity(), 
                                                            tripData.getEndingCity(), 
                                                            tripData.getAttractions());
                    int totalDistance = routePlanner.calculateTotalDistance(route);
                    
                    // 显示结果
                    displayResults(tripData, route, totalDistance);
                } catch (RuntimeException e) {
                    System.out.println("路线规划错误: " + e.getMessage());
                    System.out.println("无法计算从 " + tripData.getStartingCity() + " 到 " + tripData.getEndingCity() + " 的路线。");
                    if (!tripData.getAttractions().isEmpty()) {
                        System.out.println("请尝试减少景点数量或选择不同的景点。");
                    }
                }
                
                // 询问用户是否继续
                keepRunning = askToContinue();
                
            } catch (Exception e) {
                System.out.println("程序发生错误: " + e.getMessage());
                System.out.println("请重新开始输入。");
                
                // 清空输入缓冲区
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
            }
        }
        
        cleanup();
    }
    
    /**
     * 收集旅行数据：起点、终点和景点
     * @return 包含旅行信息的TripData对象
     */
    private TripData collectTripData() {
        String startingCity = getStartingCity();
        String endingCity = getEndingCity();
        List<String> attractions = getAttractions();
        
        return new TripData(startingCity, endingCity, attractions);
    }
    
    /**
     * 获取起始城市
     * @return 选择的起始城市名称
     */
    private String getStartingCity() {
        String startingCity = null;
        List<String> cityList = null;
        while (startingCity == null) {
            System.out.print("输入起始城市 (例如, New York NY，或 'list cities' 查看列表): ");
            String startingCityInput = scanner.nextLine().trim();
            
            // 如果用户输入"list cities"，显示城市列表
            if (startingCityInput.equalsIgnoreCase("list cities")) {
                cityList = printAvailableCities();
                continue;
            }
            
            // 如果用户输入"list attractions"，显示景点列表
            if (startingCityInput.equalsIgnoreCase("list attractions")) {
                printAvailableAttractions();
                continue;
            }
            
            // 检查是否输入的是序号
            if (cityList != null && startingCityInput.matches("\\d+")) {
                try {
                    int index = Integer.parseInt(startingCityInput) - 1;
                    if (index >= 0 && index < cityList.size()) {
                        startingCity = cityList.get(index);
                        System.out.println("已选择: " + startingCity);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    // 解析错误，继续其他匹配方式
                }
            }
            
            // 使用模糊匹配查找城市
            List<String> matchingCities = roadNetwork.findCitiesByFuzzyName(startingCityInput);
            
            if (matchingCities.isEmpty()) {
                System.out.println("错误: 找不到起始城市: " + startingCityInput);
                System.out.println("提示: 输入 'list cities' 查看所有可用城市");
                System.out.println("请重新输入起始城市");
            } else if (matchingCities.size() == 1) {
                // 只有一个匹配项
                startingCity = matchingCities.get(0);
                if (!startingCityInput.equals(startingCity)) {
                    System.out.println("提示: 已匹配到城市 '" + startingCity + "'");
                }
            } else {
                // 有多个匹配项，让用户选择
                startingCity = chooseFromMultipleCities(matchingCities);
            }
        }
        return startingCity;
    }
    
    /**
     * 获取目的地城市
     * @return 选择的目的地城市名称
     */
    private String getEndingCity() {
        String endingCity = null;
        List<String> cityList = null; // 重置城市列表
        while (endingCity == null) {
            System.out.print("输入目的地城市 (例如, Philadelphia PA，或 'list cities' 查看列表): ");
            String endingCityInput = scanner.nextLine().trim();
            
            // 如果用户输入"list cities"，显示城市列表
            if (endingCityInput.equalsIgnoreCase("list cities")) {
                cityList = printAvailableCities();
                continue;
            }
            
            // 检查是否输入的是序号
            if (cityList != null && endingCityInput.matches("\\d+")) {
                try {
                    int index = Integer.parseInt(endingCityInput) - 1;
                    if (index >= 0 && index < cityList.size()) {
                        endingCity = cityList.get(index);
                        System.out.println("已选择: " + endingCity);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    // 解析错误，继续其他匹配方式
                }
            }
            
            // 使用模糊匹配查找城市
            List<String> matchingCities = roadNetwork.findCitiesByFuzzyName(endingCityInput);
            
            if (matchingCities.isEmpty()) {
                System.out.println("错误: 找不到目的地城市: " + endingCityInput);
                System.out.println("提示: 输入 'list cities' 查看所有可用城市");
                System.out.println("请重新输入目的地城市");
            } else if (matchingCities.size() == 1) {
                // 只有一个匹配项
                endingCity = matchingCities.get(0);
                if (!endingCityInput.equals(endingCity)) {
                    System.out.println("提示: 已匹配到城市 '" + endingCity + "'");
                }
            } else {
                // 有多个匹配项，让用户选择
                endingCity = chooseFromMultipleCities(matchingCities);
            }
        }
        return endingCity;
    }
    
    /**
     * 从多个匹配的城市中选择一个
     * @param matchingCities 匹配的城市列表
     * @return 用户选择的城市，如果选择无效则返回null
     */
    private String chooseFromMultipleCities(List<String> matchingCities) {
        System.out.println("找到多个匹配的城市:");
        for (int i = 0; i < matchingCities.size(); i++) {
            System.out.println((i + 1) + ". " + matchingCities.get(i));
        }
        
        System.out.print("请选择一个城市 (输入编号): ");
        String selection = scanner.nextLine().trim();
        try {
            int index = Integer.parseInt(selection) - 1;
            if (index >= 0 && index < matchingCities.size()) {
                String city = matchingCities.get(index);
                System.out.println("已选择: " + city);
                return city;
            } else {
                System.out.println("无效的选择，请重新输入城市");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("请输入有效的数字");
            return null;
        }
    }
    
    /**
     * 获取景点列表
     * @return 选择的景点列表
     */
    private List<String> getAttractions() {
        List<String> attractions = new ArrayList<>();
        boolean validAttractions = false;
        List<String> attractionList = null;
        
        while (!validAttractions) {
            System.out.print("输入景点 (以逗号分隔, 例如, Hollywood Sign，或输入空白跳过，或 'list attractions' 查看列表): ");
            String attractionsInput = scanner.nextLine().trim();
            
            // 如果用户输入"list attractions"，显示景点列表
            if (attractionsInput.equalsIgnoreCase("list attractions")) {
                attractionList = printAvailableAttractions();
                continue;
            }
            
            // 如果输入的是序号或序号列表，且之前已经显示了景点列表
            if (attractionList != null) {
                // 检查是否输入的是单个序号
                if (attractionsInput.matches("\\d+")) {
                    try {
                        int index = Integer.parseInt(attractionsInput) - 1;
                        if (index >= 0 && index < attractionList.size()) {
                            String attraction = attractionList.get(index);
                            System.out.println("已选择: " + attraction);
                            attractions.clear();
                            attractions.add(attraction);
                            validAttractions = true;
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        // 解析错误，继续其他匹配方式
                    }
                } 
                // 检查是否输入的是逗号分隔的多个序号
                else if (attractionsInput.matches("(\\d+,?\\s*)+")) {
                    // 处理多个序号的情况
                    String[] indexStrs = attractionsInput.split(",");
                    boolean allValid = true;
                    attractions.clear();

                    for (String indexStr : indexStrs) {
                        try {
                            int index = Integer.parseInt(indexStr.trim()) - 1;
                            if (index >= 0 && index < attractionList.size()) {
                                String attraction = attractionList.get(index);
                                System.out.println("已选择: " + attraction);
                                attractions.add(attraction);
                            } else {
                                System.out.println("警告: 序号 " + (index + 1) + " 超出范围，已跳过");
                                allValid = false;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("警告: 无效的序号 '" + indexStr.trim() + "'，已跳过");
                            allValid = false;
                        }
                    }

                    if (attractions.size() > 0) {
                        if (!allValid) {
                            System.out.println("已添加所有有效的景点选择");
                        }
                        validAttractions = true;
                        continue;
                    }
                }
            }
            
            // 如果输入为空，跳过景点
            if (attractionsInput.isEmpty()) {
                validAttractions = true;
                continue;
            }
            
            boolean hasInvalidAttractions = false;
            attractions.clear();
            
            // 标准化逗号，将中文逗号替换为英文逗号
            attractionsInput = attractionsInput.replace('，', ',');
            
            try {
                String[] parts = attractionsInput.split(",");
                for (String part : parts) {
                    String attractionInput = part.trim();
                    if (attractionInput.isEmpty()) continue;
                    
                    // 使用模糊匹配查找景点
                    List<String> matchingAttractions = roadNetwork.findAttractionsByFuzzyName(attractionInput);
                    
                    if (matchingAttractions.isEmpty()) {
                        System.out.println("警告: 找不到景点: " + attractionInput);
                        System.out.println("提示: 输入 'list attractions' 查看所有可用景点");
                        hasInvalidAttractions = true;
                    } else if (matchingAttractions.size() == 1) {
                        // 只有一个匹配项
                        String attraction = matchingAttractions.get(0);
                        if (!attractionInput.equals(attraction)) {
                            System.out.println("提示: '" + attractionInput + "' 已匹配到景点 '" + attraction + "'");
                        }
                        attractions.add(attraction);
                    } else {
                        // 有多个匹配项，让用户选择
                        String selectedAttraction = chooseFromMultipleAttractions(attractionInput, matchingAttractions);
                        if (selectedAttraction != null) {
                            attractions.add(selectedAttraction);
                        }
                    }
                }
                
                if (hasInvalidAttractions) {
                    System.out.print("是否重新输入景点? (是/否，否则将只使用有效景点): ");
                    String retry = scanner.nextLine().trim().toLowerCase();
                    if (retry.equals("是") || retry.equals("y") || retry.equals("yes")) {
                        // 用户选择重新输入，继续循环
                        continue;
                    } else {
                        // 用户选择不重新输入，使用已有的有效景点
                        validAttractions = true;
                    }
                } else {
                    validAttractions = true;
                }
            } catch (Exception e) {
                System.out.println("输入格式错误: " + e.getMessage());
                System.out.println("请使用英文逗号分隔景点名称");
            }
        }
        
        return attractions;
    }
    
    /**
     * 从多个匹配的景点中选择一个
     * @param inputName 用户输入的景点名
     * @param matchingAttractions 匹配的景点列表
     * @return 用户选择的景点，如果选择跳过则返回null
     */
    private String chooseFromMultipleAttractions(String inputName, List<String> matchingAttractions) {
        System.out.println("'" + inputName + "' 匹配到多个景点:");
        for (int i = 0; i < matchingAttractions.size(); i++) {
            System.out.println((i + 1) + ". " + matchingAttractions.get(i));
        }
        
        System.out.print("请选择一个景点 (输入编号，或输入0跳过): ");
        String selection = scanner.nextLine().trim();
        try {
            int index = Integer.parseInt(selection) - 1;
            if (index >= 0 && index < matchingAttractions.size()) {
                String attraction = matchingAttractions.get(index);
                System.out.println("已选择: " + attraction);
                return attraction;
            } else if (index == -1) {
                System.out.println("已跳过此景点");
                return null;
            } else {
                System.out.println("无效的选择，已跳过此景点");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("输入无效，已跳过此景点");
            return null;
        }
    }
    
    /**
     * 显示旅行规划结果
     * @param tripData 旅行数据
     * @param route 规划的路线
     * @param totalDistance 总距离
     */
    private void displayResults(TripData tripData, List<String> route, int totalDistance) {
        System.out.println("\n结果:");
        System.out.println("起点: " + tripData.getStartingCity());
        System.out.println("终点: " + tripData.getEndingCity());
        System.out.println("景点: " + tripData.getAttractions());
        System.out.println("最佳路线: " + route);
        System.out.println("总距离: " + totalDistance + " 英里");
    }
    
    /**
     * 询问用户是否继续规划另一次旅行
     * @return true表示继续，false表示退出
     */
    private boolean askToContinue() {
        System.out.print("\n您想规划另一次旅行吗? (是/否): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        return answer.equals("是") || answer.equals("y") || answer.equals("yes");
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
        System.out.println("感谢使用道路旅行规划器！");
    }
    
    /**
     * 打印所有可用的城市，添加序号前缀
     * @return 按序号索引的城市名列表
     */
    private List<String> printAvailableCities() {
        Collection<City> cities = roadNetwork.getAllCities();
        List<String> cityNames = cities.stream()
                .map(City::getFullName)
                .sorted()
                .collect(Collectors.toList());
        
        System.out.println("序号\t城市名称");
        System.out.println("-------------------------");
        
        for (int i = 0; i < cityNames.size(); i++) {
            System.out.println((i + 1) + ".\t" + cityNames.get(i));
        }
        
        System.out.println("输入序号可直接选择城市");
        return cityNames;
    }
    
    /**
     * 打印所有可用的景点，添加序号前缀
     * @return 按序号索引的景点名列表
     */
    private List<String> printAvailableAttractions() {
        Collection<Attraction> attractions = roadNetwork.getAllAttractions();
        List<String> attractionFullNames = attractions.stream()
                .map(attraction -> attraction.getName() + " (" + attraction.getLocation() + ")")
                .sorted()
                .collect(Collectors.toList());
        
        List<String> attractionNames = attractions.stream()
                .map(Attraction::getName)
                .sorted()
                .collect(Collectors.toList());
        
        System.out.println("序号\t景点名称 (所在城市)");
        System.out.println("-------------------------------");
        
        for (int i = 0; i < attractionFullNames.size(); i++) {
            System.out.println((i + 1) + ".\t" + attractionFullNames.get(i));
        }
        
        System.out.println("输入序号可直接选择景点");
        return attractionNames;
    }
    
    /**
     * Task B的测试方法，运行所需的测试用例
     */
    private void runTestCases() {
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
    
    /**
     * 旅行数据类，用于封装旅行计划的核心信息
     */
    private static class TripData {
        private final String startingCity;
        private final String endingCity;
        private final List<String> attractions;
        
        public TripData(String startingCity, String endingCity, List<String> attractions) {
            this.startingCity = startingCity;
            this.endingCity = endingCity;
            this.attractions = attractions;
        }
        
        public String getStartingCity() {
            return startingCity;
        }
        
        public String getEndingCity() {
            return endingCity;
        }
        
        public List<String> getAttractions() {
            return attractions;
        }
    }
} 