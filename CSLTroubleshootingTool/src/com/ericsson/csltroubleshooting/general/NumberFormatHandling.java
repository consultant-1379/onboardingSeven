/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.csltroubleshooting.general;

/**
 * This class contains methods for managing numbers, e.g. converting decimal to hexadecimal.
 */
public class NumberFormatHandling {

    /**
     * Convert a decimal (passed in as a String) to a hexadecimal, returned as a String with a lead "(0x" added
     *
     * @param decimal
     *            value in decimal
     * @return value in hexadecimal with "(0x" prepended
     */
    public static String getHex(final String decimal) {
        return "(0x" + Long.toHexString(Long.parseLong(decimal, 10));
    }

    /**
     * Convert a hexadecimal, with no leading "0x", to decimal
     *
     * @param hexadecimal
     *            value in hexadecimal
     * @return value in decimal
     */
    public static String getDecimal(final String hex) {
        return Integer.toString(Integer.parseInt(hex, 16));
    }

    /**
     * Validate that a string is a valid hexadecimal.
     *
     * @param hexadecimal
     * @return
     */
    public static boolean validateHex(final String hexadecimal) {
        boolean isValid = true;
        try {
            Long.parseLong(hexadecimal, 16);
        } catch (final Exception e) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Validate that a string is a valid decimal.
     *
     * @param decimal
     * @return
     */
    public static boolean validateDecimal(final String decimal) {
        boolean isValid = true;
        try {
            Integer.parseInt(decimal);
        } catch (final Exception e) {
            isValid = false;
        }
        return isValid;
    }
}
