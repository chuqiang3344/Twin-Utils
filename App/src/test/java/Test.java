/**
 * Created by Twin on 2017/4/22.
 */
public class Test {
    public static void main(String[] args) {
        Double n= Double.valueOf(1116);
        for (Long i = 1l; i <= 100; i++) {
            Double x = n / i;
            System.out.println(i+" "+x);
        }
    }
}
