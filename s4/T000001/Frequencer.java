package s4.T000001; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 

import java.lang.*;
import s4.specification.*;

/*
interface FrequencerInterface {  // This interface provides the design for frequency counter.
    void setTarget(byte[] target);  // set the data to search.
    void setSpace(byte[] space);  // set the data to be searched target from.
    int frequency(); // It return -1, when TARGET is not set or TARGET's length is zero
                     // Otherwise, it return 0, when SPACE is not set or Space's length is zero
                     // Otherwise, get the frequency of TAGET in SPACE
    int subByteFrequency(int start, int end);
    // get the frequency of subByte of taget, i.e. target[start], taget[start+1], ... , target[end-1].
    // For the incorrect value of START or END, the behavior is undefined.
}
*/

public class Frequencer implements FrequencerInterface {
    // Code to Test, *warning: This code contains intentional problem*
    static boolean debugMode = false;
    byte[] myTarget = null;
    byte[] mySpace = null;

    int[] DAST;
    int[] SA;
    int[] table = new int[256];
    int[] replacedTarget;

    @Override
    public void setTarget(byte[] target) {
        if (target == null || target.length == 0) {
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
        if (space == null || space.length == 0) {
            mySpace = null;
            return;
        }
        mySpace = space;
        SA = new int[space.length + 1];
        SuffixArray.buildSuffixArray(SA, space, space.length);
        int dast_size = (space.length << 1) + space.length;
        DAST = new int[dast_size << 2];
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

    @Override
    public int frequency() {
        if (myTarget == null)
            return -1;
        if (mySpace == null || replacedTarget == null)
            return 0;

        int targetLength = replacedTarget.length;

        int state = -1;
        int base = 0;
        int offset = 0;
        int offset_max;
        int i = 0;
        int pattern = replacedTarget[0];
        int sa_sp;
        int sa_ep;
        int sp, ep;
        do {
            int next_state = base + pattern;
            int node_index = next_state << 2;
            if (node_index < 0 || DAST[node_index] != state) {
                return 0;
            }
            state = base + pattern;
            base = DAST[node_index + 1];
            sp = DAST[node_index + 2];
            ep = DAST[node_index + 3];

            sa_sp = SA[sp];
            sa_ep = SA[ep - 1];

            /* Suffix Treeの枝をたどる */
            offset_max = mySpace.length - ((sa_sp < sa_ep) ? sa_ep : sa_sp); // spaceの終端
            if (offset_max > targetLength - i)
                offset_max = targetLength - i; // targetの終端

            do { // patternとsa[sp],SA[ep-1]の文字が一致している間offsetを増加させる
                if (++offset >= offset_max)
                    break;
                pattern = replacedTarget[i + offset];
            } while (pattern == mySpace[sa_sp + offset] && pattern == mySpace[sa_ep + offset]);
        } while (i + offset < targetLength && base + pattern >= 0
                && (offset >= offset_max || mySpace[sa_sp + offset] != mySpace[sa_ep + offset]));
        return ep - sp;
    }

    // I know that here is a potential problem in the declaration.
    @Override
    public int subByteFrequency(int start, int length) {
        // Not yet implemented, but it should be defined as specified.
        return -1;
    }

    public static void main(String[] args) {
        Frequencer myObject;
        int freq;
        // White box test, here.
        Frequencer.debugMode = true;
        try {
            myObject = new Frequencer();
            myObject.setSpace("Hi Ho Hi Ho".getBytes());
            myObject.setTarget("Hi".getBytes());
            freq = myObject.frequency();
        } catch (Exception e) {
            System.out.println("Exception occurred: STOP");
        }
    }
}
