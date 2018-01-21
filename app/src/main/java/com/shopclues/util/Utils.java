package com.shopclues.util;

import java.util.List;

/**
 * Created by root on 2/8/17.
 */

public class Utils {

    public static boolean objectvalidator(Object o) {
        if (o == null)
            return false;
        if (o instanceof String && o != null && ((String) o).length() > 0)
            return true;
        else if (o instanceof List<?> && o != null && ((List) o).size() > 0)
            return true;


        return false;
    }


}
