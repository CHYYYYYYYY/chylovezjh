package org.example.cw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 表示道路网络，其中城市是节点，道路是带有距离权重的边
 */
public class RoadNetwork {
    private Map<String, City> cities; // 城市全名到City对象的映射
    private Map<String, Attraction> attractions; // 景点名称到Attraction对象的映射
    private Map<String, String> cityToAttraction; // 城市到景点的映射

    public RoadNetwork() {
        cities = new HashMap<>();
        attractions = new HashMap<>();
        cityToAttraction = new HashMap<>();
    }

    /**
     * 从资源文件加载城市和道路数据
     */
    public void loadData(String roadsFile, String attractionsFile) {
        loadFromResources(roadsFile, attractionsFile);
    }
    
    /**
     * 从资源文件加载数据
     */
    private void loadFromResources(String roadsFile, String attractionsFile) {
        try {
            InputStream attractionsStream = getClass().getResourceAsStream(attractionsFile);
            if (attractionsStream != null) {
                loadAttractions(new BufferedReader(new InputStreamReader(attractionsStream)));
            } else {
                System.err.println("景点资源文件未找到: " + attractionsFile);
            }
            
            InputStream roadsStream = getClass().getResourceAsStream(roadsFile);
            if (roadsStream != null) {
                loadRoads(new BufferedReader(new InputStreamReader(roadsStream)));
            } else {
                System.err.println("道路资源文件未找到: " + roadsFile);
            }
        } catch (Exception e) {
            System.err.println("加载资源文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从CSV文件加载景点数据
     */
    private void loadAttractions(BufferedReader br) {
        try {
            String line = br.readLine(); // 跳过标题行
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String attractionName = parts[0].trim();
                    String location = parts[1].trim();
                    
                    Attraction attraction = new Attraction(attractionName, location);
                    attractions.put(attractionName, attraction);
                    
                    // 如果城市不存在则创建
                    String cityFullName = location;
                    if (!cities.containsKey(cityFullName)) {
                        String[] cityParts = location.split(" ");
                        String state = cityParts[cityParts.length - 1];
                        StringBuilder cityName = new StringBuilder();
                        for (int i = 0; i < cityParts.length - 1; i++) {
                            if (i > 0) {
                                cityName.append(" ");
                            }
                            cityName.append(cityParts[i]);
                        }
                        
                        City city = new City(cityName.toString(), state);
                        cities.put(cityFullName, city);
                    }
                    
                    // 将景点与城市关联
                    City city = cities.get(cityFullName);
                    city.setAttractionName(attractionName);
                    cityToAttraction.put(cityFullName, attractionName);
                }
            }
        } catch (IOException e) {
            System.err.println("加载景点时出错: " + e.getMessage());
        }
    }
    
    /**
     * 从CSV文件加载道路数据
     */
    private void loadRoads(BufferedReader br) {
        try {
            String line = br.readLine(); // 跳过标题行
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String cityAName = parts[0].trim();
                    String cityBName = parts[1].trim();
                    int distance = Integer.parseInt(parts[2].trim());
                    
                    // 如果城市不存在则创建
                    if (!cities.containsKey(cityAName)) {
                        String[] cityParts = cityAName.split(" ");
                        String state = cityParts[cityParts.length - 1];
                        StringBuilder cityName = new StringBuilder();
                        for (int i = 0; i < cityParts.length - 1; i++) {
                            if (i > 0) {
                                cityName.append(" ");
                            }
                            cityName.append(cityParts[i]);
                        }
                        City city = new City(cityName.toString(), state);
                        cities.put(cityAName, city);
                    }
                    
                    if (!cities.containsKey(cityBName)) {
                        String[] cityParts = cityBName.split(" ");
                        String state = cityParts[cityParts.length - 1];
                        StringBuilder cityName = new StringBuilder();
                        for (int i = 0; i < cityParts.length - 1; i++) {
                            if (i > 0) {
                                cityName.append(" ");
                            }
                            cityName.append(cityParts[i]);
                        }
                        City city = new City(cityName.toString(), state);
                        cities.put(cityBName, city);
                    }
                    
                    // 添加连接（双向）
                    City cityA = cities.get(cityAName);
                    City cityB = cities.get(cityBName);
                    
                    cityA.addConnection(cityB, distance);
                    cityB.addConnection(cityA, distance); // 图是无向的
                }
            }
        } catch (IOException e) {
            System.err.println("加载道路时出错: " + e.getMessage());
        }
    }
    
    /**
     * 通过全名（城市+州）获取城市
     */
    public City getCity(String fullName) {
        return cities.get(fullName);
    }
    
    /**
     * 获取网络中的所有城市
     */
    public Collection<City> getAllCities() {
        return cities.values();
    }
    
    /**
     * 通过名称获取景点
     */
    public Attraction getAttraction(String name) {
        return attractions.get(name);
    }
    
    /**
     * 获取所有景点
     */
    public Collection<Attraction> getAllAttractions() {
        return attractions.values();
    }
    
    /**
     * 获取城市中的景点（如果存在）
     */
    public String getAttractionInCity(String cityName) {
        return cityToAttraction.get(cityName);
    }
    
    /**
     * 获取所有城市名称列表
     */
    public List<String> getAllCityNames() {
        return new ArrayList<>(cities.keySet());
    }
    
    /**
     * 获取所有景点名称列表
     */
    public List<String> getAllAttractionNames() {
        return new ArrayList<>(attractions.keySet());
    }
    
    /**
     * 检查城市是否存在
     */
    public boolean cityExists(String fullName) {
        return cities.containsKey(fullName);
    }
    
    /**
     * 检查景点是否存在
     */
    public boolean attractionExists(String name) {
        return attractions.containsKey(name);
    }
    
    /**
     * 获取景点所在的城市
     */
    public City getCityForAttraction(String attractionName) {
        Attraction attraction = attractions.get(attractionName);
        if (attraction != null) {
            return cities.get(attraction.getLocation());
        }
        return null;
    }
    
    /**
     * 根据模糊输入查找城市
     */
    public List<String> findCitiesByFuzzyName(String fuzzyName) {
        if (fuzzyName == null || fuzzyName.trim().isEmpty()) {
            return new ArrayList<>(cities.keySet());
        }
        
        String normalizedInput = fuzzyName.trim().toLowerCase();
        List<String> matches = new ArrayList<>();
        
        // 精确匹配（不区分大小写）
        for (String cityName : cities.keySet()) {
            if (cityName.toLowerCase().contains(normalizedInput)) {
                matches.add(cityName);
            }
        }
        
        return matches;
    }
    
    /**
     * 根据模糊输入查找景点
     */
    public List<String> findAttractionsByFuzzyName(String fuzzyName) {
        if (fuzzyName == null || fuzzyName.trim().isEmpty()) {
            return new ArrayList<>(attractions.keySet());
        }
        
        String normalizedInput = fuzzyName.trim().toLowerCase();
        List<String> matches = new ArrayList<>();
        
        // 查找匹配（不区分大小写）
        for (String attractionName : attractions.keySet()) {
            if (attractionName.toLowerCase().contains(normalizedInput)) {
                matches.add(attractionName);
            }
        }
        
        return matches;
    }
    
    /**
     * 给指定城市设置经纬度坐标
     */
    public void setCityCoordinates(String cityName, double latitude, double longitude) {
        City city = cities.get(cityName);
        if (city != null) {
            city.setCoordinates(latitude, longitude);
        }
    }

    /**
     * 获取城市列表，包括其坐标信息
     */
    public Collection<City> getCities() {
        return cities.values();
    }
} 