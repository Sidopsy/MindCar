package mindcar.testing.objects;

import mindcar.testing.util.DatabaseAccess;

/**
 * Object storing values from a Neurosky Mindwave Mobile
 * Created by Mattias Landkvist & Nikos Sasopoulos on 3/2/16.
 */
public class Eeg {
    private int attention;
    private int meditation;
    private int blink;
    public int delta;
    public int theta;
    public int lowAlpha;
    public int highAlpha;
    public int lowBeta;
    public int highBeta;
    public int lowGamma;
    public int highGamma;
    public double[] arr;
    //TODO - integrate patterns?

    /**
     * Constructs a basic Eeg with all of the values set to zero (0)
     */
    public Eeg() {
        attention = 0;
        meditation = 0;
        blink = 0;
    }

    /**
     * @return the attention value
     */
    public int getAttention() {
        return attention;
    }

    public double[] getArray() {
        return arr;
    }

    /**
     * @return the meditation value
     */
    public int getMeditation() {
        return meditation;
    }

    /**
     * @return the blink value
     */
    public int getBlink() {
        return blink;
    }

    /**
     * Set the attention value of the instance
     *
     * @param attention
     */
    public void setAttention(int attention) {
        this.attention = attention;
    }

    /**
     * Set the meditation value of the instance
     *
     * @param meditation
     */
    public void setMeditation(int meditation) {
        this.meditation = meditation;
    }

    /**
     * Set the blink value of the instance
     *
     * @param blink
     */
    public void setBlink(int blink) {
        this.blink = blink;
    }

    public void setDelta(int delta) {
        this.arr[0] = delta;
    }

    public void setTheta(int theta) {
        this.arr[1] = theta;
    }

    public void setLowAlpha(int lowAlpha) {
        this.arr[2] = lowAlpha;
    }

    public void setHighAlpha(int highAlpha) {
        this.arr[3] = highAlpha;
    }

    public void setLowBeta(int lowBeta) {
        this.arr[4] = lowBeta;
    }

    public void setHighBeta(int highBeta) {
        this.arr[5] = highBeta;
    }

    public void setLowGamma(int lowGamma) {
        this.arr[6] = lowGamma;
    }

    public void setHighGamma(int highGamma) {
        this.arr[7] = highGamma;
    }



    public double[] toDoubleArray(){
        return new double[]{Double.valueOf(delta), Double.valueOf(theta), Double.valueOf(lowAlpha),
                    Double.valueOf(highAlpha), Double.valueOf(lowBeta), Double.valueOf(highBeta),
                    Double.valueOf(lowGamma), Double.valueOf(highGamma)};
    }
}