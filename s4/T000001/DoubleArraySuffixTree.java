package s4.T000001;

public class DoubleArraySuffixTree {
    
    static int buildLCP(int[] LCP, int[] PLCP, byte[] text, int[] SA, int text_size) {
        for (int i = 1; i <= text_size; ++i) {
            PLCP[SA[i]] = SA[i - 1];
        }
        int l = 0, lcp_max = 0;
        for (int i = 0; i <= text_size; ++i) {
            int j = PLCP[i], l_end = text_size - (i > j ? i : j);
            while (l < l_end && text[i + l] == text[j + l]) {
                ++l;
            }
            PLCP[i] = l;
            if (l > lcp_max) {
                lcp_max = l;
                --l;
            } else if (l > 0) {
                --l;
            }
        }
        for (int i = 2; i <= text_size; ++i) {
            LCP[i] = PLCP[SA[i]];
        }
        return lcp_max;
    }

    static void buildBI(int[] BI, int[] EFO, int[] LCP, int lcp_max, int text_size) {
        for (int i = lcp_max; i >= 0; --i) EFO[i] = text_size + 1;
        for (int i = text_size; i >= 2; --i) {
            int lcp = LCP[i];
            BI[i] = EFO[lcp];
            EFO[lcp] = i;
        }
    }

    static void setBit(long[] bit_map, int i) {
        bit_map[i >> 6] |= 1L << (i & 63);
    }

