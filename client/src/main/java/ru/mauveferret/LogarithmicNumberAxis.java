package ru.mauveferret;
import java.util.ArrayList;
import java.util.List;


import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.chart.ValueAxis;
import javafx.util.Duration;
/* comment by mauveferret:
in order to edit *.fxml data with log plots you should firstly
import this class in Scene Builder.
You can do it by importing random *.jar, which contain this class
 */
/**
 * A logarithmic axis implementation for JavaFX 2 charts<br>
 * <br>
 *
 * @author Kevin Senechal
 *
 */
public class LogarithmicNumberAxis extends ValueAxis<Number> {

    /**
     * The time of animation in ms
     */
    private static final double ANIMATION_TIME = 2000;
    private final Timeline lowerRangeTimeline = new Timeline();
    private final Timeline upperRangeTimeline = new Timeline();

    private final DoubleProperty logUpperBound = new SimpleDoubleProperty();
    private final DoubleProperty logLowerBound = new SimpleDoubleProperty();

    public LogarithmicNumberAxis() {
        super(0.000000001,1000);
        bindLogBoundsToDefaultBounds();
    }

    public LogarithmicNumberAxis(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
        try {
            validateBounds(lowerBound, upperBound);
            bindLogBoundsToDefaultBounds();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Bind our logarithmic bounds with the super class bounds, consider the base 10 logarithmic scale.
     */
    private void bindLogBoundsToDefaultBounds() {
        logLowerBound.bind(new DoubleBinding() {

            {
                super.bind(lowerBoundProperty());
            }

            @Override
            protected double computeValue() {
                return Math.log10(lowerBoundProperty().get());
            }
        });
        logUpperBound.bind(new DoubleBinding() {

            {
                super.bind(upperBoundProperty());
            }

            @Override
            protected double computeValue() {
                return Math.log10(upperBoundProperty().get());
            }
        });
    }

    /**
     * Validate the bounds by throwing an exception if the values are not conform to the mathematics log interval:
     * ]0,Double.MAX_VALUE]
     *
     * @param lowerBound
     * @param upperBound
     * @throws //IllegalLogarithmicRangeException
     */
    private void validateBounds(double lowerBound, double upperBound) throws Exception {
        if (lowerBound < 0 || upperBound < 0 || lowerBound > upperBound) {
            throw new Exception(
                    "The logarithmic range should be include to ]0,Double.MAX_VALUE] and the lowerBound should be less than the upperBound");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Number> calculateMinorTickMarks() {
        Number[] range = getRange();
        List<Number> minorTickMarksPositions = new ArrayList<Number>();
        if (range != null) {
            Number lowerBound = range[0];
            Number upperBound = range[1];
            //fix by mauveferret: i=0 -> i=logLowerBound for small values lower than 1 (like 0.001)
            double logLowerBound = Math.log10(lowerBound.doubleValue());
            double logUpperBound = Math.log10(upperBound.doubleValue());
            int minorTickMarkCount = getMinorTickCount();

            for (double i = logLowerBound; i <= logUpperBound; i += 1) {
                //for (double j = 0; j <= 9; j += (1. / minorTickMarkCount)) {
                for (double j = 0; j <= 9; j +=1) {
                double value = j * Math.pow(10, i);
                    minorTickMarksPositions.add(value);
                }
            }
        }
        return minorTickMarksPositions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Number> calculateTickValues(double length, Object range) {
        List<Number> tickPositions = new ArrayList<Number>();
        if (range != null) {
            Number lowerBound = ((Number[]) range)[0];
            Number upperBound = ((Number[]) range)[1];
            double logLowerBound = Math.log10(lowerBound.doubleValue());
            double logUpperBound = Math.log10(upperBound.doubleValue());
            //fix by mauveferret: i=0 -> i=logLowerBound for small values lower than 1 (like 0.001)
            for (double i = logLowerBound; i <= logUpperBound; i += 1) {
               /* for (double j = 1; j <= 9; j++) {
                    double value = j * Math.pow(10, i);

                */
                {
                    double value =  Math.pow(10, i);
                    tickPositions.add(value);
                }
            }
        }
        return tickPositions;
    }

    @Override
    protected Number[] getRange() {
        return new Number[] { lowerBoundProperty().get(), upperBoundProperty().get() };
    }

    @Override
    protected String getTickMarkLabel(Number value) {
       /* NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumIntegerDigits(6);
        formatter.setMinimumIntegerDigits(1);
        return formatter.format(value);
        */
        return String.format("%6.0e",(double) value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setRange(Object range, boolean animate) {
        if (range != null) {
            Number lowerBound = ((Number[]) range)[0];
            Number upperBound = ((Number[]) range)[1];
            try {
                validateBounds(lowerBound.doubleValue(), upperBound.doubleValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (animate) {
                try {
                    lowerRangeTimeline.getKeyFrames().clear();
                    upperRangeTimeline.getKeyFrames().clear();

                    lowerRangeTimeline.getKeyFrames()
                            .addAll(new KeyFrame(Duration.ZERO, new KeyValue(lowerBoundProperty(), lowerBoundProperty()
                                            .get())),
                                    new KeyFrame(new Duration(ANIMATION_TIME), new KeyValue(lowerBoundProperty(),
                                            lowerBound.doubleValue())));

                    upperRangeTimeline.getKeyFrames()
                            .addAll(new KeyFrame(Duration.ZERO, new KeyValue(upperBoundProperty(), upperBoundProperty()
                                            .get())),
                                    new KeyFrame(new Duration(ANIMATION_TIME), new KeyValue(upperBoundProperty(),
                                            upperBound.doubleValue())));
                    lowerRangeTimeline.play();
                    upperRangeTimeline.play();
                } catch (Exception e) {
                    lowerBoundProperty().set(lowerBound.doubleValue());
                    upperBoundProperty().set(upperBound.doubleValue());
                }
            }
            lowerBoundProperty().set(lowerBound.doubleValue());
            upperBoundProperty().set(upperBound.doubleValue());
        }
    }

    @Override
    public Number getValueForDisplay(double displayPosition) {
        double delta = logUpperBound.get() - logLowerBound.get();
        if (getSide().isVertical()) {
            return Math.pow(10, (((displayPosition - getHeight()) / -getHeight()) * delta) + logLowerBound.get());
        } else {
            return Math.pow(10, (((displayPosition / getWidth()) * delta) + logLowerBound.get()));
        }
    }

    @Override
    public double getDisplayPosition(Number value) {
        double delta = logUpperBound.get() - logLowerBound.get();
        double deltaV = Math.log10(value.doubleValue()) - logLowerBound.get();
        if (getSide().isVertical()) {
            return (1. - ((deltaV) / delta)) * getHeight();
        } else {
            return ((deltaV) / delta) * getWidth();
        }
    }
}