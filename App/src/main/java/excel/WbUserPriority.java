package excel;

import com.tyaer.util.excel.ExcelReader;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Twin on 2017/7/6.
 */
public class WbUserPriority {

    public static void main(String[] args) {
        ExcelReader excelReader = new ExcelReader();
        String fileUrl = "App/file/wbuserpriority/用户信息.xlsx";
        String[][] strings = excelReader.readExceltoTable(fileUrl);
        System.out.println(ArrayUtils.toString(strings));
        int length = strings.length;
        System.out.println(length);
        for (String[] string : strings) {

        }
    }
}
