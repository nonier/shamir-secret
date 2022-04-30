package ru.tversu.shamirscheme.utils;

public class Utils {

    public static boolean isSimple(Integer p) {
        for (int i = 2; i < p; i++) {
            if (p % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static int minusMod(Integer x, Integer p) {
        return (x % p + p) % p;
    }

    public static int getOpposite(int x, int p) {
        int result = 0;
        for (int i = 1; i > 0; i++) {
            if (((x * i) % p) == 1) {
                result = i;
                break;
            }
        }
        return result;
    }
}
