import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 排序算法评估程序 - 比较插入排序、快速排序和归并排序的性能
 */
public class SortingAlgorithmEvaluation {
    
    public static void main(String[] args) {
        // 定义数据集文件路径
        String[] datasets = {
            "CW3_Data_Files/1000places_sorted.csv",
            "CW3_Data_Files/1000places_random.csv",
            "CW3_Data_Files/10000places_sorted.csv",
            "CW3_Data_Files/10000places_random.csv"
        };
        
        // 创建结果表格标题
        System.out.println("| 数据集 | 插入排序 (ns/ms) | 快速排序 (ns/ms) | 归并排序 (ns/ms) |");
        System.out.println("|--------|-----------------|-----------------|-----------------|");
        
        // 对每个数据集运行测试
        for (String dataset : datasets) {
            // 读取数据
            List<String> data = readData(dataset);
            if (data.isEmpty()) {
                System.out.println("| " + dataset + " | 读取失败 | 读取失败 | 读取失败 |");
                continue;
            }
            
            // 测试三种排序算法
            String datasetName = dataset.substring(dataset.lastIndexOf("/") + 1);
            
            // 插入排序
            List<String> dataCopy1 = new ArrayList<>(data);
            long insertionTime = testInsertionSort(dataCopy1);
            
            // 快速排序
            List<String> dataCopy2 = new ArrayList<>(data);
            long quickTime = testQuickSort(dataCopy2);
            
            // 归并排序
            List<String> dataCopy3 = new ArrayList<>(data);
            long mergeTime = testMergeSort(dataCopy3);
            
            // 打印结果行
            System.out.printf("| %s | %d ns (%.2f ms) | %d ns (%.2f ms) | %d ns (%.2f ms) |\n",
                    datasetName, 
                    insertionTime, insertionTime / 1000000.0,
                    quickTime, quickTime / 1000000.0,
                    mergeTime, mergeTime / 1000000.0);
        }
        
        // 分析和总结
        System.out.println("\n排序算法性能分析总结：");
        System.out.println("1. 输入顺序对算法性能的影响：");
        System.out.println("   - 插入排序在已排序数据上表现较好，在随机数据上表现较差");
        System.out.println("   - 快速排序在随机数据上表现较好，在已排序数据上可能导致最坏情况");
        System.out.println("   - 归并排序在已排序和随机数据上表现稳定");
        
        System.out.println("\n2. 数据规模对算法性能的影响：");
        System.out.println("   - 插入排序: O(n²)，数据量增大时性能下降明显");
        System.out.println("   - 快速排序: 平均 O(n log n)，大数据集上通常表现良好");
        System.out.println("   - 归并排序: O(n log n)，大数据集上表现稳定");
        
        System.out.println("\n3. 对于含有重复值的数据集：");
        System.out.println("   - 归并排序通常是最佳选择，因为它是稳定的排序算法，能保持相等元素的相对顺序");
        System.out.println("   - 快速排序在重复数据上可能会退化，除非特别优化");
        
        System.out.println("\n4. 在内存受限系统中的选择：");
        System.out.println("   - 归并排序需要额外 O(n) 空间");
        System.out.println("   - 插入排序只需要 O(1) 额外空间，适合内存受限场景");
        System.out.println("   - 快速排序理论上需要 O(log n) 栈空间，但可以优化为原地排序");
    }
    
    /**
     * 从CSV文件中读取数据
     */
    private static List<String> readData(String filePath) {
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // 跳过标题行
            br.readLine();
            while ((line = br.readLine()) != null) {
                data.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("读取文件 " + filePath + " 失败: " + e.getMessage());
        }
        return data;
    }
    
    /**
     * 测试插入排序并返回执行时间（纳秒）
     */
    private static long testInsertionSort(List<String> data) {
        long startTime = System.nanoTime();
        
        insertionSort(data);
        
        long endTime = System.nanoTime();
        return endTime - startTime;
    }
    
    /**
     * 插入排序算法实现
     */
    private static void insertionSort(List<String> data) {
        int n = data.size();
        for (int i = 1; i < n; i++) {
            String key = data.get(i);
            int j = i - 1;
            
            while (j >= 0 && data.get(j).compareTo(key) > 0) {
                data.set(j + 1, data.get(j));
                j = j - 1;
            }
            data.set(j + 1, key);
        }
    }
    
    /**
     * 测试快速排序并返回执行时间（纳秒）
     */
    private static long testQuickSort(List<String> data) {
        long startTime = System.nanoTime();
        
        quickSort(data, 0, data.size() - 1);
        
        long endTime = System.nanoTime();
        return endTime - startTime;
    }
    
    /**
     * 快速排序算法实现
     */
    private static void quickSort(List<String> data, int low, int high) {
        if (low < high) {
            // 获取分区点
            int pi = partition(data, low, high);
            
            // 递归地对分区点两侧进行排序
            quickSort(data, low, pi - 1);
            quickSort(data, pi + 1, high);
        }
    }
    
    /**
     * 快速排序分区函数
     */
    private static int partition(List<String> data, int low, int high) {
        // 选择最右边的元素作为基准
        String pivot = data.get(high);
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (data.get(j).compareTo(pivot) <= 0) {
                i++;
                // 交换 data[i] 和 data[j]
                String temp = data.get(i);
                data.set(i, data.get(j));
                data.set(j, temp);
            }
        }
        
        // 交换 data[i+1] 和 data[high]（基准元素）
        String temp = data.get(i + 1);
        data.set(i + 1, data.get(high));
        data.set(high, temp);
        
        return i + 1;
    }
    
    /**
     * 测试归并排序并返回执行时间（纳秒）
     */
    private static long testMergeSort(List<String> data) {
        long startTime = System.nanoTime();
        
        List<String> result = mergeSort(data);
        // 将排序结果复制回原列表
        for (int i = 0; i < data.size(); i++) {
            data.set(i, result.get(i));
        }
        
        long endTime = System.nanoTime();
        return endTime - startTime;
    }
    
    /**
     * 归并排序算法实现
     */
    private static List<String> mergeSort(List<String> data) {
        int n = data.size();
        if (n <= 1) {
            return data;
        }
        
        // 分割列表
        int mid = n / 2;
        List<String> left = new ArrayList<>(data.subList(0, mid));
        List<String> right = new ArrayList<>(data.subList(mid, n));
        
        // 递归排序
        left = mergeSort(left);
        right = mergeSort(right);
        
        // 合并结果
        return merge(left, right);
    }
    
    /**
     * 归并排序的合并函数
     */
    private static List<String> merge(List<String> left, List<String> right) {
        List<String> result = new ArrayList<>();
        int leftIndex = 0, rightIndex = 0;
        
        // 比较并合并
        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (left.get(leftIndex).compareTo(right.get(rightIndex)) <= 0) {
                result.add(left.get(leftIndex));
                leftIndex++;
            } else {
                result.add(right.get(rightIndex));
                rightIndex++;
            }
        }
        
        // 添加剩余元素
        while (leftIndex < left.size()) {
            result.add(left.get(leftIndex));
            leftIndex++;
        }
        
        while (rightIndex < right.size()) {
            result.add(right.get(rightIndex));
            rightIndex++;
        }
        
        return result;
    }
} 