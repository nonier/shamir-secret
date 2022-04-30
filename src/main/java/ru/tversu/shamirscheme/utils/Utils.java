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

    public static int minusMod(int x, int p) {
        return (x % p + p) % p;
    }
}
