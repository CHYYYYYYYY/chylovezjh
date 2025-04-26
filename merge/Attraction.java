/**
 * 表示旅行规划中的景点（兴趣点）
 */
public class Attraction {
    private String name;
    private String location; // 城市和州
    private String city; // 城市名称
    private String state; // 州缩写

    public Attraction(String name, String location) {
        this.name = name;
        this.location = location;
        
        // 解析location以提取城市和州
        String[] parts = location.split(" ");
        if (parts.length > 1) {
            this.state = parts[parts.length - 1];
            StringBuilder cityBuilder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) {
                    cityBuilder.append(" ");
                }
                cityBuilder.append(parts[i]);
            }
            this.city = cityBuilder.toString();
        } else {
            this.city = location;
            this.state = "";
        }
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return name + " (" + location + ")";
    }
} 