/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.utils;

import com.amd.aparapi.Kernel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */
public class LevenshteinDistance {

    public int leastCompare(String s1, String[] s2) {
        String[] arr = new String[s2.length];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = s1;
        }

        int[] results = compare(arr, s2);
        int min = Integer.MAX_VALUE;

        for(int val : results) {
            if(val < min) {
                val = min;
            }
        }
        return min;

    }

    public int[] compare(String[] s1, String[] s2) {

        if(s1.length != s2.length) {
            throw new IllegalArgumentException("Array lengths must be equal!");
        }

        final int result[] = new int[s1.length];
        ArrStart s1Box = toIntArr(s1);
        final int[] s1Arr = s1Box.arr;
        final int[] s1Starts = s1Box.starts;
        final int[] s1Lengths = lengths(s1);
        ArrStart s2Box = toIntArr(s2);
        final int[] s2Arr = s2Box.arr;
        final int[] s2Starts = s2Box.starts;
        final int[] s2Lengths = lengths(s2);

        final int[] dumpPositions = getDumpPositions(s1, s2);
        int dumpSize = getDumpSize(s1, s2);

//        System.out.println(dumpSize);

        final int dump[] = new int[dumpSize]; //Yeah...
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                result[getGlobalId()] = ld(s1Arr, s2Arr, s1Starts[getGlobalId()], s2Starts[getGlobalId()], s1Lengths[getGlobalId()], s2Lengths[getGlobalId()], dumpPositions[getGlobalId()]);
            }

            public int ld(int[] s, int[] t, int sStart, int tStart, int sLength, int tLength, int dumpOffset) {
                int i; // iterates through s
                int j; // iterates through t
                int s_i; // ith character of s
                int t_j; // jth character of t
                int cost; // cost

                if(sLength == 0) {
                    return tLength;
                }
                if(tLength == 0) {
                    return sLength;
                }
                int firstSize = sLength + 1;

                for(i = 0; i <= sLength; i++) {
                    dump[dumpOffset + (firstSize * i + 0)] = i;
                }

                for(j = 0; j <= tLength; j++) {
                    dump[dumpOffset + (firstSize * 0 + j)] = j;
                }

                for(i = 1; i <= sLength; i++) {
                    s_i = s[sStart + (i - 1)];
                    for(j = 1; j <= tLength; j++) {
                        t_j = t[tStart + (j - 1)];
                        cost = s_i == t_j ? 0 : 1;
                        int a = dump[dumpOffset + (firstSize * (i - 1) + j)] + 1;
                        int b = dump[dumpOffset + (firstSize * i + (j - 1))] + 1;
                        int c = dump[dumpOffset + (firstSize * (i - 1) + (j - 1))] + cost;

                        int mi = a;
                        if(b < mi) {
                            mi = b;
                        }
                        if(c < mi) {
                            mi = c;
                        }
                        dump[dumpOffset + (firstSize * i + j)] = mi;
                    }
                }
                return dump[dumpOffset + (firstSize * sLength + tLength)];
            }
        };
        kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
        kernel.execute(s1.length);
        return result;
    }

    private int[] getDumpPositions(String[] s1, String[] s2) {
        int[] ret = new int[s1.length];
        for(int i = 0; i < s1.length; i++) {
            int prev;
            if(i == 0) {
                prev = 0;
            }
            else {
                prev = ret[i - 1];
            }
            ret[i] = prev + (s1[i].length() + 2) * (s2[i].length() + 2);

        }
        return ret;
    }

    private int getDumpSize(String[] s1, String[] s2) {
        int size = 0;
        for(int i = 0; i < s1.length; i++) {
            size += (s1[i].length() + 2) * (s2[i].length() + 2);

        }
        return size;
    }

    private static class ArrStart {

        public int[] arr;
        public int[] starts;
    }

    private ArrStart toIntArr(String[] strArr) {
        ArrStart ret = new ArrStart();
        List<Integer> starts = new ArrayList<>();
        List<Integer> arr = new ArrayList<>();
        int i = 0;
        for(String str : strArr) {
            starts.add(i);
            for(int character : str.toCharArray()) {
                arr.add(character);
                i++;
            }
        }
        ret.arr = toPrimitiveInt(arr);
        ret.starts = toPrimitiveInt(starts);
        return ret;
    }

    private int[] toPrimitiveInt(List<Integer> list) {
        int[] ret = new int[list.size()];
        for(int i = 0; i < list.size(); i++) {
            ret[i] = list.get(i);
        }
        return ret;
    }

    private int[] lengths(String[] arr) {
        int[] ret = new int[arr.length];
        for(int i = 0; i < arr.length; i++) {
            ret[i] = arr[i].length();
        }
        return ret;
    }
}
