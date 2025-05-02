package org.example.cw;

import java.util.HashMap;
import java.util.Map;

/**
 * 语言管理器，用于管理应用程序的多语言支持
 */
public class LanguageManager {
    // 语言常量
    public static final int LANGUAGE_CHINESE = 0;
    public static final int LANGUAGE_ENGLISH = 1;
    
    // 当前语言
    private static int currentLanguage = LANGUAGE_CHINESE;
    
    // 文本资源
    private static final Map<Integer, Map<String, String>> textResources = new HashMap<>();
    
    static {
        // 中文资源
        Map<String, String> zhResources = new HashMap<>();
        zhResources.put("appTitle", "道路旅行规划器");
        zhResources.put("citySelection", "城市选择");
        zhResources.put("searchCity", "搜索城市:");
        zhResources.put("cityPrompt", "输入城市名或州缩写");
        zhResources.put("startCity", "起始城市:");
        zhResources.put("selectStartCity", "选择起始城市");
        zhResources.put("endCity", "目的地城市:");
        zhResources.put("selectEndCity", "选择目的地城市");
        zhResources.put("attractionSelection", "景点选择");
        zhResources.put("searchAttraction", "搜索景点:");
        zhResources.put("attractionPrompt", "输入景点名称");
        zhResources.put("availableAttractions", "可选景点:");
        zhResources.put("selectedAttractions", "已选景点:");
        zhResources.put("add", "添加");
        zhResources.put("remove", "删除");
        zhResources.put("algorithmOptions", "算法选项");
        zhResources.put("useAStar", "使用改进的A*算法（否则使用Dijkstra）");
        zhResources.put("useOrderedAttractions", "按指定顺序访问景点（否则优化顺序）");
        zhResources.put("calculateRoute", "计算最佳路线");
        zhResources.put("compareAlgorithms", "比较算法性能");
        zhResources.put("results", "计算结果");
        zhResources.put("routeMap", "路线地图");
        zhResources.put("from", "从");
        zhResources.put("to", "到");
        zhResources.put("algorithm", "算法:");
        zhResources.put("attractionVisit", "景点访问:");
        zhResources.put("ordered", "按顺序");
        zhResources.put("optimized", "优化顺序");
        zhResources.put("totalDistance", "总距离:");
        zhResources.put("kilometers", "公里");
        zhResources.put("route", "路线:");
        zhResources.put("attraction", "景点:");
        zhResources.put("inputError", "输入错误");
        zhResources.put("selectCities", "请选择起始城市和目的地城市");
        zhResources.put("routeError", "路线计算错误");
        zhResources.put("routeErrorDesc", "无法计算路线:");
        zhResources.put("dijkstraTime", "Dijkstra算法时间:");
        zhResources.put("astarTime", "A*算法时间:");
        zhResources.put("milliseconds", "毫秒");
        zhResources.put("dijkstraDistance", "Dijkstra算法距离:");
        zhResources.put("astarDistance", "A*算法距离:");
        zhResources.put("performanceComparison", "性能比较");
        zhResources.put("languageSwitch", "Switch to English");
        
        textResources.put(LANGUAGE_CHINESE, zhResources);
        
        // 英文资源
        Map<String, String> enResources = new HashMap<>();
        enResources.put("appTitle", "Road Trip Planner");
        enResources.put("citySelection", "City Selection");
        enResources.put("searchCity", "Search City:");
        enResources.put("cityPrompt", "Enter city name or state");
        enResources.put("startCity", "Start City:");
        enResources.put("selectStartCity", "Select starting city");
        enResources.put("endCity", "Destination City:");
        enResources.put("selectEndCity", "Select destination city");
        enResources.put("attractionSelection", "Attraction Selection");
        enResources.put("searchAttraction", "Search Attraction:");
        enResources.put("attractionPrompt", "Enter attraction name");
        enResources.put("availableAttractions", "Available Attractions:");
        enResources.put("selectedAttractions", "Selected Attractions:");
        enResources.put("add", "Add");
        enResources.put("remove", "Remove");
        enResources.put("algorithmOptions", "Algorithm Options");
        enResources.put("useAStar", "Use improved A* algorithm (otherwise Dijkstra)");
        enResources.put("useOrderedAttractions", "Visit attractions in specified order (otherwise optimize)");
        enResources.put("calculateRoute", "Calculate Best Route");
        enResources.put("compareAlgorithms", "Compare Algorithm Performance");
        enResources.put("results", "Results");
        enResources.put("routeMap", "Route Map");
        enResources.put("from", "From");
        enResources.put("to", "to");
        enResources.put("algorithm", "Algorithm:");
        enResources.put("attractionVisit", "Attraction Visit:");
        enResources.put("ordered", "In Order");
        enResources.put("optimized", "Optimized Order");
        enResources.put("totalDistance", "Total Distance:");
        enResources.put("kilometers", "kilometers");
        enResources.put("route", "Route:");
        enResources.put("attraction", "Attraction:");
        enResources.put("inputError", "Input Error");
        enResources.put("selectCities", "Please select start and destination cities");
        enResources.put("routeError", "Route Calculation Error");
        enResources.put("routeErrorDesc", "Unable to calculate route:");
        enResources.put("dijkstraTime", "Dijkstra algorithm time:");
        enResources.put("astarTime", "A* algorithm time:");
        enResources.put("milliseconds", "milliseconds");
        enResources.put("dijkstraDistance", "Dijkstra algorithm distance:");
        enResources.put("astarDistance", "A* algorithm distance:");
        enResources.put("performanceComparison", "Performance Comparison");
        enResources.put("languageSwitch", "切换到中文");
        
        textResources.put(LANGUAGE_ENGLISH, enResources);
    }
    
    /**
     * 获取指定键的文本，根据当前语言设置
     * @param key 文本键
     * @return 对应语言的文本
     */
    public static String getText(String key) {
        return textResources.get(currentLanguage).getOrDefault(key, key);
    }
    
    /**
     * 切换语言
     * @return 切换后的语言代码
     */
    public static int toggleLanguage() {
        currentLanguage = (currentLanguage == LANGUAGE_CHINESE) ? LANGUAGE_ENGLISH : LANGUAGE_CHINESE;
        return currentLanguage;
    }
    
    /**
     * 设置当前语言
     * @param language 语言代码
     */
    public static void setLanguage(int language) {
        if (language == LANGUAGE_CHINESE || language == LANGUAGE_ENGLISH) {
            currentLanguage = language;
        }
    }
    
    /**
     * 获取当前语言
     * @return 当前语言代码
     */
    public static int getCurrentLanguage() {
        return currentLanguage;
    }
} 