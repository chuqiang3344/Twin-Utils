import org.junit.Test;

/**
 * Created by Twin on 2017/5/8.
 */
public class Int_Test {
    public static void main(String[] args) {
        Integer i = null;
        if (i < 1) {
            System.out.println(111);
        }
    }

    @Test
    public void t1() {
        int second = 100;
        double decay = Math.exp(-(0.000026) * second);//每小时衰减9%,每天衰减90%,每小时衰减9%,每天衰减90%，返回 e 的指定次幂。
        System.out.println(decay);
        System.out.println(Math.log(200000));
    }

    @Test
    public void t2() {
//        Object xx="123";
        Object xx = null;
        int xx1 = (int) xx;
        System.out.println(xx1);
    }
}
