package s4.T000001; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 

import java.lang.*;
import java.util.random.RandomGenerator;

import javax.swing.plaf.TreeUI;

import s4.specification.*;

public class InformationEstimator implements InformationEstimatorInterface {
    static boolean debugMode = false;
    // Code to test, *warning: This code is slow, and it lacks the required test
    byte[] myTarget; // data to compute its information quantity
    byte[] mySpace; // Sample space to compute the probability

    int[] DAST;
    int[] SA;

    int[] replacedTarget;
    int[] table = new int[256];

    boolean debug = false;

    @Override
    public void setTarget(byte[] target) {
        if (target == null || target.length == 0){
            myTarget = null;
            return;
        }
        myTarget = target;
        if (mySpace != null) {
            replacedTarget = new int[myTarget.length];
            for (int i = 0, len = myTarget.length; i < len; ++i) {
                int replaced = table[((int) myTarget[i]) & 0xFF];
                if (replaced < 0) {
                    replacedTarget = null;
                    return;
                }
                replacedTarget[i] = replaced;
            }
        }
    }

    @Override
    public void setSpace(byte[] space) {
        if (space == null || space.length == 0){
            mySpace = null;
            return;
        }
        mySpace = space;
        SA = new int[space.length + 1];
        int dast_size = 256 + (space.length << 1) + space.length;
        DAST = new int[dast_size << 2];
        SuffixArray.buildSuffixArray(SA, space, space.length);
        DoubleArraySuffixTree.buildSuffixTreeReplace(DAST, dast_size, space, space.length, SA, table);
        if (myTarget != null) {
            replacedTarget = new int[myTarget.length];
            for (int i = 0, len = myTarget.length; i < len; ++i) {
                int replaced = table[((int) myTarget[i]) & 0xFF];
                if (replaced < 0) {
                    replacedTarget = null;
                    return;
                }
                replacedTarget[i] = replaced;
            }
        }
    }

    private double estimation_sa() {
        if (myTarget == null)
            return 0.0;
        if (mySpace == null || replacedTarget == null)
            return Double.MAX_VALUE;

        double[] info_list = new double[myTarget.length];
        double log_space_length = Math.log(mySpace.length);

        double value, new_value;
        int sp, ep, tmp;
        int j, offset;

        int i = myTarget.length - 1;
        info_list[i] = 0.0;

        double cache;

        int pattern;
        int mid;

        for (;;) {
            value = Double.MAX_VALUE;
            sp = 1;
            ep = mySpace.length + 1;
            tmp = 0;
            cache = log_space_length;

            for (offset = 0, j = i; j < myTarget.length; ++offset, ++j) {
                pattern = myTarget[j];
                if (SA[sp] + offset >= mySpace.length || pattern - mySpace[SA[sp] + offset] > 0) {
                    tmp = ep;
                    do {
                        mid = sp + ((tmp - sp) >> 1);
                        if (SA[mid] + offset >= mySpace.length || pattern - mySpace[SA[mid] + offset] <= 0) {
                            tmp = mid;
                        } else {
                            sp = mid;
                        }
                    } while (sp - tmp + 1 < 0);
                    sp = tmp;
                }

                if (SA[ep - 1] + offset >= mySpace.length || pattern - mySpace[SA[ep - 1] + offset] < 0) {
                    tmp = sp;
                    do {
                        mid = tmp + ((ep - tmp) >> 1);
                        // System.out.println(ep + " " + tmp + " " + (mid - 1) + " " + SA[mid - 1] + " " + offset);
                        if (SA[mid - 1] + offset >= mySpace.length || pattern - mySpace[SA[mid - 1] + offset] >= 0) {
                            tmp = mid;
                        } else {
                            ep = mid;
                        }
                    } while (tmp - ep + 1 < 0);
                    ep = tmp;
                }
                
                if(debug) System.out.println((char)myTarget[j] + ":" + (ep - sp));


                if (tmp > 0) {
                    if (sp == ep) break;
                    tmp = 0;
                    cache = -Math.log((double)(ep - sp));
                }
                new_value = cache + info_list[j];
                if (new_value < value) {
                    value = new_value;
                }
            }
            value += log_space_length;

            if (i == 0) {
                return value / Math.log(2.0);
            }
            info_list[--i] = value;
        }
    }

