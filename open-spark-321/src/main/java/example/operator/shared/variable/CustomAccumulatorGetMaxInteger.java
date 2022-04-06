package example.operator.shared.variable;

import org.apache.spark.util.AccumulatorV2;

/**
 * 自定义累加器：这里求出获取到的最大值。
 */
public class CustomAccumulatorGetMaxInteger extends AccumulatorV2<Integer, Integer> {

    private int max = Integer.MIN_VALUE;

    @Override
    public boolean isZero() {
        return max == Integer.MIN_VALUE;
    }

    @Override
    public AccumulatorV2<Integer, Integer> copy() {
        CustomAccumulatorGetMaxInteger copyAcc = new CustomAccumulatorGetMaxInteger();
        copyAcc.max = this.max;
        return copyAcc;
    }

    @Override
    public void reset() {
        max = Integer.MIN_VALUE;
    }

    @Override
    public void add(Integer v) {
        //
        max = Math.max(this.max, v);
    }

    @Override
    public void merge(AccumulatorV2<Integer, Integer> other) {
        max = Math.max(other.value(), max);
    }

    @Override
    public Integer value() {
        return max;
    }
}
