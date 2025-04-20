# 道路旅行规划器

一个用于规划美国道路旅行的Java应用程序。该应用程序可以找到从起始城市到目的地城市的最短路线，同时经过用户选择的景点，并确保总行驶距离最短。

## 功能特点

- 从CSV文件加载和解析道路网络数据
- 使用Dijkstra算法查找最优路线
- 以最高效的顺序访问多个景点
- 交互式命令行界面

## 系统要求

- Java 8或更高版本
- CSV数据文件（`roads.csv`和`attractions.csv`）

## 如何编译和运行

1. 编译所有Java文件：
   ```
   javac *.java
   ```

2. 运行应用程序：
   ```
   java RoadTripPlanner
   ```

## 使用方法

1. 输入起始城市（例如，"New York NY"）
2. 输入目的地城市（例如，"Miami FL"）
3. 输入要访问的景点，用逗号分隔（例如，"Hollywood Sign, Liberty Bell"）
4. 查看最优路线和总距离

## 示例测试用例

应用程序包含内置测试用例，可通过修改main方法调用`runTestCases()`来访问：

1. 休斯顿到费城，不经过任何景点
2. 费城到圣安东尼奥，经过好莱坞标志
3. 圣何塞到凤凰城，经过自由钟和千禧公园

## 架构设计

应用程序使用面向对象原则设计：

- `City`：表示图中的城市节点
- `Attraction`：表示景点（兴趣点）
- `RoadNetwork`：管理道路网络图
- `RoutePlanner`：实现Dijkstra算法进行路线规划
- `RoadTripPlanner`：主应用程序类，提供用户界面

## 数据文件

- `attractions.csv`：包含景点名称和位置
- `roads.csv`：包含城市间的连接和距离 