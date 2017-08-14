import com.tyaer.util.excel.ExcelReader;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by Twin on 2017/5/22.
 */
public class ExcelReader_Test {
    public static void main(String[] args) {
        try {
            // 对读取Excel表格标题测试
            String fileUrl = "App/file/wbuserpriority/用户信息.xlsx";
            ExcelReader excelReader = new ExcelReader();
            String[] title = excelReader.readExcelTitle(fileUrl);
            System.out.println("获得Excel表格的标题:");
            for (String s : title) {
                System.out.print(s + " ");
            }
            System.out.println();
            // 对读取Excel表格内容测试
            InputStream is2 = new FileInputStream("./30个具有道德争议的事件/2015-1-31@环卫工雪天烤火被辞.xls");
            Map<Integer, String> map = excelReader.readExcelContent(fileUrl);
            System.out.println("获得Excel表格的内容:");
            for (int i = 1; i <= map.size(); i++) {
                System.out.println(map.get(i));
            }

        } catch (FileNotFoundException e) {
            System.out.println("未找到指定路径的文件!");
            e.printStackTrace();
        }
    }

    @Test
    public void test1(){
        ExcelReader excelReader = new ExcelReader();
        String fileUrl = "App/file/wbuserpriority/用户信息.xlsx";
        String[][] strings = excelReader.readExceltoTable(fileUrl);
        System.out.println(ArrayUtils.toString(strings));
        int length = strings.length;
        System.out.println(length);
        System.out.println(excelReader.readExcelTitle(fileUrl));
        System.out.println(excelReader.readExcelContent(fileUrl));
    }
}
