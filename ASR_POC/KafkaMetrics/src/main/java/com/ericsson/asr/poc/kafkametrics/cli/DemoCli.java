/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.asr.poc.kafkametrics.cli;

import static com.ericsson.asr.poc.kafkametrics.constants.Constants.*;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;

public class DemoCli {

    private final CommandLine cmd;

    public DemoCli(final String[] args) throws ParseException {
        final CommandLineParser cmdLineParser = new BasicParser();
        this.cmd = cmdLineParser.parse(new DemoCliOptions(), args);
    }

    public String getAction() {
        final String action = cmd.getOptionValue(ACTION);
        return action;
    }

    public long getNumberOfRecords() {
        final String numberOfRecords = cmd.getOptionValue(NUMBER_OF_RECORDS);
        return Long.parseLong(numberOfRecords);
    }

    public String getTopicName() {
        final String topicName = cmd.getOptionValue(TOPIC_NAME);
        return topicName;
    }

    public String getPropertiesFile() {
        final String propertiesFile = cmd.getOptionValue(PROPERTIES_FILE);
        return propertiesFile;
    }

}
