package org.example.cw;

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
     * 支持Dijkstra和A*算法，以及有序和无序景点访问
     * 
     * @param startingCity 起始城市全名（例如 "New York NY"）
     * @param endingCity 目的地城市全名（例如 "Miami FL"）
     * @param attractions 要访问的景点名称列表
     * @param useAstar 是否使用A*算法（true）或Dijkstra算法（false）
     * @param orderedAttractions 景点是否按指定顺序访问
     * @return 按顺序访问的城市列表，包括起始和结束城市
     */
    public List<String> route(String startingCity, String endingCity, List<String> attractions, 
                              boolean useAstar, boolean orderedAttractions) {
        // 验证输入
        if (!roadNetwork.cityExists(startingCity)) {
            throw new IllegalArgumentException("找不到起始城市: " + startingCity);
        }
        if (!roadNetwork.cityExists(endingCity)) {
            throw new IllegalArgumentException("找不到目的地城市: " + endingCity);
        }
        
        // 获取起始和结束城市对象
        City start = roadNetwork.getCity(startingCity);
        City end = roadNetwork.getCity(endingCity);
        
        // 如果没有景点，只需找到从起点到终点的最短路径
        if (attractions == null || attractions.isEmpty()) {
            Map<City, Integer> distances = new HashMap<>();
            Map<City, City> previous = new HashMap<>();
            
            // 比较Dijkstra和A*算法
            long startTime = System.nanoTime();
            
            if (useAstar) {
                findShortestPathAStar(start, end, distances, previous);
            } else {
                findShortestPath(start, end, distances, previous);
            }
            
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000; // 转换为毫秒
            
            List<String> route = constructPath(start, end, previous);
            if (route.isEmpty()) {
                throw new RuntimeException("找不到从 " + startingCity + " 到 " + endingCity + " 的路径");
            }
            
            // 打印算法的复杂度分析
            int vertices = roadNetwork.getCities().size();
            int edges = countEdges();
            System.out.println("算法: " + (useAstar ? "A*" : "Dijkstra"));
            System.out.println("顶点数 (V): " + vertices);
            System.out.println("边数 (E): " + edges);
            System.out.println("时间复杂度: O(V + E log V)");
            System.out.println("执行时间: " + duration + " 毫秒");
            
            return route;
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
        
        // 如果景点按顺序访问
        if (orderedAttractions) {
            return handleOrderedAttractions(start, end, attractionCities, useAstar);
        } else {
            // 不按顺序访问景点，尝试所有可能的排列
            return handleUnorderedAttractions(start, end, attractionCities, useAstar);
        }
    }
    
    /**
     * 处理按顺序访问景点的情况
     */
    private List<String> handleOrderedAttractions(City start, City end, List<City> attractionCities, boolean useAstar) {
        // 构建需要访问的所有城市列表（起点 -> 景点1 -> 景点2 -> ... -> 终点）
        List<City> orderedCities = new ArrayList<>();
        orderedCities.add(start);
        orderedCities.addAll(attractionCities);
        orderedCities.add(end);
        
        // 分段计算最短路径
        List<String> finalRoute = new ArrayList<>();
        
        for (int i = 0; i < orderedCities.size() - 1; i++) {
            City from = orderedCities.get(i);
            City to = orderedCities.get(i + 1);
            
            Map<City, Integer> distances = new HashMap<>();
            Map<City, City> previous = new HashMap<>();
            
            if (useAstar) {
                findShortestPathAStar(from, to, distances, previous);
            } else {
                findShortestPath(from, to, distances, previous);
            }
            
            List<String> segment = constructPath(from, to, previous);
            
            if (segment.isEmpty()) {
                throw new RuntimeException("找不到从 " + from.getFullName() + " 到 " + to.getFullName() + " 的路径");
            }
            
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
     * 处理不按顺序访问景点的情况（使用全排列找最短路径）
     */
    private List<String> handleUnorderedAttractions(City start, City end, List<City> attractionCities, boolean useAstar) {
        List<List<City>> allPermutations = generatePermutations(attractionCities);
        
        List<String> bestRoute = null;
        int shortestDistance = Integer.MAX_VALUE;
        
        for (List<City> permutation : allPermutations) {
            // 构建这个排列的访问顺序
            List<City> orderedCities = new ArrayList<>();
            orderedCities.add(start);
            orderedCities.addAll(permutation);
            orderedCities.add(end);
            
            // 计算这个排列的路径
            List<String> route = new ArrayList<>();
            int totalDistance = 0;
            boolean validRoute = true;
            
            for (int i = 0; i < orderedCities.size() - 1; i++) {
                City from = orderedCities.get(i);
                City to = orderedCities.get(i + 1);
                
                Map<City, Integer> distances = new HashMap<>();
                Map<City, City> previous = new HashMap<>();
                
                if (useAstar) {
                    findShortestPathAStar(from, to, distances, previous);
                } else {
                    findShortestPath(from, to, distances, previous);
                }
                
                List<String> segment = constructPath(from, to, previous);
                
                if (segment.isEmpty()) {
                    validRoute = false;
                    break;
                }
                
                if (i == 0) {
                    route.addAll(segment);
                } else {
                    route.addAll(segment.subList(1, segment.size()));
                }
                
                totalDistance += distances.get(to);
            }
            
            if (validRoute && totalDistance < shortestDistance) {
                shortestDistance = totalDistance;
                bestRoute = route;
            }
        }
        
        if (bestRoute == null) {
            throw new RuntimeException("找不到经过所有景点的有效路径");
        }
        
        System.out.println("找到的最短路径总距离: " + shortestDistance);
        return bestRoute;
    }
    
    /**
     * 生成列表的所有排列
     */
    private <T> List<List<T>> generatePermutations(List<T> list) {
        List<List<T>> result = new ArrayList<>();
        generatePermutationsHelper(list, 0, result);
        return result;
    }
    
    private <T> void generatePermutationsHelper(List<T> list, int start, List<List<T>> result) {
        if (start == list.size() - 1) {
            result.add(new ArrayList<>(list));
            return;
        }
        
        for (int i = start; i < list.size(); i++) {
            // 交换元素
            Collections.swap(list, start, i);
            // 递归生成剩余元素的排列
            generatePermutationsHelper(list, start + 1, result);
            // 恢复原始顺序（回溯）
            Collections.swap(list, start, i);
        }
    }
    
    /**
     * 计算图中的边数
     */
    private int countEdges() {
        Set<String> countedEdges = new HashSet<>();
        int totalEdges = 0;
        
        for (City city : roadNetwork.getCities()) {
            for (Map.Entry<City, Integer> connection : city.getConnections().entrySet()) {
                City neighbor = connection.getKey();
                // 为避免重复计算双向边，创建一个唯一标识符
                String edgeId = city.getFullName().compareTo(neighbor.getFullName()) < 0 ?
                        city.getFullName() + "-" + neighbor.getFullName() :
                        neighbor.getFullName() + "-" + city.getFullName();
                
                if (!countedEdges.contains(edgeId)) {
                    countedEdges.add(edgeId);
                    totalEdges++;
                }
            }
        }
        
        return totalEdges;
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
     * A*算法实现，寻找最短路径
     * 使用简单启发式函数，不依赖经纬度坐标
     */
    private void findShortestPathAStar(City start, City end, Map<City, Integer> distances, Map<City, City> previous) {
        // 记录g(n)：从起点到当前节点的实际距离
        Map<City, Integer> gScore = new HashMap<>();
        gScore.put(start, 0);
        
        // 记录f(n) = g(n) + h(n)：总估计成本
        Map<City, Integer> fScore = new HashMap<>();
        fScore.put(start, simpleHeuristic(start, end));
        
        // 使用优先队列按f(n)排序
        PriorityQueue<City> openSet = new PriorityQueue<>(
            Comparator.comparingInt(city -> fScore.getOrDefault(city, Integer.MAX_VALUE))
        );
        openSet.add(start);
        
        Set<City> closedSet = new HashSet<>();
        
        while (!openSet.isEmpty()) {
            City current = openSet.poll();
            
            if (current.equals(end)) {
                // 构建距离映射以与Dijkstra保持一致的接口
                distances.put(end, gScore.get(end));
                break;
            }
            
            closedSet.add(current);
            
            for (Map.Entry<City, Integer> connection : current.getConnections().entrySet()) {
                City neighbor = connection.getKey();
                int edgeWeight = connection.getValue();
                
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                
                int tentativeGScore = gScore.get(current) + edgeWeight;
                
                if (!openSet.contains(neighbor) || tentativeGScore < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    // 发现了更优路径
                    previous.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + simpleHeuristic(neighbor, end));
                    
                    // 更新距离映射以与Dijkstra保持一致的接口
                    distances.put(neighbor, tentativeGScore);
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
    }
    
    /**
     * 简单的启发式函数，不依赖经纬度坐标
     * 使用到终点连接城市的最小距离作为估计
     */
    private int simpleHeuristic(City current, City end) {
        // 直接连接到终点的情况
        if (current.getConnections().containsKey(end)) {
            return current.getConnections().get(end);
        }
        
        // 没有直接连接，则使用一个保守估计
        // 对于没有连接到终点的城市，我们使用最小连接距离作为保守估计
        int minDistance = Integer.MAX_VALUE;
        for (int distance : current.getConnections().values()) {
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        
        // 如果没有连接，返回0（退化为Dijkstra）
        if (minDistance == Integer.MAX_VALUE) {
            return 0;
        }
        
        // 返回最小连接距离作为启发式估计
        return minDistance / 2;  // 除以2是为了保证这是一个低估，确保算法的最优性
    }
    
    /**
     * 根据前驱节点构建路径
     */
    private List<String> constructPath(City start, City end, Map<City, City> previous) {
        List<String> path = new ArrayList<>();
        
        if (!previous.containsKey(end) && !end.equals(start)) {
            return path; // 没有路径
        }
        
        // 从终点追溯到起点
        List<City> cityPath = new ArrayList<>();
        City current = end;
        while (current != null && !current.equals(start)) {
            cityPath.add(current);
            current = previous.get(current);
        }
        
        if (current == null) {
            return path; // 没有完整路径
        }
        
        // 添加起点
        cityPath.add(start);
        
        // 反转路径（从起点到终点）
        Collections.reverse(cityPath);
        
        // 转换为城市名称
        for (City city : cityPath) {
            path.add(city.getFullName());
        }
        
        return path;
    }
    
    /**
     * 计算路线总距离
     */
    public int calculateTotalDistance(List<String> route) {
        int totalDistance = 0;
        
        if (route == null || route.size() < 2) {
            return totalDistance;
        }
        
        for (int i = 0; i < route.size() - 1; i++) {
            String cityA = route.get(i);
            String cityB = route.get(i + 1);
            
            City city1 = roadNetwork.getCity(cityA);
            City city2 = roadNetwork.getCity(cityB);
            
            if (city1 != null && city2 != null) {
                Map<City, Integer> connections = city1.getConnections();
                Integer distance = connections.get(city2);
                
                if (distance != null) {
                    totalDistance += distance;
                } else {
                    // 如果城市之间没有直接连接，这意味着计算的路线有问题
                    System.err.println("警告: 城市 " + cityA + " 和 " + cityB + " 之间没有直接连接");
                }
            }
        }
        
        return totalDistance;
    }
    
    /**
     * 为兼容原有代码提供的方法
     */
    public List<String> route(String startingCity, String endingCity, List<String> attractions) {
        // 默认使用Dijkstra算法，按给定顺序访问景点
        return route(startingCity, endingCity, attractions, false, true);
    }
} 