import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 表示道路网络图中的城市节点
 */
public class City {
    private String name;
    private String state;
    private Map<City, Integer> connections; // 相邻城市和距离
    private boolean hasAttraction; // 标示该城市是否有景点
    private String attractionName; // 景点名称（如果有）

    public City(String name, String state) {
        this.name = name;
        this.state = state;
        this.connections = new HashMap<>();
        this.hasAttraction = false;
        this.attractionName = null;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public String getFullName() {
        return name + " " + state;
    }

    public void addConnection(City destination, int distance) {
        connections.put(destination, distance);
    }

    public Map<City, Integer> getConnections() {
        return connections;
    }

    public boolean hasAttraction() {
        return hasAttraction;
    }

    public void setHasAttraction(boolean hasAttraction) {
        this.hasAttraction = hasAttraction;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
        this.hasAttraction = (attractionName != null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(name, city.name) && 
               Objects.equals(state, city.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, state);
    }

    @Override
    public String toString() {
        return name + " " + state;
    }
} 