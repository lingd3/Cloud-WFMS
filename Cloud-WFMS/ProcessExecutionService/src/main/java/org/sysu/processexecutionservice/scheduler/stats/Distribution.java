package org.sysu.processexecutionservice.scheduler.stats;

/**
 * Accumulator of statistics about a distribution of observer values that are produced incrementally
 * 也就是用分布的方式来统计数据，而不是一个个记录下来
 */
public class Distribution {
    private long numValues; //样本个数
    private double sumValues; //样本之和
    private double sumSquareValues; //分布的平方和；离散程度
    private double minValue; //最小值
    private double maxValue; //最大值

    public Distribution() {
        numValues = 0L;
        sumValues = 0.0;
        sumSquareValues = 0.0;
        minValue = 0.0;
        maxValue = 0.0;
    }

    /**
     * Accumulating new values
     * @param val
     */
    public void noteValue(double val) {
        numValues++;
        sumValues += val;
        sumSquareValues += val * val;
        if(numValues == 1) {
            minValue = val;
            maxValue = val;
        } else if(val < minValue) {
            minValue = val;
        } else if(val > maxValue) {
            maxValue = val;
        }
    }

    public void clear() {
        numValues = 0L;
        sumValues = 0.0;
        sumSquareValues = 0.0;
        maxValue = 0.0;
        minValue = 0.0;
    }

    public long getNumValues() {
        return numValues;
    }

    public double getMean() {
        if(numValues < 1) {
            return 0.0;
        } else {
            return sumValues / numValues;
        }
    }

    /**
     * 方差等于平方和的均值减去均值的平方
     * @return
     */
    public double getVariance() {
        if (numValues < 2) {
            return 0.0;
        } else if(sumValues == 0.0) {
            return 0.0;
        } else {
            double mean = getMean();
            return (sumSquareValues / numValues) - mean * mean;
        }
    }

    /**
     * 标准差
     * @return
     */
    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getSumValues() {
        return sumValues;
    }

    public double getMinValue() {
        return minValue;
    }

    public void add(Distribution anotherDistribution) {
        if (anotherDistribution != null) {
            numValues += anotherDistribution.numValues;
            sumValues += anotherDistribution.sumValues;
            sumSquareValues += anotherDistribution.sumSquareValues;
            minValue = (minValue < anotherDistribution.minValue) ? minValue
                    : anotherDistribution.minValue;
            maxValue = (maxValue > anotherDistribution.maxValue) ? maxValue
                    : anotherDistribution.maxValue;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("{Distribution:")
                .append("N=").append(getNumValues())
                .append(": ").append(getMinValue())
                .append("..").append(getSumValues())
                .append("..").append(getMean())
                .append("..").append(getMaxValue())
                .append("}")
                .toString();
    }
}
