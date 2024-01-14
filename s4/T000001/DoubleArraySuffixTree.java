package s4.T000001;

public class DoubleArraySuffixTree {

    private static void buildBI(int[] BI, int[] EFO, int[] LCP, int lcp_max, int text_size) {
        for (int i = lcp_max; i >= 0; --i)
            EFO[i] = text_size + 1;
        for (int i = text_size; i >= 2; --i) {
            int lcp = LCP[i];
            BI[i] = EFO[lcp];
            EFO[lcp] = i;
        }
    }

    private static int buildLCP(int[] LCP, int[] PLCP, byte[] text, int[] SA, int text_size) {
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

    private static void setBit(long[] bit_map, int i) {
        bit_map[i >> 6] |= 1L << (i & 63);
    }

    private static int calcBase(long[] dast_map, int dast_map_size, long[] child_map, long[] tmp_5, int first,
            int start) {
        int i, j;
        if (start < 0)
            start = 0;

        for (i = start >> 6, j = start & 63; i < dast_map_size; ++i, j = 0) {
            for (; j < 64; ++j) {
                tmp_5[0] = child_map[0] << j;
                tmp_5[1] = child_map[1] << j | child_map[0] >> (64 - j);
                tmp_5[2] = child_map[2] << j | child_map[1] >> (64 - j);
                tmp_5[3] = child_map[3] << j | child_map[2] >> (64 - j);
                tmp_5[4] = child_map[3] >> (64 - j);

                if (((dast_map[i] & tmp_5[0]) | (dast_map[i + 1] & tmp_5[1]) | (dast_map[i + 2] & tmp_5[2])
                        | (dast_map[i + 3] & tmp_5[3]) | (dast_map[i + 4] & tmp_5[4])) == 0) {
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

    public static void buildSuffixTreeReplace(int[] DAST, int dast_size, byte[] text, int text_size, int[] SA,
            int[] table) {
        int dast_len = dast_size << 2;
        int dast_map_size = (dast_size + 63) >> 6;
        long[] dast_map = new long[dast_map_size + 4];

        int[] BI = new int[text_size + 1];
        int[] LCP = new int[text_size + 1];

        int lcp_max = buildLCP(LCP, BI, text, SA, text_size);

        int[] EFO = new int[lcp_max + 1];

        buildBI(BI, EFO, LCP, lcp_max, text_size);

        int sp, ep, j;
        for (int i = 0; i < 256; ++i) {
            table[i] = -1;
        }

        for (sp = 1, ep = EFO[0], j = 0; ep <= text_size; sp = ep, ep = BI[ep], ++j) {
            table[((int) text[SA[sp]]) & 0xFF] = j;
        }
        table[((int) text[SA[sp]]) & 0xFF] = j;

        int[] replaced_text = LCP;
        for (int i = 0; i < text_size; ++i) {
            replaced_text[i] = table[((int) text[i]) & 0xFF];
        }

        for (int i = 0; i < dast_len; i += 4) {
            DAST[i] = Integer.MIN_VALUE; // dast.check = MIN_VALUE
        }

        dast_map[dast_map_size] = Long.MIN_VALUE;
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
        int pattern;

        int parent_base;
        int parent_ep;

        int node_check = -1;
        int node_sp = 1;
        int node_ep = text_size + 1;
        int offset = 0;
        int child_index;
        int node_index;

        L1: for (;;) {
            child_map[0] = 0;
            child_map[1] = 0;
            child_map[2] = 0;
            child_map[3] = 0;

            setBit(child_map, replaced_text[SA[sp1] + offset]);
            for (sp = sp2; sp < node_ep; sp = BI[sp]) {
                setBit(child_map, replaced_text[SA[sp] + offset]);
            }

            node_base = calcBase(dast_map, dast_map_size, child_map, tmp_5, 0, state - 50);
            if (node_base == Integer.MIN_VALUE) {
                System.out.println("Error: not enough space for DAST");
            }

            DAST[base_i] = node_base;

            for (sp = sp1, ep = sp2; ep < node_ep; sp = ep, ep = BI[ep]) {
                pattern = replaced_text[SA[sp] + offset];
                child_index = (node_base + pattern) << 2;
                DAST[child_index] = state;
                DAST[child_index + 1] = Integer.MIN_VALUE;
                DAST[child_index + 2] = sp;
                DAST[child_index + 3] = ep;
            }
            pattern = replaced_text[SA[sp] + offset];
            child_index = (node_base + pattern) << 2;
            DAST[child_index] = state;
            DAST[child_index + 1] = Integer.MIN_VALUE;
            DAST[child_index + 2] = sp;
            DAST[child_index + 3] = node_ep;

            EFO[offset] = ep;

            parent_base = node_base;
            parent_ep = node_ep;

            state = replaced_text[SA[sp1] + offset] + node_base;
            node_index = state << 2;
            base_i = node_index + 1;
            node_check = DAST[node_index];
            node_base = DAST[node_index + 1];
            node_sp = DAST[node_index + 2];
            node_ep = DAST[node_index + 3];

            offset_list[++offset_list_i] = offset;

            if (node_ep - node_sp > 1) {
                while (EFO[++offset] >= node_ep)
                    ;
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
                    state = replaced_text[SA[node_ep] + offset] + parent_base;
                    node_index = state << 2;
                    base_i = node_index + 1;
                    node_check = DAST[node_index];
                    node_base = DAST[node_index + 1];
                    node_sp = DAST[node_index + 2];
                    node_ep = DAST[node_index + 3];
                    if (node_ep - node_sp > 1) {
                        while (EFO[++offset] >= node_ep)
                            ;
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
                    parent_base = 0;
                } else {
                    node_index = node_check << 2;
                    parent_base = DAST[node_index + 1];
                    parent_ep = DAST[node_index + 3];
                }
            }
        }
    }

    public static void buildSuffixTree(int[] DAST, int dast_size, byte[] text, int text_size, int[] SA) {
        int dast_len = dast_size << 2;
        int dast_map_size = (dast_size + 63) >> 6;
        long[] dast_map = new long[dast_map_size + 4];
        int first = text[SA[1]];

        int[] BI = new int[text_size + 1];
        int[] LCP = new int[text_size + 1];

        int lcp_max = buildLCP(LCP, BI, text, SA, text_size);

        int[] EFO = new int[lcp_max + 1];

        buildBI(BI, EFO, LCP, lcp_max, text_size);

        for (int i = 0; i < dast_len; i += 4) {
            DAST[i] = Integer.MIN_VALUE; // dast.check = MIN_VALUE
        }

        dast_map[dast_map_size] = Long.MIN_VALUE;
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

        L1: for (;;) {
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
                while (EFO[++offset] >= node_ep)
                    ;
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
                        while (EFO[++offset] >= node_ep)
                            ;
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
}
