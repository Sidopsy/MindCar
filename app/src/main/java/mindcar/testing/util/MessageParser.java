package mindcar.testing.util;

import android.os.Message;

import com.neurosky.thinkgear.TGEegPower;

import mindcar.testing.objects.Eeg;
import mindcar.testing.objects.Pattern;

/**
 * this class parses msgs and updates the smart car object
 * Created by Mattias & Sarah on 3/7/16.
 */
public class MessageParser {

    /**
     * Saves incomming raw values to a eeg object.
     *
     * @param msg
     * @param eeg
     */
    public static void parseEeg(Message msg, Eeg eeg) {
        TGEegPower tgEegPower = (TGEegPower) msg.obj;
        eeg.setDelta(tgEegPower.delta);
        eeg.setTheta(tgEegPower.theta);
        eeg.setLowAlpha(tgEegPower.lowAlpha);
        eeg.setHighAlpha(tgEegPower.highAlpha);
        eeg.setLowBeta(tgEegPower.lowBeta);
        eeg.setHighAlpha(tgEegPower.highBeta);
        eeg.setLowGamma(tgEegPower.lowGamma);
        eeg.setHighGamma(tgEegPower.midGamma);
    }


    /**
     * Assign values from raw data to the correct eeg frequency
     *
     * @param msg
     * @param eeg
     */
    public static void parseRawData(Message msg, Eeg eeg) {
        int value = msg.arg1;
        if (value > 0 && value <= 3) {
            eeg.setDelta(value);
        } else if (value >= 4 && value <= 7) {
            eeg.setTheta(value);
        } else if (value >= 8 && value <= 9) {
            eeg.setLowAlpha(value);
        } else if (value >= 10 && value <= 12) {
            eeg.setHighAlpha(value);
        } else if (value >= 13 && value <= 17) {
            eeg.setLowBeta(value);
        } else if (value >= 18 && value <= 30) {
            eeg.setHighBeta(value);
        } else if (value >= 31 && value <= 40) {
            eeg.setLowGamma(value);
        } else if (value >= 41 && value <= 50) {
            eeg.setHighGamma(value);
        } else {
            ;
        }

    }

    public static String toString(double[] doubles){
        StringBuilder str = new StringBuilder();
        for(double d: doubles){
            str.append(d);
        }
        return str.toString();
    }

}