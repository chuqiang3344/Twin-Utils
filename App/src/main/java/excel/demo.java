package excel;

import com.tyaer.database.mysql.MySQLHelperSingleton;
import com.tyaer.util.excel.ExcelReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by Twin on 2017/3/17.
 */
public class demo {

    public static void main(String[] args) {
        String fileUrl = "App/file/wbuserpriority/用户信息.xlsx";
        // 对读取Excel表格标题测试
        ExcelReader excelReader = new ExcelReader();
        String[] title = excelReader.readExcelTitle(fileUrl);
        System.out.println("获得Excel表格的标题:");
        for (String s : title) {
            System.out.print(s + " ");
        }
        System.out.println();
        // 对读取Excel表格内容测试
        Map<Integer, String> map = excelReader.readExcelContent(fileUrl);
        System.out.println("获得Excel表格的内容:" + map.size());
        for (int i = 1; i <= map.size(); i++) {
            System.out.println(map.get(i));
        }
    }

    @Test
    public void table() {
//        MySQLHelperSingleton mySQLHelperSingleton = new MySQLHelperSingleton("root", "izhonghong@2016root123", "jdbc:mysql://112.95.227.96/sina_dispute_weibo3?useUnicode=true&amp;characterEncoding=UTF-8");
        MySQLHelperSingleton mySQLHelperSingleton = new MySQLHelperSingleton("root", "izhonghong@2016root123", "jdbc:mysql://119.145.230.3:53306/sina_dispute_weibo?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false");

        ExcelReader excelReader = new ExcelReader();
        String[][] strings = excelReader.readExceltoTable("./file/事件抓取链接2.xls");
        System.out.println(ArrayUtils.toString(strings));
        String event = null;
        String name = null;
        int length = strings.length;
        System.out.println(length);
        ArrayList<ArrayList<Object>> arrayLists = new ArrayList<>();
        for (int i = 1; i < length; i++) {
            ArrayList<Object> objectss = new ArrayList<>();
            String[] string = strings[i];
            if (StringUtils.isNotBlank(string[0])) {
                event = string[0];
            }
            if (StringUtils.isNotBlank(string[1])) {
                name = string[1];
            }
            String url = string[2];
            if (event.contains("王宝强离婚案")) {
                objectss.add("31");
            } else if (event.contains("罗一笑事件")) {
                objectss.add("32");
            }
            if (name.contains("新浪搜索-热门微博")) {
                objectss.add(name);
                objectss.add(url);
                System.out.println(objectss);
                arrayLists.add(objectss);
            }
        }
        String sql = "insert into dispute_event_origin(event_id,username,url) values(?,?,?)";
        System.out.println(arrayLists.size());
        mySQLHelperSingleton.batchUpdateByPreparedStatement(sql, arrayLists);
    }

    @Test
    public void table2() {
//        MySQLHelperSingleton mySQLHelperSingleton = new MySQLHelperSingleton("root", "izhonghong@2016root123", "jdbc:mysql://112.95.227.96/sina_dispute_weibo3?useUnicode=true&amp;characterEncoding=UTF-8");
        String url1 = "jdbc:mysql://119.145.230.3:53306/sina_dispute_weibo_hgsd?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false";
        MySQLHelperSingleton mySQLHelperSingleton = new MySQLHelperSingleton("root", "izhonghong@2016root123", url1);

        ExcelReader excelReader = new ExcelReader();
        String[][] strings = excelReader.readExceltoTable("./file/韩国萨德事件.xlsx");
        System.out.println(ArrayUtils.toString(strings));
        String event = null;
        String name = null;
        int length = strings.length;
        System.out.println(length);
        ArrayList<ArrayList<Object>> arrayLists = new ArrayList<>();
        for (int i = 1; i < length; i++) {
            ArrayList<Object> objectss = new ArrayList<>();
            String[] string = strings[i];
            if (StringUtils.isNotBlank(string[0])) {
                event = string[0];
            }
            if (StringUtils.isNotBlank(string[1])) {
                name = string[1];
            }
            String url = string[2];
//            if(name.contains("新浪搜索-热门微博")){
            objectss.add("62");
            objectss.add(name);
            objectss.add(url);
            System.out.println(objectss);
            arrayLists.add(objectss);
//            }
        }
        String sql = "insert into dispute_event_origin(event_id,username,url) values(?,?,?)";
        System.out.println(arrayLists.size());
        mySQLHelperSingleton.batchUpdateByPreparedStatement(sql, arrayLists);
    }

