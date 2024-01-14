package s4.T000001;

public class Debug {

    public static void printBitMap(long[] bit_map, int bit_map_size) {
        for (int i = 0; i < bit_map_size; ++i) {
            System.out.println("bit_map[" + i + "] = " + Long.toBinaryString(bit_map[i]));
        }
    }

    public static void printSA_LCP_BI(int[] sa, int[] lcp, int[] bi, byte[] text, int size) {
        int n_len = 1, n_size = 10;
        while (size >= n_size) {
            n_len++;
            n_size *= 10;
        }

        for (int i = 0; i <= size; ++i) {
            System.out.print(
                    String.format("%" + n_len + "d: sa = %" + n_len + "d, lcp = %" + n_len + "d, bi = %" + n_len + "d;",
                            i, sa[i], lcp[i], bi[i]));
            for (int j = sa[i]; j < size; ++j) {
                System.out.print((char) text[j]);
            }
            System.out.println();
        }
    }

    private static void printSuffixTree(int[] DAST, int dast_len, int[] SA, byte[] text, int text_size, int state,
            int base, StringBuilder sb1, StringBuilder sb2, int offset, int sb_offset, int sp, int ep) {
        boolean is_first = true;
        for (int i = 0; i < 128; ++i) {
            int child_index = (base + i) << 2;
            if (child_index < 0)
                continue;
            if (child_index >= dast_len)
                break;
            if (DAST[child_index] == state) {
                int j = offset, k = sb_offset + 1;
                int sa_sp = SA[DAST[child_index + 2]];
                int sa_ep = SA[DAST[child_index + 3] - 1];
                int j_max = text_size - ((sa_sp > sa_ep) ? sa_sp : sa_ep);

                if (is_first) {
                    if (DAST[child_index + 2] != sp) {
                        sb1.append("-$");
                        System.out.println(sb1.toString());
                        sb1.setLength(0);
                        sb1.append(sb2.toString());
                    }
                    is_first = false;
                }

                sb1.append('-');
                do {
                    sb1.append((char) text[sa_sp + j]);
                    sb2.append(' ');
                    ++j;
                    ++k;
                } while (j < j_max && text[sa_sp + j] == text[sa_ep + j]);
                sb2.append('|');

                if (DAST[child_index + 3] == ep) {
                    sb1.setCharAt(sb_offset - 1, '└');
                    sb2.setCharAt(sb_offset - 1, ' ');
                }

                printSuffixTree(DAST, dast_len, SA, text, text_size, base + i, DAST[child_index + 1], sb1, sb2, j, k,
                        DAST[child_index + 2], DAST[child_index + 3]);
                sb2.setLength(sb_offset);
                sb1.setLength(0);
                sb1.append(sb2.toString());
            }
        }
        if (is_first) {
            System.out.println(sb1.append('$').toString());
        }
    }

    /* 10進数での桁数をカウント */
    private static int countDigit(int n) {
        int digit = 1;
        while (n >= 10) {
            n /= 10;
            digit++;
        }
        return digit;
    }

    /* 16進数での桁数をカウント */
    private static int countDigitHex(int n) {
        int digit = 1;
        while (n >= 0x10) {
            n >>= 4;
            digit++;
        }
        return digit;
    }

    /* n桁になるように埋める */
    private static void fill(StringBuilder sb, int digit, int n) {
        for (int i = digit; i < n; ++i) {
            sb.append('0');
        }
    }

    private static void printSuffixTreeBinary(int[] DAST, int dast_len, int[] SA, byte[] text, int text_size, int state,
            int base, StringBuilder sb1, StringBuilder sb2, int offset, int sb_offset, int sp, int ep) {
        boolean is_first = true;
        for (int i = 0; i < 128; ++i) {
            int child_index = (base + i) << 2;
            if (child_index < 0)
                continue;
            if (child_index >= dast_len)
                break;
            if (DAST[child_index] == state) {
                int j = offset, k = sb_offset + 1;
                int sa_sp = SA[DAST[child_index + 2]];
                int sa_ep = SA[DAST[child_index + 3] - 1];
                int j_max = text_size - ((sa_sp > sa_ep) ? sa_sp : sa_ep);

                if (is_first) {
                    if (DAST[child_index + 2] != sp) {
                        sb1.append("-$");
                        System.out.println(sb1.toString());
                        sb1.setLength(0);
                        sb1.append(sb2.toString());
                    }
                    is_first = false;
                }

                sb1.append('-');
                do {
                    fill(sb1, countDigitHex(text[sa_sp + j]), 2);
                    sb1.append(Integer.toHexString(text[sa_sp + j]));
                    sb2.append("  ");
                    ++j;
                    k += 2;
                } while (j < j_max && text[sa_sp + j] == text[sa_ep + j] && k < 80);
                sb2.append('|');

                if (DAST[child_index + 3] == ep) {
                    sb1.setCharAt(sb_offset - 1, '└');
                    sb2.setCharAt(sb_offset - 1, ' ');
                }

                printSuffixTreeBinary(DAST, dast_len, SA, text, text_size, base + i, DAST[child_index + 1], sb1, sb2, j, k,
                        DAST[child_index + 2], DAST[child_index + 3]);
                sb2.setLength(sb_offset);
                sb1.setLength(0);
                sb1.append(sb2.toString());
            }
        }
        if (is_first) {
            System.out.println(sb1.append('$').toString());
        }
    }

    public static void printSuffixTree(int[] DAST, int[] SA, byte[] text) {
        System.out.println("root");
        printSuffixTree(DAST, DAST.length, SA, text, text.length, -1, -text[SA[1]], new StringBuilder("|"),
                new StringBuilder("|"), 0, 1, 1, text.length + 1);
    }

    public static void printSuffixTreeBinary(int[] DAST, int[] SA, byte[] text) {
        System.out.println("root");
        printSuffixTreeBinary(DAST, DAST.length, SA, text, text.length, -1, -text[SA[1]], new StringBuilder("|"),
                new StringBuilder("|"), 0, 1, 1, text.length + 1);
    }

    public static void printDASTarr(int[] DAST, int dast_len) {
        for (int i = 0; i < dast_len; i += 4) {
            System.out.println("DAST[" + (i >> 2) + "]: check = " + DAST[i] + ", base = " + DAST[i + 1] + ", sp = "
                    + DAST[i + 2] + ", ep = " + DAST[i + 3]);
        }
    }

    public static void printSuffixArray(int[] sa, byte[] text, int size) {
        int n_len = 1, n_size = 10;
        while (size >= n_size) {
            n_len++;
            n_size *= 10;
        }

        for (int i = 0; i <= size; ++i) {
            System.out.print(String.format("sa[%" + n_len + "d] = %" + n_len + "d:", i, sa[i]));
            for (int j = sa[i]; j < size; ++j) {
                System.out.print((char) text[j]);
            }
            System.out.println();
        }
    }
}
