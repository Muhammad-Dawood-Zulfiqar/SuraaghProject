package com.example.suraagh_deliverable_1.AI;

import java.util.concurrent.TimeUnit;

public class DateUtils {

    /**
     * Calculates score based on time difference between two timestamps.
     * @param timestampLost Time item was lost (milliseconds)
     * @param timestampFound Time item was found (milliseconds)
     * @return Score between 0.0 (Too far) and 1.0 (Same day)
     */
    public static double calculateTimeScore(long timestampLost, long timestampFound) {
        // Calculate absolute difference in milliseconds
        long diffInMillis = Math.abs(timestampFound - timestampLost);

        // Convert to days
        long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        // --- SCORING LOGIC ---

        if (diffInDays == 0) return 1.0;      // Same day match
        if (diffInDays <= 3) return 0.9;      // Very close
        if (diffInDays <= 7) return 0.7;      // Within a week
        if (diffInDays <= 30) return 0.4;     // Within a month (maybe late post)

        return 0.1; // More than a month apart -> Not a match
    }
}