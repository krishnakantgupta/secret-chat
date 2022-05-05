package com.kk.secretchat;

import java.util.Date;

public class Utils {

    public static long getTimeDifferenceInMinutes(long oldDate, long newDate) {
        long difference = newDate - oldDate;

        long difference_In_Minutes
                = (difference / (1000 * 60)) % 60;
        return difference_In_Minutes;
    }

    public static String getLastSeenTime(long oldDate) {
        String lastSeen ="Last Seen: ";
        long difference = new Date().getTime() - oldDate;
        long difference_In_Minutes = 0;
        long difference_In_Hours = 0;
        long difference_In_Days = 0;

        long difference_In_seconds = (difference / 1000) % 60;


        difference_In_Minutes = (difference / (1000 * 60)) % 60;


        difference_In_Hours = (difference / (1000 * 60 * 60)) % 24;


//        difference_In_Days = (difference / (1000 * 60 * 60 * 24)) % 365;
        if (difference_In_Hours != 0) {
            lastSeen +=  " " +difference_In_Hours;
            if (difference_In_Minutes != 0) {
                lastSeen +=  "hr and " +difference_In_Minutes +" mins";
            }
            lastSeen += " ago";

        }else  if (difference_In_Minutes != 0) {
            lastSeen +=  " " +difference_In_Minutes +" mins ago";
        }else{
            lastSeen +=  " " +difference_In_seconds +" seconds ago";
        }
        return lastSeen;

    }

    public static long getTimeDifferenceInSec(long oldDate, long newDate) {
        long difference = newDate - oldDate;
        long difference_In_seconds
                = (difference / 1000) % 60;
        return difference_In_seconds;


//        long difference_In_Hours
//                = (difference_In_Time
//                / (1000 * 60 * 60))
//                % 24;


//        long difference_In_Days
//                = (difference_In_Time
//                / (1000 * 60 * 60 * 24))
//                % 365;
    }
}
