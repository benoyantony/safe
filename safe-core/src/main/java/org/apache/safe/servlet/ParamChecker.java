/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. See accompanying LICENSE file.
 */
package org.apache.safe.servlet;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.safe.util.DateUtils;
import org.apache.safe.util.LogUtils;

/**
 * Utility class to check common parameter preconditions.
 */
public class ParamChecker {

    /**
     * Check that a value is not null. If null throws an IllegalArgumentException.
     *
     * @param obj value.
     * @param name parameter name for the exception message.
     * @return the given value.
     */
    public static <T> T notNull(T obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        return obj;
    }

    /**
     * Check that a list is not null and that none of its elements is null. If null or if the list has emtpy elements
     * throws an IllegalArgumentException.
     *
     * @param list the list of strings.
     * @param name parameter name for the exception message.
     * @return the given list.
     */
    public static <T> List<T> notNullElements(List<T> list, String name) {
        notNull(list, name);
        for (int i = 0; i < list.size(); i++) {
            notNull(list.get(i), LogUtils.format("list [{0}] element [{1}]", name, i));
        }
        return list;
    }

    /**
     * Check that a string is not null and not empty. If null or emtpy throws an IllegalArgumentException.
     *
     * @param str value.
     * @param name parameter name for the exception message.
     * @return the given value.
     */
    public static String notEmpty(String str, String name) {
        if (str == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        if (str.length() == 0) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        return str;
    }

    /**
     * Check that a list is not null and that none of its elements is null. If null or if the list has emtpy elements
     * throws an IllegalArgumentException.
     *
     * @param list the list of strings.
     * @param name parameter name for the exception message.
     * @return the given list.
     */
    public static List<String> notEmptyElements(List<String> list, String name) {
        notNull(list, name);
        for (int i = 0; i < list.size(); i++) {
            notEmpty(list.get(i), LogUtils.format("list [{0}] element [{1}]", name, i));
        }
        return list;
    }

    /**
     * Return if the specified token is a valid Java identifier.
     *
     * @param token string to validate if it is a valid Java identifier.
     * @return if the specified token is a valid Java identifier.
     */
    public static boolean isValidIdentifier(String token) {
        ParamChecker.notEmpty(token, "identifier");
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (!(c >= '0' && c <= '9') && !(c >= 'A' && c <= 'Z') && !(c >= 'a' && c <= 'z') && !(c == '_')) {
                return false;
            }
            if (i == 0 && (c >= '0' && c <= '9')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether the value is greater than or equals 0.
     *
     * @param value : value to test
     * @param name : Name of the parameter
     * @return If the value is > 0, return the value. Otherwise throw IllegalArgumentException
     */
    public static int checkGTZero(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(LogUtils.format("parameter [{0}] = [{1}] must be greater than zero", name,
                                                           value));
        }
        return value;
    }

    /**
     * Check whether the value is greater than or equals to 0.
     *
     * @param value : value to test
     * @param name : Name of the parameter
     * @return If the value is >= 0, return the value. Otherwise throw IllegalArgumentException
     */
    public static int checkGEZero(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(LogUtils.format(
                    "parameter [{0}] = [{1}] must be greater than or equals zero", name, value));
        }
        return value;
    }

    /**
     * Check whether the value is Integer.
     *
     * @param value : value to test
     * @param name : Name of the parameter
     * @return If the value is integer, return the value. Otherwise throw IllegalArgumentException
     */
    public static int checkInteger(String val, String name) {
        int ret;
        try {
            ret = Integer.parseInt(val);
        }
        catch (NumberFormatException nex) {
            throw new IllegalArgumentException(LogUtils.format(
                    "parameter [{0}] = [{1}]  must be an integer. Parsing error {2}", name, val, nex));
        }
        return ret;
    }

    /**
     * Check whether the value is UTC data format.
     *
     * @param value : value to test
     * @param name : Name of the parameter
     * @return If the value is in UTC date format, return the value. Otherwise throw IllegalArgumentException
     */
    public static Date checkUTC(String date, String name) {
        Date ret;
        try {
            ret = DateUtils.parseDateUTC(date);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(LogUtils.format(
                    "parameter [{0}] = [{1}] must be Date in UTC format (yyyy-MM-dd'T'HH:mm'Z')."
                            + " Parsing error {2}", name, date, ex));
        }
        return ret;
    }

    /**
     * Check whether the value mention correct Timezone.
     *
     * @param value : value to test
     * @param name : Name of the parameter
     * @return If the value is correct TZ return the value. Otherwise throw IllegalArgumentException
     */
    public static TimeZone checkTimeZone(String tzStr, String name) {
        TimeZone tz;
        try {
            tz = DateUtils.getTimeZone(tzStr);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(LogUtils.format("parameter [{0}] = [{1}] must be a valid TZ."
                    + " Parsing error {2}", name, tzStr, ex));
        }
        return tz;
    }

    /**
     * Check whether an item is a member of an array of string
     *
     * @param item : item to test
     * @param members : List of items in string
     * @param name : Name of the parameter
     * @return If the item is in the member return true. Otherwise throw IllegalArgumentException
     */
    public static boolean isMember(String item, String[] members, String name) {
        for (int i = 0; i < members.length; i++) {
            if (members[i].equals(item)) {
                return true;
            }
        }
        // Error case
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < members.length; i++) {
            buff.append(members[i]).append(", ");
        }
        throw new IllegalArgumentException(LogUtils.format("parameter [{0}] = [{1}] " + "must be in the list {2}", name,
                                                       item, buff.toString()));
    }
}
