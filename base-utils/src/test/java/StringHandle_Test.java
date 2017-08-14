import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Twin on 2017/5/17.
 */
public class StringHandle_Test {

    @Test
    public void one(){
        Pattern pattern = Pattern.compile("[a-z]");
        Matcher matcher = pattern.matcher("/liuqiangdong");
        int groupCount = matcher.groupCount();
        List<String> result = new ArrayList<String>();
        while (matcher.find()) {
            System.out.println(matcher.group(0));
//            String group = matcher.group(1);
//            result.add(group);
        }
        System.out.println(result);
    }

    @Test
    public void json(){
        String weiboDatas = "{\"type\":\"article_topic\",\"data\":"+123+"}";
        long  start= Calendar.getInstance().getTimeInMillis();
        System.out.println(weiboDatas);
        Object parse = JSON.parse(weiboDatas);
        System.out.println(parse);
        String string = parse.toString();
        System.out.println(string);
    }

}