    @Test
    public void table2add() {
//        MySQLHelperSingleton mySQLHelperSingleton = new MySQLHelperSingleton("root", "izhonghong@2016root123", "jdbc:mysql://112.95.227.96/sina_dispute_weibo3?useUnicode=true&amp;characterEncoding=UTF-8");
        String url1 = "jdbc:mysql://119.145.230.3:53306/sina_dispute_weibo_lyxpl?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false";
        MySQLHelperSingleton mySQLHelperSingleton = new MySQLHelperSingleton("root", "izhonghong@2016root123", url1);

        ExcelReader excelReader = new ExcelReader();
        String[][] strings = excelReader.readExceltoTable("./file/罗一笑评论1129-1215.xlsx");
        System.out.println(ArrayUtils.toString(strings));
        int length = strings.length;
        System.out.println(length);
        ArrayList<ArrayList<Object>> arrayLists = new ArrayList<>();
        for (int i = 1; i < length; i++) {
            ArrayList<Object> objectss = new ArrayList<>();
            String[] string = strings[i];
            String url = string[0];
//            if(name.contains("新浪搜索-热门微博")){
            objectss.add("51");
            objectss.add("罗一笑评论");
            objectss.add(url);
            System.out.println(objectss);
            arrayLists.add(objectss);
//            }
        }
        String sql = "insert into dispute_event_origin(event_id,username,url) values(?,?,?)";
        System.out.println(arrayLists.size());
        mySQLHelperSingleton.batchUpdateByPreparedStatement(sql, arrayLists);
    }

    @Test
    public void table10() {
        String url1 = "jdbc:mysql://119.145.230.3:53306/sina_dispute_weibo_10?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false";
        MySQLHelperSingleton mySQLHelperSingleton = new MySQLHelperSingleton("root", "izhonghong@2016root123", url1);
        File[] files = new File("./file/10").listFiles();
        int startId = 52;
        for (File file : files) {
            ExcelReader excelReader = new ExcelReader();
            String[][] strings = excelReader.readExceltoTable(file.getAbsolutePath());
            System.out.println(ArrayUtils.toString(strings));
            String event = null;
            String name = null;
            int length = strings.length;
            System.out.println(length);
            ArrayList<ArrayList<Object>> arrayLists = new ArrayList<>();
            for (int i = 1; i < length; i++) {
                ArrayList<Object> objectss = new ArrayList<>();
                String[] string = strings[i];
                if (StringUtils.isNotBlank(string[0])) {
                    event = string[0];
                }
                if (i == 1) {
                    String sql = "insert into dispute_event(id,date,event) values(?,?,?)";
                    ArrayList<Object> objects = new ArrayList<>();
                    objects.add(startId);
                    objects.add(new Timestamp(Calendar.getInstance().getTimeInMillis()));
                    objects.add(event);
                    mySQLHelperSingleton.updateByPreparedStatement(sql, objects);
                }
                if (StringUtils.isNotBlank(string[1])) {
                    name = string[1];
                }
                String url = string[2];
//            if(name.contains("新浪搜索-热门微博")){
                objectss.add(startId);
                objectss.add(name);
                objectss.add(url);
                System.out.println(objectss);
                arrayLists.add(objectss);
//            }
            }
            String sql = "insert into dispute_event_origin(event_id,username,url) values(?,?,?)";
            System.out.println(arrayLists.size());
            mySQLHelperSingleton.batchUpdateByPreparedStatement(sql, arrayLists);
            startId++;
        }
    }

}
