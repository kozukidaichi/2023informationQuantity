package s4.T000001;

public class SuffixArray {
    private static void induced_sort(int[] vec, int vec_size, int val_range, int[] sa, int sa_size, boolean[] sl, int[] lms_idx, int lms_idx_offset, int lms_idx_size) {
        int[] l = new int[val_range], r = new int[val_range];
        for (int i = 0; i < val_range; ++i)
        {
            l[i] = 0;
            r[i] = 0;
        }
        for (int i = 0; i < vec_size; ++i) {
            int c = vec[i];
            if (c + 1 < val_range) ++l[c + 1];
            ++r[c];
        }
        
        for (int i = 1; i < val_range; ++i)
        {
            l[i] += l[i - 1];
            r[i] += r[i - 1];
        }
        for (int i = 0; i < sa_size; ++i) {
            sa[i] = -1;
        }
        for (int i = lms_idx_offset + lms_idx_size - 1; i >= lms_idx_offset; --i)
        {
            sa[--r[vec[lms_idx[i]]]] = lms_idx[i];
        }
        for (int i = 0; i < sa_size; ++i) {
            int j = sa[i];
            if (j >= 1 && sl[j - 1]) {
                sa[l[vec[j - 1]]++] = j - 1;
            }
        }
        for (int i = 0; i < val_range; ++i) {
            r[i] = 0;
        }
        for (int i = 0; i < vec_size; ++i) {
            ++r[vec[i]];
        }
        for (int i = 1; i < val_range; ++i)
        {
            r[i] += r[i - 1];
        }
        for (int i = sa_size - 1; i > 0; --i) {
            int j = sa[i];
            if (j >= 1 && !sl[j - 1]) {
                sa[--r[vec[j - 1]]] = j - 1;
            }
        }
    }
    private static void SA_IS(int[] sa, int[] vec, int n, int val_range, int[] buffer, int buffer_offset) {
        //lms_idx[i] = buffer[buffer_offset + i]
        int lms_len = 0;
        boolean[] sl = new boolean[n];
        sl[n - 1] = false;
        for (int i = n - 2; i >= 0; --i)
        {
            sl[i] = (vec[i] > vec[i + 1] || (vec[i] == vec[i + 1] && sl[i + 1]));
            if (sl[i] && !sl[i + 1])
            {
                buffer[buffer_offset + lms_len] = i + 1;
                ++lms_len;
            }
        }
        /* reverse lms_idx */
        for (int i = buffer_offset, j = buffer_offset + lms_len - 1; i < j; ++i, --j) {
            int tmp = buffer[i];
            buffer[i] = buffer[j];
            buffer[j] = tmp;
        }

        induced_sort(vec, n, val_range, sa, n, sl, buffer, buffer_offset, lms_len);

        int[] new_lms_idx = new int[lms_len];
        for (int i = 0, k = 0; i < n; ++i)
        {
            if (!sl[sa[i]] && sa[i] >= 1 && sl[sa[i] - 1])
            {
                new_lms_idx[k] = sa[i];
                ++k;
            }
        }

        int cur = 0;
        sa[n - 1] = 0;
        for (int k = 1; k < lms_len; ++k)
        {
            int i = new_lms_idx[k - 1], j = new_lms_idx[k];
            if (vec[i] != vec[j])
            {
                sa[j] = ++cur;
                continue;
            }
            for (int a = i + 1, b = j + 1;; ++a, ++b)
            {
                if (vec[a] != vec[b])
                {
                    ++cur;
                    break;
                }
                if ((!sl[a] && sl[a - 1]) || (!sl[b] && sl[b - 1]))
                {
                    if (!((!sl[a] && sl[a - 1]) && (!sl[b] && sl[b - 1])))
                        ++cur;
                    break;
                }
            }
            sa[j] = cur;
        }

        if (cur + 1 < lms_len) {
            int[] lms_vec = new int[lms_len];
            int[] lms_sa = new int[lms_len];
            for (int i = 0; i < lms_len; ++i)
                lms_vec[i] = sa[buffer[buffer_offset + i]];
            SA_IS(lms_sa, lms_vec, lms_len, cur + 1, buffer, buffer_offset + lms_len);
            for (int i = 0; i < lms_len; ++i)
            {
                new_lms_idx[i] = buffer[buffer_offset + lms_sa[i]];
            }
        }
        induced_sort(vec, n, val_range, sa, n, sl, new_lms_idx, 0, lms_len);
    }
    
    public static void buildSuffixArray(int[] sa, byte[] text, int size) {
        byte first = text[0], last = text[0];
        for (int i = 1; i < size; ++i) {
            byte tmp = text[i];
            if (tmp < first) first = tmp;
            else if (tmp > last) last = tmp;
        }

        int[] vec = new int[(size + 1) << 1];
        for (int i = 0; i < size; ++i) {
            vec[i] = (int)text[i] - first + 1;
        }

        vec[size] = 0;
        SA_IS(sa, vec, size + 1, last - first + 4, vec, size + 1);
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
                System.out.print((char)text[j]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int n = 100;
        byte[] text = new byte[n];
        for (int i = 0; i < n; ++i) {
            text[i] = (byte)('a' + (i % 26));
        }
        int[] sa = new int[n + 1];
        buildSuffixArray(sa, text, n);
        printSuffixArray(sa, text, n);
    }
}