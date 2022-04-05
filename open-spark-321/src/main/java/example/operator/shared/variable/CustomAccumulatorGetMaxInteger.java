package example.operator.shared.variable;

import org.apache.spark.util.AccumulatorV2;

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
        max = currentMaxValue = Math.max(this.max, v);
    }

    @Override
    public void merge(AccumulatorV2<Integer, Integer> other) {

    }

    @Override
    public Integer value() {
        return null;
    }
}
