/*****************************************************************************
 * This file is part of Rinzo
 *
 * Author: Claudio Cancinos
 * WWW: https://sourceforge.net/projects/editorxml
 * Copyright (C): 2008, Claudio Cancinos
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; If not, see <http://www.gnu.org/licenses/>
 ****************************************************************************/
package ar.com.tadp.xml.rinzo.core.utils;

import java.awt.Toolkit;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * 
 * @author ccancinos
 */
public class InUtils {
    private static class StringComparator implements Comparator<String> {

        public int compare(String obj, String obj1) {
            return obj.compareTo(obj1);
        }

        StringComparator() {
        }
    }

    private static class SortPair {

        Object fObject;

        int fIndex;

        public SortPair(Object obj, int i) {
            fObject = obj;
            fIndex = i;
        }
    }

    private static class SortPairComparator implements Comparator<SortPair> {

        Comparator<Object> fComp;

        public int compare(SortPair obj, SortPair obj1) {
            return fComp.compare(obj.fObject, obj1.fObject);
        }

        public SortPairComparator(Comparator<Object> comparator) {
            fComp = comparator;
        }
    }


    public static final Comparator<String> STRING_COMPARATOR = new StringComparator();

    public static final int BUF_SIZE = 4096;

    public static final int NUM_BUFS = 8;