    @Override
    public double estimation() {
        if (myTarget == null)
            return 0.0;
        if (mySpace == null || replacedTarget == null)
            return Double.MAX_VALUE;

        double[] info_list = new double[replacedTarget.length];
        double log_space_length = Math.log(mySpace.length);

        int i = replacedTarget.length - 1;

        info_list[i] = 0.0;

        double value, new_value;
        int state, base;

        int sp, ep;
        int sa_sp, sa_ep;
        int offset, offset_max;

        int pattern;


        StringBuilder sb = new StringBuilder();

        for (;;) {
            value = Double.MAX_VALUE;

            state = -1;
            base = -0;

            offset = 0;
            sp = 1;
            ep = mySpace.length + 1;

            pattern = replacedTarget[i];
            sb.setLength(0);
            sb.append((char)myTarget[i]);
            do {
                int node_index = (base + pattern) << 2;
                if (DAST[node_index] != state){
                    break;
                }

                state = base + pattern;
                base = DAST[node_index + 1];
                sp = DAST[node_index + 2];
                ep = DAST[node_index + 3];

                sa_sp = SA[sp];
                sa_ep = SA[ep - 1];

                /* Suffix Treeの枝をたどる */
                offset_max = mySpace.length - ((sa_sp < sa_ep) ? sa_ep : sa_sp); // textの終端

                if (offset + i + 1 < replacedTarget.length) {
                    do { // patternとsa[sp],SA[ep-1]の文字が一致している間offsetを増加させる
                        ++offset;
                        if (offset + i >= replacedTarget.length) break;
                        pattern = myTarget[i + offset];
                        sb.append((char)myTarget[i + offset]);
                    } while (offset < offset_max && pattern == mySpace[sa_sp + offset] && pattern == mySpace[sa_ep + offset]);
                    pattern = table[pattern & 0xFF];
                } else {
                    ++offset;
                }
                
                // if(debug) System.out.println(Integer.toHexString(pattern) + ":" + offset + ":" + (ep - sp));

                /* 情報量計算 */
                new_value = log_space_length - (Math.log((double) ep - sp)) + info_list[i + offset - 1];

                if (new_value < value) {
                    value = new_value;
                }

                // if(debug) System.out.println(i + offset < replacedTarget.length && base + pattern >= 0 && (offset >= offset_max || mySpace[sa_sp + offset] != mySpace[sa_ep + offset]));
            } while (i + offset < replacedTarget.length && base + pattern >= 0
                    && (offset >= offset_max || mySpace[sa_sp + offset] != mySpace[sa_ep + offset]));
            if (i == 0) {
                return value / Math.log(2.0);
            }

            /* info_list[--i] = value */
            /* info_list[i+1...sample_size - 2]の値がvalueより大きいときはvalueに更新 */
            /*
             * Suffix Treeの枝の部分は参照されない。枝の部分と参照される部分の頻度は同じため、info_list[n] >= info_list[n +
             * 1]となるようにinfo_list[i+1]以降を更新する。
             */
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
        // myObject.setSpace("3210321001230123".getBytes());
        // Debug.printSuffixTree(myObject.DAST, myObject.SA, myObject.mySpace);
        // myObject.setTarget("0".getBytes());
        // value = myObject.estimation();
        // System.out.println(value);
        // myObject.setTarget("01".getBytes());
        // value = myObject.estimation();
        // System.out.println(value);
        // myObject.setTarget("0123".getBytes());
        // value = myObject.estimation();
        // System.out.println(value);
        // myObject.setTarget("00".getBytes());
        // value = myObject.estimation();
        // System.out.println(value);

        byte[] space = "aabaaaaabbaababbaaaaaaaabbbbafdsasdacababababababababaaaabbbbbbaaaaaaabbbbaabbababaaaaabbbbbaaaaaaaaaabbbbbbbbbbbabababababab".getBytes();
        byte[] target = "aabbaabbaaabbaaaaaaaaaaaaaabbbbbbbbcbbbbbbaaaaaaabbabbbabaababaababbabbabbbbbbbbbaaaaaaaababbbbaaaaaabbbaaabbbbab".getBytes();

        myObject.setSpace(space);
        myObject.setTarget(target);
        Debug.printSuffixArray(myObject.SA, space, space.length);
        Debug.printSuffixTree(myObject.DAST, myObject.SA, space);
        // myObject.debug = true;
        double tval = myObject.estimation_sa();
        // myObject.debug = true;
        value = myObject.estimation();
        if (Math.abs(value - tval) > 0.000001){
            System.out.println(value + ":" + tval);
        } else {
            System.out.println("OK");
        }

        System.out.println("\r1000/1000");
    }
}
