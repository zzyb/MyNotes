package example.operator.transformations;

import java.io.Serializable;
import java.util.Comparator;

public class TopComparator implements Comparator<String>, Serializable {
    @Override
    public int compare(String o1, String o2) {
        if (o1.length() > o2.length()) {
            return 1;
        } else if (o1.length() < o2.length()) {
            return -1;
        } else {
            return 0;
        }
    }
}
