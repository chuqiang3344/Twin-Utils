import java.util.ArrayList;

/**
 * Created by Twin on 2017/5/12.
 */
public class List_Test {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        System.out.println(list);
        int n = 1;

        int j;
        for (int i = 0; i < list.size(); ) {
            j = i + 3;
            if (j >= list.size()) {
                j = list.size();
            }
            System.out.println(i+" "+j);
            System.out.println(n + ":" + list.subList(i, j));
            i = j;
            n++;
        }

    }
}
