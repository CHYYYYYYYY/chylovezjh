import java.util.*;

/**
 * 使用基于图的算法规划城市间的路线，访问景点
 */
public class RoutePlanner {
    private RoadNetwork roadNetwork;
    
    public RoutePlanner(RoadNetwork roadNetwork) {
        this.roadNetwork = roadNetwork;
    }
    
    /**
     * 寻找从起始城市到目的地城市的最短路线，途经所有景点
     * 使用Dijkstra算法的变体，带有中间点
     * 
     * @param startingCity 起始城市全名（例如 "New York NY"）
     * @param endingCity 目的地城市全名（例如 "Miami FL"）
     * @param attractions 要访问的景点名称列表
     * @return 按顺序访问的城市列表，包括起始和结束城市
     */
    public List<String> route(String startingCity, String endingCity, List<String> attractions) {
        // 验证输入
        if (!roadNetwork.cityExists(startingCity)) {
            throw new IllegalArgumentException("找不到起始城市: " + startingCity);
        }
        if (!roadNetwork.cityExists(endingCity)) {
            throw new IllegalArgumentException("找不到目的地城市: " + endingCity);
        }
        
        // 获取景点所在的城市
        List<City> attractionCities = new ArrayList<>();
        for (String attraction : attractions) {
            City city = roadNetwork.getCityForAttraction(attraction);
            if (city != null) {
                attractionCities.add(city);
            } else {
                throw new IllegalArgumentException("找不到景点: " + attraction);
            }
        }
        
        // 获取起始和结束城市对象
        City start = roadNetwork.getCity(startingCity);
        City end = roadNetwork.getCity(endingCity);
        
        // 如果没有景点，只需找到从起点到终点的最短路径
        if (attractionCities.isEmpty()) {
            Map<City, Integer> distances = new HashMap<>();
            Map<City, City> previous = new HashMap<>();
            findShortestPath(start, end, distances, previous);
            
            List<String> route = constructPath(start, end, previous);
            if (route.isEmpty()) {
                throw new RuntimeException("找不到从 " + startingCity + " 到 " + endingCity + " 的路径");
            }
            return route;
        }
        
        // 添加起始和结束城市
        List<City> allCities = new ArrayList<>();
        allCities.add(start);
        allCities.addAll(attractionCities);
        allCities.add(end);
        
        // 找出每对城市之间的所有最短路径
        Map<String, Map<String, List<String>>> allPaths = new HashMap<>();
        Map<String, Map<String, Integer>> allDistances = new HashMap<>();
        
        for (int i = 0; i < allCities.size(); i++) {
            City city = allCities.get(i);
            String cityName = city.getFullName();
            
            allPaths.put(cityName, new HashMap<>());
            allDistances.put(cityName, new HashMap<>());
            
            Map<City, Integer> distances = new HashMap<>();
            Map<City, City> previous = new HashMap<>();
            
            // 从这个城市找到到所有其他城市的最短路径
            findShortestPath(city, null, distances, previous);
            
            // 存储路径和距离
            for (int j = 0; j < allCities.size(); j++) {
                if (i != j) {
                    City other = allCities.get(j);
                    String otherName = other.getFullName();
                    
                    List<String> path = constructPath(city, other, previous);
                    int distance = distances.getOrDefault(other, Integer.MAX_VALUE);
                    
                    if (!path.isEmpty()) {
                        allPaths.get(cityName).put(otherName, path);
                        allDistances.get(cityName).put(otherName, distance);
                    }
                }
            }
        }
        
        // 现在使用类似TSP的方法找出访问城市的最佳顺序
        // 为简单起见，我们这里使用贪心算法
        List<City> orderedCities = new ArrayList<>();
        orderedCities.add(start);
        
        List<City> remainingCities = new ArrayList<>(attractionCities);
        
        while (!remainingCities.isEmpty()) {
            City current = orderedCities.get(orderedCities.size() - 1);
            City closest = null;
            int minDistance = Integer.MAX_VALUE;
            
            for (City city : remainingCities) {
                String fromName = current.getFullName();
                String toName = city.getFullName();
                
                int distance = allDistances.getOrDefault(fromName, new HashMap<>())
                                         .getOrDefault(toName, Integer.MAX_VALUE);
                
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = city;
                }
            }
            
            if (closest != null) {
                orderedCities.add(closest);
                remainingCities.remove(closest);
            } else {
                throw new RuntimeException("找不到到剩余景点的路径");
            }
        }
        
        orderedCities.add(end);
        
        // 构建最终路线
        List<String> finalRoute = new ArrayList<>();
        for (int i = 0; i < orderedCities.size() - 1; i++) {
            City from = orderedCities.get(i);
            City to = orderedCities.get(i + 1);
            
            String fromName = from.getFullName();
            String toName = to.getFullName();
            
            List<String> segment = allPaths.getOrDefault(fromName, new HashMap<>())
                                          .getOrDefault(toName, new ArrayList<>());
            
            if (i == 0) {
                finalRoute.addAll(segment);
            } else {
                // 跳过第一个城市，因为它已经在前一段路线中
                finalRoute.addAll(segment.subList(1, segment.size()));
            }
        }
        
        return finalRoute;
    }
    
    /**
     * Dijkstra算法实现，寻找最短路径
     */
    private void findShortestPath(City start, City end, Map<City, Integer> distances, Map<City, City> previous) {
        PriorityQueue<City> queue = new PriorityQueue<>(
            Comparator.comparingInt(city -> distances.getOrDefault(city, Integer.MAX_VALUE))
        );
        
        // 初始化距离
        distances.put(start, 0);
        queue.add(start);
        
        Set<City> visited = new HashSet<>();
        
        while (!queue.isEmpty()) {
            City current = queue.poll();
            
            // 如果我们到达了目的地，可以停止
            if (current.equals(end)) {
                break;
            }
            
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            
            int distance = distances.get(current);
            
            for (Map.Entry<City, Integer> connection : current.getConnections().entrySet()) {
                City neighbor = connection.getKey();
                int edgeWeight = connection.getValue();
                
                int newDistance = distance + edgeWeight;
                
                if (!distances.containsKey(neighbor) || newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
    }
    
    /**
     * 使用previous映射构建从起点到终点的路径
     */
    private List<String> constructPath(City start, City end, Map<City, City> previous) {
        List<String> path = new ArrayList<>();
        
        if (!previous.containsKey(end) && !start.equals(end)) {
            return path; // 不存在路径
        }
        
        City current = end;
        while (current != null) {
            path.add(0, current.getFullName());
            if (current.equals(start)) {
                break;
            }
            current = previous.get(current);
        }
        
        return path;
    }
    
    /**
     * 计算路线的总距离
     */
    public int calculateTotalDistance(List<String> route) {
        int totalDistance = 0;
        
        for (int i = 0; i < route.size() - 1; i++) {
            String from = route.get(i);
            String to = route.get(i + 1);
            
            City fromCity = roadNetwork.getCity(from);
            City toCity = roadNetwork.getCity(to);
            
            Integer distance = fromCity.getConnections().get(toCity);
            if (distance != null) {
                totalDistance += distance;
            } else {
                throw new RuntimeException("在 " + from + " 和 " + to + " 之间没有直接连接");
            }
        }
        
        return totalDistance;
    }
} 