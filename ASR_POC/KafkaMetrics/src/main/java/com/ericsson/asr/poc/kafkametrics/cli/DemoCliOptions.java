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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

@SuppressWarnings("serial")
public class DemoCliOptions extends Options {

    public DemoCliOptions() {
        addOption(createActionOption());
        addOption(createNumberOfRecordsOption());
        addOption(createTopicNameOption());
        addOption(createPropertiesFileOption());
    }

    private Option createActionOption() {
        final Option option = new Option(ACTION, true, "Action is Producer or Consumer");
        option.setType(String.class);
        option.setRequired(true);
        return option;
    }

    private Option createNumberOfRecordsOption() {
        final Option option = new Option(NUMBER_OF_RECORDS, true, "Number of records that will be generated.");
        option.setType(String.class);
        option.setRequired(true);
        return option;
    }

    private Option createTopicNameOption() {
        final Option option = new Option(TOPIC_NAME, true, "Name of the topic in kafka that the records will be published to.");
        option.setType(String.class);
        option.setRequired(true);
        return option;
    }

    private Option createPropertiesFileOption() {
        final Option option = new Option(PROPERTIES_FILE, true, "Full path to the properties file");
        option.setType(String.class);
        option.setRequired(true);
        return option;
    }

}