    public InUtils() {
    }

    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    public static String[] csvToArray(String s) {
        ArrayList<String> arraylist = new ArrayList<String>();
        boolean flag = false;
        boolean flag1 = true;
        StringBuffer stringbuffer = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c <= ' ')
                flag = true;
            else if (c == ',') {
                arraylist.add(stringbuffer.toString());
                stringbuffer.setLength(0);
                flag1 = true;
                flag = false;
            } else {
                if (!flag1 && flag)
                    stringbuffer.append(' ');
                flag1 = flag = false;
                stringbuffer.append(c);
            }
        }

        arraylist.add(stringbuffer.toString());
        return arraylist.toArray(new String[arraylist.size()]);
    }

    public static String[] ssvToArray(String s) {
        ArrayList<String> arraylist = new ArrayList<String>();
        boolean flag = true;
        StringBuffer stringbuffer = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c <= ' ') {
                if (!flag) {
                    arraylist.add(stringbuffer.toString());
                    stringbuffer.setLength(0);
                }
                flag = true;
            } else {
                flag = false;
                stringbuffer.append(c);
            }
        }

        if (!flag)
            arraylist.add(stringbuffer.toString());
        return arraylist.toArray(new String[arraylist.size()]);
    }

    public static String arrayToSv(String as[], String s) {
        StringBuffer stringbuffer = new StringBuffer();
        for (int i = 0; i < as.length; i++) {
            if (i > 0)
                stringbuffer.append(s);
            stringbuffer.append(as[i]);
        }

        return stringbuffer.toString();
    }

    public static String collectionToSv(Collection<?> collection, String s) {
        StringBuffer stringbuffer = new StringBuffer();
        int i = 0;
        String s1;
        for (Iterator iterator = collection.iterator(); iterator.hasNext(); stringbuffer.append(s1)) {
            s1 = iterator.next().toString();
            if (i++ > 0)
                stringbuffer.append(s);
        }

        return stringbuffer.toString();
    }

    public static boolean containsIdentical(Object aobj[], Object obj) {
        for (int i = 0; i < aobj.length; i++)
            if (aobj[i] == obj)
                return true;

        return false;
    }

    public static boolean containsEqual(Object aobj[], Object obj) {
        for (int i = 0; i < aobj.length; i++)
            if (obj == null ? aobj[i] == null : obj.equals(aobj[i]))
                return true;

        return false;
    }

    public static LinkedList setToList(Set set) {
        Iterator iterator = set.iterator();
        LinkedList linkedlist = new LinkedList();
        while(iterator.hasNext()) {
        	linkedlist.add(iterator.next());
        }
        return linkedlist;
    }

    public static LinkedList<Object> arrayToList(Object aobj[]) {
        LinkedList<Object> linkedlist = new LinkedList<Object>();
        if (aobj != null && aobj.length > 0) {
            for (int i = 0; i < aobj.length; i++)
                linkedlist.add(aobj[i]);

        }
        return linkedlist;
    }

    public static ArrayList<Object> arrayToArrayList(Object aobj[]) {
        ArrayList<Object> arraylist = new ArrayList<Object>();
        if (aobj != null && aobj.length > 0) {
            for (int i = 0; i < aobj.length; i++)
                arraylist.add(aobj[i]);

        }
        return arraylist;
    }

    public static String toSeparatedValue(Collection collection, String s) {
        Iterator iterator = collection.iterator();
        StringBuffer stringbuffer = new StringBuffer();
        boolean flag = true;
        for (; iterator.hasNext(); stringbuffer.append(iterator.next().toString())) {
            if (!flag)
                stringbuffer.append(s);
            flag = false;
        }

        return stringbuffer.toString();
    }

    public static String toSeparatedValue(Object aobj[], String s) {
        StringBuffer stringbuffer = new StringBuffer();
        boolean flag = true;
        for (int i = 0; i < aobj.length; i++) {
            if (!flag)
                stringbuffer.append(s);
            flag = false;
            stringbuffer.append(aobj[i].toString());
        }

        return stringbuffer.toString();
    }

    public static String[] csvToArray(CharSequence charsequence) {
        ArrayList<String> arraylist = new ArrayList<String>();
        StringBuffer stringbuffer = new StringBuffer();
        int i = 0;
        for (int j = charsequence.length(); i < j; i++) {
            char c = charsequence.charAt(i);
            if (c == ',') {
                if (stringbuffer.length() > 0) {
                    arraylist.add(stringbuffer.toString());
                    stringbuffer.setLength(0);
                }
            } else {
                stringbuffer.append(c);
            }
        }

        if (stringbuffer.length() > 0)
            arraylist.add(stringbuffer.toString());
        return arraylist.toArray(new String[arraylist.size()]);
    }

    public static String[] csvToTrimArray(CharSequence charsequence) {
        ArrayList<String> arraylist = new ArrayList<String>();
        StringBuffer stringbuffer = new StringBuffer();
        int i = -1;
        int j = 0;
        for (int k = charsequence.length(); j < k; j++) {
            char c = charsequence.charAt(j);
            if (c == ',') {
                if (stringbuffer.length() > 0) {
                    addTrimmed(stringbuffer, arraylist, i);
                    i = -1;
                }
            } else if (c > ' ' || i >= 0) {
                if (c > ' ')
                    i = stringbuffer.length();
                stringbuffer.append(c);
            }
        }

        if (stringbuffer.length() > 0)
            addTrimmed(stringbuffer, arraylist, i);
        return arraylist.toArray(new String[arraylist.size()]);
    }

    private static void addTrimmed(StringBuffer stringbuffer, java.util.List<String> list, int i) {
        int j = i + 1;
        stringbuffer.setLength(j);
        list.add(stringbuffer.toString());
        stringbuffer.setLength(0);
    }

    public static Object[] addElement(Object aobj[], Object obj) {
        Object aobj1[] = (Object[]) Array.newInstance(((Object) (aobj)).getClass().getComponentType(), aobj.length + 1);
        System.arraycopy(((Object) (aobj)), 0, ((Object) (aobj1)), 0, aobj.length);
        aobj1[aobj.length] = obj;
        return aobj1;
    }

    public static Object[] copyArray(Object aobj[]) {
        Object aobj1[] = (Object[]) Array.newInstance(((Object) (aobj)).getClass().getComponentType(), aobj.length);
        System.arraycopy(((Object) (aobj)), 0, ((Object) (aobj1)), 0, aobj.length);
        return aobj1;
    }

    public static Object[] catArrays(Object aobj[], Object aobj1[]) {
        if (aobj == null || aobj.length == 0)
            return aobj1;
        if (aobj1 == null || aobj1.length == 0) {
            return aobj;
        } else {
            Object aobj2[] = (Object[]) Array.newInstance(((Object) (aobj)).getClass().getComponentType(), aobj.length
                    + aobj1.length);
            System.arraycopy(((Object) (aobj)), 0, ((Object) (aobj2)), 0, aobj.length);
            System.arraycopy(((Object) (aobj1)), 0, ((Object) (aobj2)), aobj.length, aobj1.length);
            return aobj2;
        }
    }

    public static char[] catArrays(char ac[], char ac1[]) {
        int i = ac.length;
        int j = ac1.length;
        char ac2[] = new char[i + j];
        System.arraycopy(ac, 0, ac2, 0, i);
        System.arraycopy(ac1, 0, ac2, i, j);
        return ac2;
    }

    public static char[] catArrays(char ac[], char ac1[], int i) {
        int j = ac.length;
        char ac2[] = new char[j + i];
        System.arraycopy(ac, 0, ac2, 0, j);
        System.arraycopy(ac1, 0, ac2, j, i);
        return ac2;
    }

    public static int indexOf(char ac[], int i, int j, char ac1[], int k, int l) {
        if (i > j || i < 0 || j > ac.length)
            throw new IllegalArgumentException("Value range invalid");
        if (k > l || k < 0 || l > ac1.length)
            throw new IllegalArgumentException("Buffer range invalid");
        if (i == j || k == l)
            return -1;
        char c = ac[i];
        int i1 = i + 1;
        int j1 = k;
        for (int k1 = (l - j) + i1; j1 < k1; j1++)
            if (ac1[j1] == c) {
                boolean flag = true;
                for (int l1 = i1; l1 < j; l1++) {
                    if (ac[l1] == ac1[j1 + l1])
                        continue;
                    flag = false;
                    break;
                }

                if (flag)
                    return j1;
            }

        return -1;
    }

    public static int indexOf(Object aobj[], Object obj, Comparator<Object> comparator) {
        if (aobj == null || aobj.length == 0)
            return -1;
        int i = 0;
        int j = aobj.length;
        boolean flag = false;
        while (i < j) {
            int k = i + j >> 1;
            Object obj1 = aobj[k];
            int l = comparator.compare(obj1, obj);
            if (l < 0) {
                i = k + 1;
            } else {
                j = k;
                flag = l == 0;
            }
        }
        return flag ? j : -1;
    }

    public static int insertPosition(Object aobj[], Object obj, Comparator<Object> comparator) {
        if (aobj == null || aobj.length == 0)
            return 0;
        int i = 0;
        int j;
        for (j = aobj.length; i < j;) {
            int k = i + j >> 1;
            Object obj1 = aobj[k];
            int l = comparator.compare(obj1, obj);
            if (l < 0)
                i = k + 1;
            else
                j = k;
        }

        return j;
    }

    public static int findRange(Object aobj[], Object obj, Comparator<Object> comparator) {
        if (aobj == null || aobj.length == 0)
            return -1;
        int i = 0;
        int j;
        for (j = aobj.length; i < j;) {
            int k = i + j >> 1;
            Object obj1 = aobj[k];
            int l = comparator.compare(obj1, obj);
            if (l <= 0)
                i = k + 1;
            else
                j = k;
        }

        return j - 1;
    }

    public static void sort(Object aobj[], int i, Comparator<Object> comparator) {
        if (aobj.length % i != 0)
            throw new IllegalArgumentException("array must have a multiple of " + i + " elements");
        Object aobj1[] = new Object[aobj.length];
        System.arraycopy(((Object) (aobj)), 0, ((Object) (aobj1)), 0, aobj.length);
        int j = aobj.length / i;
        SortPair asortpair[] = new SortPair[j];
        int k = 0;
        for (int l = 0; k < asortpair.length; l += i) {
            asortpair[k] = new SortPair(aobj[l] != null ? aobj[l] : "", l);
            k++;
        }

        SortPairComparator sortpaircomparator = new SortPairComparator(comparator);
        Arrays.sort(asortpair, sortpaircomparator);
        int i1 = 0;
        for (int j1 = 0; i1 < asortpair.length; j1 += i) {
            SortPair sortpair = asortpair[i1];
            aobj[j1] = sortpair.fObject;
            for (int k1 = 1; k1 < i; k1++)
                aobj[j1 + k1] = aobj1[sortpair.fIndex + k1];

            i1++;
        }

    }

    private static void buggysort(Object aobj[], int i, Comparator<Object> comparator) {
        if (aobj.length % i != 0) {
            throw new IllegalArgumentException("array must have a multiple of " + i + " elements");
        } else {
            int j = 0;
            int k = aobj.length - i;
            sort(aobj, i, j, k, comparator);
            return;
        }
    }

    private static void sort(Object aobj[], int i, int j, int k, Comparator<Object> comparator) {
        if (j >= k)
            return;
        if (j + i == k) {
            int l = comparator.compare(aobj[j], aobj[k]);
            if (l > 0)
                swap(aobj, i, j, k);
            return;
        }
        int i1 = (j + k) / 2;
        int j1 = (i1 / i) * i;
        int k1 = partition(aobj, i, j, k, j1, comparator);
        if (k1 - j <= k - k1 - i) {
            sort(aobj, i, j, k1, comparator);
            sort(aobj, i, k1 + i, k, comparator);
        } else {
            sort(aobj, i, k1 + i, k, comparator);
            sort(aobj, i, j, k1, comparator);
        }
    }

    private static int partition(Object aobj[], int i, int j, int k, int l, Comparator<Object> comparator) {
        Object obj = aobj[l];
        int i1 = i * 2;
        do {
            while (j < k) {
                int j1 = comparator.compare(aobj[j], obj);
                if (j1 >= 0)
                    break;
                j += i;
            }
            for (; j < k; k -= i) {
                int k1 = comparator.compare(obj, aobj[k]);
                if (k1 >= 0)
                    break;
            }

            if (j >= k)
                break;
            swap(aobj, i, j, k);
            if (k - j < i1)
                break;
            j += i;
            k -= i;
        } while (true);
        return j;
    }

    private static void swap(Object aobj[], int i, int j, int k) {
        for (int l = 0; l < i; l++) {
            Object obj = aobj[j + l];
            aobj[j + l] = aobj[k + l];
            aobj[k + l] = obj;
        }

    }

    public static String newString(char c, int i) {
        StringBuffer stringbuffer = new StringBuffer(i);
        while (i-- > 0)
            stringbuffer.append(c);
        return stringbuffer.toString();
    }

    public static char[] toCharArray(StringBuffer stringbuffer) {
        return toCharArray(stringbuffer, 0, stringbuffer.length());
    }

    public static char[] toCharArray(StringBuffer stringbuffer, int i, int j) {
        char ac[] = new char[j - i];
        stringbuffer.getChars(i, j, ac, 0);
        return ac;
    }

    public static char[] copy(char ac[]) {
        char ac1[] = new char[ac.length];
        System.arraycopy(ac, 0, ac1, 0, ac.length);
        return ac1;
    }

    public static int hexChr(char c) {
        if (c >= '0' && c <= '9')
            return c - 48;
        if (c >= 'a' && c <= 'f')
            return (c - 97) + 10;
        if (c >= 'A' && c <= 'F')
            return (c - 65) + 10;
        else
            throw new IllegalArgumentException("'" + c + "' is not a hex character");
    }

    public static boolean isUpHexDigit(char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'F';
    }

    public static int getListSize(java.util.List list, int i, int j) {
        if (list == null)
            return 0;
        int k = 0;
        int l = i;
        int i1 = 0;
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();
            k++;
            i1 += i;
            if (obj instanceof String) {
                String s = (String) obj;
                i1 += getStringSize(s, i, j);
            }
        }

        if (list instanceof LinkedList)
            l += (k + 1) * (12 + i) + 8;
        else if (list instanceof ArrayList) {
            ArrayList arraylist = (ArrayList) list;
            arraylist.trimToSize();
            l += j + k * 4;
        }
        return l + i1;
    }

    public static int getStringSize(String s, int i, int j) {
        return s != null ? i + s.length() * 2 + j + 16 : 0;
    }
}
