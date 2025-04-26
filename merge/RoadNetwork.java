import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
     * 从CSV文件加载城市和道路数据
     */
    public void loadData(String roadsFile, String attractionsFile) {
        loadAttractions(attractionsFile);
        loadRoads(roadsFile);
    }
    
    /**
     * 从CSV文件加载景点数据
     */
    private void loadAttractions(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
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
    private void loadRoads(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
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
                        cities.put(cityAName, new City(cityName.toString(), state));
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
                        cities.put(cityBName, new City(cityName.toString(), state));
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
     * 根据模糊输入查找城市
     * 可以匹配城市名称、州缩写或名称的一部分
     * @param fuzzyName 用户输入的模糊城市名
     * @return 找到的城市全名，如果没找到则返回null
     */
    public String findCityByFuzzyName(String fuzzyName) {
        if (fuzzyName == null || fuzzyName.trim().isEmpty()) {
            return null;
        }
        
        String normalizedInput = fuzzyName.trim().toLowerCase();
        
        // 1. 直接尝试精确匹配（不区分大小写）
        for (String cityName : cities.keySet()) {
            if (cityName.toLowerCase().equals(normalizedInput)) {
                return cityName;
            }
        }
        
        // 2. 检查是否匹配州缩写
        for (String cityName : cities.keySet()) {
            String[] parts = cityName.split(" ");
            String state = parts[parts.length - 1].toLowerCase();
            if (state.equals(normalizedInput)) {
                return cityName;
            }
        }
        
        // 3. 检查是否是城市名的一部分（没有州缩写）
        for (String cityName : cities.keySet()) {
            String[] parts = cityName.split(" ");
            StringBuilder cityNameOnly = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) cityNameOnly.append(" ");
                cityNameOnly.append(parts[i]);
            }
            
            if (cityNameOnly.toString().toLowerCase().equals(normalizedInput)) {
                return cityName;
            }
        }
        
        // 4. 检查是否包含输入的关键词
        List<String> matchingCities = new ArrayList<>();
        for (String cityName : cities.keySet()) {
            if (cityName.toLowerCase().contains(normalizedInput)) {
                matchingCities.add(cityName);
            }
        }
        
        if (matchingCities.size() == 1) {
            return matchingCities.get(0);
        } else if (matchingCities.size() > 1) {
            // 如果有多个匹配项，返回最短的那个（通常更精确）
            return matchingCities.stream()
                    .min(Comparator.comparing(String::length))
                    .orElse(null);
        }
        
        // 5. 拆分输入的单词进行更灵活的匹配
        String[] inputWords = normalizedInput.split("\\s+");
        if (inputWords.length > 1) {
            for (String cityName : cities.keySet()) {
                boolean allWordsMatch = true;
                String lowerCityName = cityName.toLowerCase();
                for (String word : inputWords) {
                    if (!lowerCityName.contains(word)) {
                        allWordsMatch = false;
                        break;
                    }
                }
                if (allWordsMatch) {
                    return cityName;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 检查网络中是否存在城市（支持模糊匹配）
     */
    public boolean cityExistsFuzzy(String fuzzyName) {
        return findCityByFuzzyName(fuzzyName) != null;
    }
    
    /**
     * 检查网络中是否存在城市
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
     * 根据模糊输入查找景点
     * @param fuzzyName 用户输入的模糊景点名
     * @return 找到的景点全名，如果没找到则返回null
     */
    public String findAttractionByFuzzyName(String fuzzyName) {
        if (fuzzyName == null || fuzzyName.trim().isEmpty()) {
            return null;
        }
        
        String normalizedInput = fuzzyName.trim().toLowerCase();
        
        // 1. 直接尝试精确匹配（不区分大小写）
        for (String attractionName : attractions.keySet()) {
            if (attractionName.toLowerCase().equals(normalizedInput)) {
                return attractionName;
            }
        }
        
        // 2. 检查是否包含输入的关键词（全词匹配）
        List<String> matchingAttractions = new ArrayList<>();
        for (String attractionName : attractions.keySet()) {
            if (attractionName.toLowerCase().contains(normalizedInput)) {
                matchingAttractions.add(attractionName);
            }
        }
        
        if (matchingAttractions.size() == 1) {
            return matchingAttractions.get(0);
        }
        
        // 3. 检查是否匹配部分单词
        if (matchingAttractions.isEmpty()) {
            for (String attractionName : attractions.keySet()) {
                String[] words = attractionName.toLowerCase().split("\\s+");
                for (String word : words) {
                    if (word.contains(normalizedInput) || normalizedInput.contains(word)) {
                        matchingAttractions.add(attractionName);
                        break;
                    }
                }
            }
            
            if (matchingAttractions.size() == 1) {
                return matchingAttractions.get(0);
            }
        }
        
        // 4. 拆分输入的单词进行更灵活的匹配
        if (matchingAttractions.isEmpty() && normalizedInput.contains(" ")) {
            String[] inputWords = normalizedInput.split("\\s+");
            
            // 计算每个景点的匹配分数
            Map<String, Integer> matchScores = new HashMap<>();
            
            for (String attractionName : attractions.keySet()) {
                String lowerAttractionName = attractionName.toLowerCase();
                int score = 0;
                
                for (String word : inputWords) {
                    if (word.length() <= 2) continue; // 跳过太短的词
                    
                    if (lowerAttractionName.contains(word)) {
                        score += 2; // 包含完整单词加2分
                    } else {
                        // 检查是否包含部分单词
                        String[] attractionWords = lowerAttractionName.split("\\s+");
                        for (String aWord : attractionWords) {
                            if (aWord.contains(word) || word.contains(aWord)) {
                                score += 1; // 部分匹配加1分
                                break;
                            }
                        }
                    }
                }
                
                if (score > 0) {
                    matchScores.put(attractionName, score);
                }
            }
            
            // 找出得分最高的景点
            if (!matchScores.isEmpty()) {
                return matchScores.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .get().getKey();
            }
        }
        
        // 5. 尝试使用编辑距离算法进行模糊匹配
        if (matchingAttractions.isEmpty()) {
            String bestMatch = null;
            int minDistance = Integer.MAX_VALUE;
            
            for (String attractionName : attractions.keySet()) {
                int distance = calculateLevenshteinDistance(normalizedInput, attractionName.toLowerCase());
                if (distance < minDistance) {
                    minDistance = distance;
                    bestMatch = attractionName;
                }
            }
            
            // 只有当编辑距离足够小时才返回匹配结果
            if (minDistance <= Math.max(3, normalizedInput.length() / 3)) {
                return bestMatch;
            }
        }
        
        // 如果多个匹配，返回首个
        return matchingAttractions.isEmpty() ? null : matchingAttractions.get(0);
    }
    
    /**
     * 计算两个字符串之间的编辑距离（Levenshtein距离）
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] curr = new int[s2.length() + 1];
        
        for (int j = 0; j <= s2.length(); j++) {
            prev[j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        
        return prev[s2.length()];
    }
    
    /**
     * 检查景点是否存在（支持模糊匹配）
     */
    public boolean attractionExistsFuzzy(String fuzzyName) {
        return findAttractionByFuzzyName(fuzzyName) != null;
    }

    /**
     * 根据模糊输入查找所有匹配的城市
     * @param fuzzyName 用户输入的模糊城市名
     * @return 找到的城市全名列表，如果没找到则返回空列表
     */
    public List<String> findCitiesByFuzzyName(String fuzzyName) {
        if (fuzzyName == null || fuzzyName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String normalizedInput = fuzzyName.trim().toLowerCase();
        List<String> matchingCities = new ArrayList<>();
        
        // 1. 直接尝试精确匹配（不区分大小写）
        for (String cityName : cities.keySet()) {
            if (cityName.toLowerCase().equals(normalizedInput)) {
                matchingCities.add(cityName);
                return matchingCities; // 精确匹配直接返回
            }
        }
        
        // 2. 检查是否匹配州缩写
        if (normalizedInput.length() == 2) { // 可能是州缩写
            for (String cityName : cities.keySet()) {
                String[] parts = cityName.split(" ");
                String state = parts[parts.length - 1].toLowerCase();
                if (state.equals(normalizedInput)) {
                    matchingCities.add(cityName);
                }
            }
            
            if (!matchingCities.isEmpty()) {
                return matchingCities;
            }
        }
        
        // 3. 检查其他匹配方式
        for (String cityName : cities.keySet()) {
            // 检查城市名（不包含州）
            String[] parts = cityName.split(" ");
            StringBuilder cityNameOnly = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) cityNameOnly.append(" ");
                cityNameOnly.append(parts[i]);
            }
            
            String cityNameStr = cityNameOnly.toString().toLowerCase();
            
            if (cityNameStr.equals(normalizedInput)) {
                matchingCities.add(cityName);
            } else if (cityName.toLowerCase().contains(normalizedInput)) {
                matchingCities.add(cityName);
            }
        }
        
        return matchingCities;
    }

    /**
     * 根据模糊输入查找所有匹配的景点
     * @param fuzzyName 用户输入的模糊景点名
     * @return 找到的景点全名列表，如果没找到则返回空列表
     */
    public List<String> findAttractionsByFuzzyName(String fuzzyName) {
        if (fuzzyName == null || fuzzyName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String normalizedInput = fuzzyName.trim().toLowerCase();
        List<String> matchingAttractions = new ArrayList<>();
        
        // 1. 直接尝试精确匹配（不区分大小写）
        for (String attractionName : attractions.keySet()) {
            if (attractionName.toLowerCase().equals(normalizedInput)) {
                matchingAttractions.add(attractionName);
                return matchingAttractions; // 精确匹配直接返回
            }
        }
        
        // 2. 检查是否包含输入的关键词（全词匹配）
        for (String attractionName : attractions.keySet()) {
            if (attractionName.toLowerCase().contains(normalizedInput)) {
                matchingAttractions.add(attractionName);
            }
        }
        
        if (!matchingAttractions.isEmpty()) {
            return matchingAttractions;
        }
        
        // 3. 检查是否匹配部分单词
        for (String attractionName : attractions.keySet()) {
            String[] words = attractionName.toLowerCase().split("\\s+");
            for (String word : words) {
                if (word.contains(normalizedInput) || normalizedInput.contains(word)) {
                    matchingAttractions.add(attractionName);
                    break;
                }
            }
        }
        
        return matchingAttractions;
    }
} 