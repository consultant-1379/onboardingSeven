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

import static org.junit.Assert.*;

import org.junit.*;

public class NumberFormatHandlingTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.NumberFormatHandling#getHex()}.
     */
    @Test
    public void testHexToDecimal() {
        assertTrue(NumberFormatHandling.getHex("123456").equals("(0x1e240"));
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.NumberFormatHandling#getDecimal()}.
     */
    @Test
    public void testDecimalToHex() {
        assertTrue(NumberFormatHandling.getDecimal("1e240").equals("123456"));
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.NumberFormatHandling#validateDecimal()}.
     */
    @Test
    public void testValidateDecimal() {
        assertTrue(NumberFormatHandling.validateDecimal("12345"));
        assertFalse(NumberFormatHandling.validateDecimal("1e34"));
    }

    /**
     * Test method for {@link com.ericsson.csltroubleshooting.general.NumberFormatHandling#validateHex()}.
     */
    @Test
    public void testValidateHex() {
        assertTrue(NumberFormatHandling.validateHex("12345"));
        assertTrue(NumberFormatHandling.validateHex("1e34"));
        assertFalse(NumberFormatHandling.validateHex("1z34"));
    }
}
