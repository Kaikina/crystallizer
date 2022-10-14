package com.tgirou.crystallizer.api.util;

import java.util.Random;

public class Utils {
    /**
     * Returns a random integer between min and max (all-inclusive)
     * @param min the lower bound
     * @param max the upper bound
     * @return a random int
     */
    public static int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max + 1 - min) + min;
    }
}
