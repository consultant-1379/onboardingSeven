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
package com.ericsson.asr.poc.kafkametrics.utils;

import java.io.FileInputStream;
import java.util.Properties;

public class Utils {

    public static Properties readPropertiesFile(final String propertyFile) throws Exception {
        final Properties properties = new Properties();

        final FileInputStream inputStream = new FileInputStream(propertyFile);
        properties.load(inputStream);

        return properties;
    }
}