    static int calcBase(long[] dast_map, int dast_map_size, long[] child_map, long[] tmp_5, int first, int start) {
        int i, j;
        if (start < 0) start = 0;
        for (i = start >> 6, j = start & 63; i < dast_map_size; ++i, j = 0) {
            for (; j < 64; ++j) {
                tmp_5[0] = child_map[0] << j                         ;
                tmp_5[1] = child_map[1] << j | child_map[0] >> (64-j);
                tmp_5[2] = child_map[2] << j | child_map[1] >> (64-j);
                tmp_5[3] = child_map[3] << j | child_map[2] >> (64-j);
                tmp_5[4] =                     child_map[3] >> (64-j);

                if (((dast_map[i] & tmp_5[0]) | (dast_map[i + 1] & tmp_5[1]) | (dast_map[i + 2] & tmp_5[2]) | (dast_map[i + 3] & tmp_5[3]) | (dast_map[i + 4] & tmp_5[4])) == 0) {
                    dast_map[i] |= tmp_5[0];
                    dast_map[i + 1] |= tmp_5[1];
                    dast_map[i + 2] |= tmp_5[2];
                    dast_map[i + 3] |= tmp_5[3];
                    dast_map[i + 4] |= tmp_5[4];
                    return (i << 6) + j - first;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    public static void printSA_LCP_BI(int[] sa, int[] lcp, int[] bi, byte[] text, int size) {
        int n_len = 1, n_size = 10;
        while (size >= n_size) {
            n_len++;
            n_size *= 10;
        }

        for (int i = 0; i <= size; ++i) {
            System.out.print(String.format("%" + n_len + "d: sa = %" + n_len + "d, lcp = %" + n_len + "d, bi = %" + n_len + "d;", i, sa[i], lcp[i], bi[i]));
            for (int j = sa[i]; j < size; ++j) {
                System.out.print((char)text[j]);
            }
            System.out.println();
        }
    }
    /*
     * 2進数で出力
     */
    static void printBitMap(long[] bit_map, int bit_map_size) {
        for (int i = 0; i < bit_map_size; ++i) {
            System.out.println("bit_map[" + i + "] = " + Long.toBinaryString(bit_map[i]));
        }
    }

    static void buildSuffixTree(int[] DAST, int dast_size, byte[] text, int text_size, int[] SA) {
        int dast_len = dast_size << 2;
        int dast_map_size = (dast_size + 63) >> 6;
        long[] dast_map = new long[dast_map_size + 4];
        int first = text[SA[1]];

        int[] BI = new int[text_size + 1];
        int[] LCP = new int[text_size + 1];

        int lcp_max = buildLCP(LCP, BI, text, SA, text_size);

        int[] EFO = new int[lcp_max + 1];

        buildBI(BI, EFO, LCP, lcp_max, text_size);
        // printSA_LCP_BI(SA, LCP, BI, text, text_size);

        for (int i = 0; i < dast_len; i += 4) {
            DAST[i] = Integer.MIN_VALUE; //dast.check = MIN_VALUE
        }

        dast_map[dast_map_size    ] = Long.MIN_VALUE;
        dast_map[dast_map_size + 1] = Long.MIN_VALUE;
        dast_map[dast_map_size + 2] = Long.MIN_VALUE;
        dast_map[dast_map_size + 3] = Long.MIN_VALUE;

        int[] offset_list = new int[lcp_max + 2];
        long[] child_map = new long[4];
        long[] tmp_5 = new long[5];

        int offset_list_i = -1;

        int base_i = 0;
        int node_base;

        int state = -1;
        int sp1 = 1, sp2 = EFO[0];
        int sp, ep;
        int pattern;

        int parent_base;
        int parent_ep;

        int node_check = -1;
        int node_sp = 1;
        int node_ep = text_size + 1;
        int offset = 0;
        int child_index;
        int node_index;

        L1:
        for(;;) {
            child_map[0] = 0;
            child_map[1] = 0;
            child_map[2] = 0;
            child_map[3] = 0;

            setBit(child_map, text[SA[sp1] + offset] - first);
            for (sp = sp2; sp < node_ep; sp = BI[sp]) {
                setBit(child_map, text[SA[sp] + offset] - first);
            }

            node_base = calcBase(dast_map, dast_map_size, child_map, tmp_5, first, state - 50);
            if (node_base == Integer.MIN_VALUE) {
                System.out.println("Error: not enough space for DAST");
            }

            DAST[base_i] = node_base;

            for (sp = sp1, ep = sp2; ep < node_ep; sp = ep, ep = BI[ep]) {
                pattern = text[SA[sp] + offset];
                child_index = (node_base + pattern) << 2;
                DAST[child_index] = state;
                DAST[child_index + 1] = Integer.MIN_VALUE;
                DAST[child_index + 2] = sp;
                DAST[child_index + 3] = ep;
            }
            pattern = text[SA[sp] + offset];
            child_index = (node_base + pattern) << 2;
            DAST[child_index] = state;
            DAST[child_index + 1] = Integer.MIN_VALUE;
            DAST[child_index + 2] = sp;
            DAST[child_index + 3] = node_ep;

            EFO[offset] = ep;

            parent_base = node_base;
            parent_ep = node_ep;

            state = text[SA[sp1] + offset] + node_base;
            node_index = state << 2;
            base_i = node_index + 1;
            node_check = DAST[node_index];
            node_base = DAST[node_index + 1];
            node_sp = DAST[node_index + 2];
            node_ep = DAST[node_index + 3];

            offset_list[++offset_list_i] = offset;

            if (node_ep - node_sp > 1) {
                while (EFO[++offset] >= node_ep);
                if (SA[node_sp] + offset >= text_size) {
                    sp1 = node_sp + 1;
                    sp2 = BI[sp1];
                } else {
                    sp1 = node_sp;
                    sp2 = EFO[offset];
                }
                continue L1;
            }
            for (;;) {
                while (node_ep < parent_ep) {
                    state = text[SA[node_ep] + offset] + parent_base;
                    node_index = state << 2;
                    base_i = node_index + 1;
                    node_check = DAST[node_index];
                    node_base = DAST[node_index + 1];
                    node_sp = DAST[node_index + 2];
                    node_ep = DAST[node_index + 3];
                    if (node_ep - node_sp > 1) {
                        while (EFO[++offset] >= node_ep);
                        if (SA[node_sp] + offset >= text_size) {
                            sp1 = node_sp + 1;
                            sp2 = BI[sp1];
                        } else {
                            sp1 = node_sp;
                            sp2 = EFO[offset];
                        }
                        continue L1;
                    }
                }

                if (node_check < 0) {
                    // SuffixArray.printSuffixArray(SA, text, text_size);
                    // printDASTarr(DAST, dast_len);
                    // printDAST(DAST, dast_len, first);
                    return;
                }

                state = node_check;
                node_index = state << 2;
                node_check = DAST[node_index];
                node_base = DAST[node_index + 1];
                node_sp = DAST[node_index + 2];
                node_ep = DAST[node_index + 3];
                offset = offset_list[--offset_list_i];
                if (node_check == -1) {
                    parent_ep = text_size + 1;
                    parent_base = -first;
                } else {
                    node_index = node_check << 2;
                    parent_base = DAST[node_index + 1];
                    parent_ep = DAST[node_index + 3];
                }

            }
        }
    }
    /*
     * input
     * sb1: |-a
     * sb2: | |
     * 
     * first child $
     * sb1: |-a-$
     * sb1: | |-b
     * sb2: | | |
     * 
     * first child
     * sb1: |-a-a
     * sb2: | | |
     * 
     * second child
     * sb1: | |-b
     * sb2: | | |
     */
    static void printDASTchild(int[] DAST, int dast_len, int state, int base, StringBuilder sb1, StringBuilder sb2, int sb_i, int sp) {
        boolean is_first = true;
        for (int i = 0; i <= 128; ++i) {
            int child_index = (base + i) << 2;
            if (child_index < 0) continue;
            if (child_index >= dast_len) break;
            if (DAST[child_index] == state) {
                if (is_first) {
                    if (DAST[child_index + 2] != sp) {
                        sb1.append('-');
                        sb1.append('$');
                        System.out.println(sb1.toString());
                        sb1.setLength(0);
                        sb1.append(sb2.toString());
                    }
                    sb1.append('-');
                    sb1.append((char)i);
                    sb2.append(' ');
                    sb2.append('|');
                    is_first = false;
                } else {
                    sb1.setLength(0);
                    sb1.append(sb2.toString());
                    sb1.setCharAt(sb_i, '-');
                    sb1.setCharAt(sb_i + 1, (char)i);
                }
                printDASTchild(DAST, dast_len, base + i, DAST[child_index + 1], sb1, sb2, sb_i + 2, DAST[child_index + 2]);
            }
        }
        if (is_first) {
            sb1.append('-');
            sb1.append('$');
            System.out.println(sb1.toString());
        }
        sb2.setLength(sb_i);
    }

    static void printDAST(int[] DAST, int dast_len, int first) {
        System.out.println("root");
        StringBuilder sb = new StringBuilder();
        sb.append('|');
        printDASTchild(DAST, dast_len, -1, -first, new StringBuilder("|"), new StringBuilder("|"), 1, 1);
    }

    static void printDASTarr(int[] DAST, int dast_len) {
        for (int i = 0; i < dast_len; i += 4) {
            System.out.println("DAST[" + (i >> 2) + "]: check = " + DAST[i] + ", base = " + DAST[i + 1] + ", sp = " + DAST[i + 2] + ", ep = " + DAST[i + 3]);
        }
    }
}
