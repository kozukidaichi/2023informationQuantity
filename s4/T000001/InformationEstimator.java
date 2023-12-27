package s4.T000001; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 

import java.lang.*;
import s4.specification.*;

/*
ここだけ変更
*/

public class InformationEstimator implements InformationEstimatorInterface {
    static boolean debugMode = false;
    // Code to test, *warning: This code is slow, and it lacks the required test
    byte[] myTarget; // data to compute its information quantity
    byte[] mySpace; // Sample space to compute the probability
    FrequencerInterface myFrequencer; // Object for counting frequency

    int[] DAST;
    int[] SA;

    private void showVariables() {
        for (int i = 0; i < mySpace.length; i++) {
            System.out.write(mySpace[i]);
        }
        System.out.write(' ');
        for (int i = 0; i < myTarget.length; i++) {
            System.out.write(myTarget[i]);
        }
        System.out.write(' ');
    }

    byte[] subBytes(byte[] x, int start, int end) {
        // corresponding to substring of String for byte[],
        // It is not implement in class library because internal structure of byte[]
        // requires copy.
        byte[] result = new byte[end - start];
        for (int i = 0; i < end - start; i++) {
            result[i] = x[start + i];
        }
        ;
        return result;
    }

    // f: information quantity for a count, -log2(count/sizeof(space))
    double f(int freq) {
        return -Math.log10((double) freq / (double) mySpace.length) / Math.log10((double) 2.0);
    }

    @Override
    public void setTarget(byte[] target) {
        myTarget = target;
    }

    @Override
    public void setSpace(byte[] space) {
        mySpace = space;
        SA = new int[space.length + 1];
        int dast_size = Math.max(space.length << 1, (space[SA[space.length]] - space[SA[1]])) + space.length;
        DAST = new int[dast_size << 2];
        SuffixArray.buildSuffixArray(SA, space, space.length);
        DoubleArraySuffixTree.buildSuffixTree(DAST, dast_size, space, space.length, SA);
    }

    @Override
    public double estimation() {
        double[] info_list = new double[myTarget.length];
        double log_space_length = Math.log(mySpace.length) / Math.log(2.0);

        int i = myTarget.length - 1;
        int first = mySpace[SA[1]];

        info_list[i] = 0.0;

        double value, new_value;
        int state, base;

        int sp, ep;
        int sa_sp, sa_ep;
        int offset, offset_max;

        int pattern;

        for (;;) {
            value = Double.MAX_VALUE;
            
            state = -1;
            base = -first;

            offset = 0;
            sp = 1;
            ep = mySpace.length + 1;

            pattern = myTarget[i];
            if (pattern < first) return Double.MAX_VALUE;
            do {
                int node_index = (base + pattern) << 2;
                if (DAST[node_index] != state) break;
                
                state = base + pattern;
                base = DAST[node_index + 1];
                sp = DAST[node_index + 2];
                ep = DAST[node_index + 3];

                sa_sp = SA[sp];
                sa_ep = SA[ep - 1];

                /* Suffix Treeの枝をたどる */
                offset_max = mySpace.length - ((sa_sp < sa_ep) ? sa_ep : sa_sp); //textの終端
                if (offset_max > myTarget.length - i) offset_max = myTarget.length - i; //sampleの終端
                
                do { //patternとsa[sp],SA[ep-1]の文字が一致している間offsetを増加させる
                    ++offset;
                    if (offset >= offset_max) break;
                    pattern = myTarget[i + offset];
                } while(pattern == mySpace[sa_sp + offset] && pattern == mySpace[sa_ep + offset]);

            /* 情報量計算 */
                new_value = log_space_length - (Math.log((double) ep - sp)/Math.log(2.0)) + info_list[i + offset - 1];
                if (new_value < value) {
                    value = new_value;
                }
            } while (i + offset < myTarget.length && base + pattern >= 0 && mySpace[sa_sp + offset] != mySpace[sa_ep + offset]);

            if (i == 0) {
                return value;
            }
    
            /* info_list[--i] = value */
            /* info_list[i+1...sample_size - 2]の値がvalueより大きいときはvalueに更新 */
            /* Suffix Treeの枝の部分は参照されない。枝の部分と参照される部分の頻度は同じため、info_list[n] >= info_list[n + 1]となるようにinfo_list[i+1]以降を更新する。 */
            int j = --i;
            do {
                info_list[j] = value;
            } while (info_list[++j] > value);
        }
    }

    public static void main(String[] args) {
        InformationEstimator myObject;
        double value;
        debugMode = true;
        myObject = new InformationEstimator();
        myObject.setSpace("3210321001230123".getBytes());
        myObject.setTarget("0".getBytes());
        value = myObject.estimation();
        System.out.println(value);
        myObject.setTarget("01".getBytes());
        value = myObject.estimation();
        System.out.println(value);
        myObject.setTarget("0123".getBytes());
        value = myObject.estimation();
        System.out.println(value);
        myObject.setTarget("00".getBytes());
        value = myObject.estimation();
        System.out.println(value);
    }
}
