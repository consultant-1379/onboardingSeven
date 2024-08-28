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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class contains various constants used throughout the troubleshooting tool.
 */
public class CslConstants {

    // Usage message.
    public static final String USAGE_MSG = "Usage: Following options are supported:\njava -jar CslTroubleshootingTool -r <Path to CellTrace>\n\tDecode CellTrace, generate the sessions and give an indication of which CSL files to investigate\n\njava -jar CslTroubleshootingTool -fn <Path to CellTrace> <Path to CSL files> <Node Id>\n\tSeach CSL files for files that contain same date/time as the CellTrace for the given eNodeB\n\njava -jar CslTroubleshootingTool -fp <Path to CellTrace> <Path to CSL files> <regex>\n\tSeach CSL files for files that contain same date/time as the CellTrace for the given pattern\n\njava -jar CslTroubleshootingTool -fcp <Path to CellTrace> <MMES1APID (decimal or hex with leading 0x)> <date and time in 24 hour format, yyyyMMddHHmmss>\n\tSeach CellTrace (using the .session files) for files that contain an event that has the provided MMES1APID in an event at the provided time";

    // Input switches.
    public static final String RAW_ANALYSIS = "-r";
    public static final String SEARCH_CSL_BY_NODE_ID = "-fn";
    public static final String SEARCH_CSL_BY_PATTERN = "-fp";
    public static final String SEARCH_CELLTRACE_BY_PATTERN = "-fcp";

    // File handling
    public static final String CELLTRACE_EXTENSION = ".bin.gz";
    public static final String DECODED_EXTENSION = ".decoded.txt";
    public static final String CSV_EXTENSION = ".csv";
    public static final String SESSION_EXTENSION = ".sessions.txt";

    // Event handling
    public static final String EVENT_STRING_START_DELIMITER = "recordType 4,";
    public static final String EVENT_STRING_END_DELIMITER = "EVENT_PARAM_TRACE_RECORDING_SESSION_REFERENCE";

    // Resource directories
    private static final String RESOURCES_DIR = "CSLDecoder\\";
    private static final String WARNINGS_DIR = "warnings\\";
    private static final String FILE_DIR = "File\\";
    private static final String SPEC_DIR = "Spec\\";
    private static final String TOOLS_DIR = "Tools\\";
    private static final String TOOLS1_DIR = RESOURCES_DIR + "tools\\";
    private static final String LTNG_DIR = "ltng\\";
    private static final String ETC_DIR = TOOLS1_DIR + LTNG_DIR + "etc\\";
    private static final String LIB_DIR = TOOLS1_DIR + LTNG_DIR + "lib\\";
    static final String[] DIRECTORIES = { RESOURCES_DIR, WARNINGS_DIR, TOOLS_DIR, FILE_DIR, FILE_DIR + SPEC_DIR, TOOLS1_DIR, TOOLS1_DIR + LTNG_DIR,
            ETC_DIR, LIB_DIR };
    public static final String INPUT_RESOURCES_DIR = "CslDecoderResources/";

    // Resource names
    private static final String PERL_NAME = "perl.exe";
    private static final String LTNG_NAME = "ltng-decoder";

    // Resources
    public static final Map<String, String> RESOURCES;
    static {
        final Map<String, String> map = new HashMap<>();
        map.put(PERL_NAME, RESOURCES_DIR + PERL_NAME);
        map.put("cygperl5_22.dll", RESOURCES_DIR + "cygperl5_22.dll");
        map.put("cygcrypt-0.dll", RESOURCES_DIR + "cygcrypt-0.dll");
        map.put("cygwin1.dll", RESOURCES_DIR + "cygwin1.dll");
        map.put("cyggcc_s-1.dll", RESOURCES_DIR + "cyggcc_s-1.dll");
        map.put("cygssp-0.dll", RESOURCES_DIR + "cygssp-0.dll");
        map.put(LTNG_NAME, RESOURCES_DIR + LTNG_NAME);
        map.put("batEvDec.pl", RESOURCES_DIR + "batEvDec.pl");
        map.put("Cwd.pm", "Cwd.pm");
        map.put("strict.pm", "strict.pm");
        map.put("Exporter.pm", "Exporter.pm");
        map.put("vars.pm", "vars.pm");
        map.put("register.pm", WARNINGS_DIR + "register.pm");
        map.put("warnings.pm", "warnings.pm");
        map.put("Basename.pm", FILE_DIR + "Basename.pm");
        map.put("lib.pm", "lib.pm");
        map.put("Config.pm", "Config.pm");
        map.put("Socket.pm", "Socket.pm");
        map.put("Carp.pm", "Carp.pm");
        map.put("XSLoader.pm", "XSLoader.pm");
        map.put("DynaLoader.pm", "DynaLoader.pm");
        map.put("Spec.pm", FILE_DIR + "Spec.pm");
        map.put("Cygwin.pm", FILE_DIR + SPEC_DIR + "Cygwin.pm");
        map.put("Unix.pm", FILE_DIR + SPEC_DIR + "Unix.pm");
        map.put("constant.pm", "constant.pm");
        map.put("TestTools.pm", "TestTools.pm");
        map.put("ltng.classpath", ETC_DIR + "ltng.classpath");
        map.put("cygpath.exe", "cygpath.exe");
        map.put("com.ericsson.testtools.cliutil-R2D.jar", LIB_DIR + "com.ericsson.testtools.cliutil-R2D.jar");
        map.put("com.fasterxml.jackson.core.jackson-core-2.6.3.jar", LIB_DIR + "com.fasterxml.jackson.core.jackson-core-2.6.3.jar");
        map.put("com.ericsson.testtools.usage.usage-reporter-R4M.jar", LIB_DIR + "com.ericsson.testtools.usage.usage-reporter-R4M.jar");
        map.put("com.ericsson.testtools.ltng.ltng-core-R135B.jar", LIB_DIR + "com.ericsson.testtools.ltng.ltng-core-R135B.jar");
        map.put("com.ericsson.testtools.ltng.ltng-cli-R135B.jar", LIB_DIR + "com.ericsson.testtools.ltng.ltng-cli-R135B.jar");
        map.put("com.ericsson.testtools.ltng.ltng-lte-R135B.jar", LIB_DIR + "com.ericsson.testtools.ltng.ltng-lte-R135B.jar");
        map.put("com.google.protobuf.protobuf-java-2.5.0.jar", LIB_DIR + "com.google.protobuf.protobuf-java-2.5.0.jar");
        map.put("com.ericsson.testtools.createifmodelwrat.ifmodelutil-R11A.jar",
                LIB_DIR + "com.ericsson.testtools.createifmodelwrat.ifmodelutil-R11A.jar");
        map.put("com.ericsson.testtools.createstridxml.createstridxml-core-R7B.jar",
                LIB_DIR + "com.ericsson.testtools.createstridxml.createstridxml-core-R7B.jar");
        map.put("com.google.code.gson.gson-2.6.2.jar", LIB_DIR + "com.google.code.gson.gson-2.6.2.jar");
        map.put("org.slf4j.slf4j-api-1.7.12.jar", LIB_DIR + "org.slf4j.slf4j-api-1.7.12.jar");
        map.put("com.ericsson.testtools.asn1.s1ap-R6D.jar", LIB_DIR + "com.ericsson.testtools.asn1.s1ap-R6D.jar");
        map.put("com.ericsson.testtools.asn1.ranap-R6B.jar", LIB_DIR + "com.ericsson.testtools.asn1.ranap-R6B.jar");
        map.put("ch.qos.logback.logback-classic-1.1.3.jar", LIB_DIR + "ch.qos.logback.logback-classic-1.1.3.jar");
        map.put("org.eclipse.tracecompass.ctf.core.ttd-2.0.0.201608051656.jar",
                LIB_DIR + "org.eclipse.tracecompass.ctf.core.ttd-2.0.0.201608051656.jar");
        map.put("com.ericsson.testtools.asn1.lppa-R9B.jar", LIB_DIR + "com.ericsson.testtools.asn1.lppa-R9B.jar");
        map.put("com.ericsson.testtools.rmiltng-R1A.jar", LIB_DIR + "com.ericsson.testtools.rmiltng-R1A.jar");
        map.put("com.ericsson.testtools.ltng.jango-core-R135B.jar", LIB_DIR + "com.ericsson.testtools.ltng.jango-core-R135B.jar");
        map.put("org.apache.httpcomponents.httpcore-4.4.6.jar", LIB_DIR + "org.apache.httpcomponents.httpcore-4.4.6.jar");
        map.put("commons-codec.commons-codec-1.9.jar", LIB_DIR + "commons-codec.commons-codec-1.9.jar");
        map.put("com.ericsson.testtools.asn1.rrcutran-R6D.jar", LIB_DIR + "com.ericsson.testtools.asn1.rrcutran-R6D.jar");
        map.put("commons-logging.commons-logging-1.2.jar", LIB_DIR + "commons-logging.commons-logging-1.2.jar");
        map.put("org.antlr.antlr-runtime-3.5.1.jar", LIB_DIR + "org.antlr.antlr-runtime-3.5.1.jar");
        map.put("com.ericsson.testtools.ltng.jango-protocol-lte-R135B.jar", LIB_DIR + "com.ericsson.testtools.ltng.jango-protocol-lte-R135B.jar");
        map.put("com.ericsson.testtools.ltng.jango-reader-lte-R135B.jar", LIB_DIR + "com.ericsson.testtools.ltng.jango-reader-lte-R135B.jar");
        map.put("dom4j.dom4j-2.0.0-ALPHA-2.jar", LIB_DIR + "dom4j.dom4j-2.0.0-ALPHA-2.jar");
        map.put("org.apache.httpcomponents.httpclient-4.5.3.jar", LIB_DIR + "org.apache.httpcomponents.httpclient-4.5.3.jar");
        map.put("com.ericsson.testtools.asn1.rrc-R6E.jar", LIB_DIR + "com.ericsson.testtools.asn1.rrc-R6E.jar");
        map.put("com.ericsson.testtools.ltng.jango-protocol-common-R135B.jar",
                LIB_DIR + "com.ericsson.testtools.ltng.jango-protocol-common-R135B.jar");
        map.put("com.objsys.asn1j.asn1rt-7.0.0.jar", LIB_DIR + "com.objsys.asn1j.asn1rt-7.0.0.jar");
        map.put("com.ericsson.testtools.createprotobufjarwrat.createprotobufjarwrat-if-R6A.jar",
                LIB_DIR + "com.ericsson.testtools.createprotobufjarwrat.createprotobufjarwrat-if-R6A.jar");
        map.put("com.ericsson.testtools.asn1.m3ap-R6B.jar", LIB_DIR + "com.ericsson.testtools.asn1.m3ap-R6B.jar");
        map.put("ch.qos.logback.logback-core-1.1.3.jar", LIB_DIR + "ch.qos.logback.logback-core-1.1.3.jar");
        map.put("com.ericsson.testtools.usage.usage-shared-R4M.jar", LIB_DIR + "com.ericsson.testtools.usage.usage-shared-R4M.jar");
        map.put("args4j.args4j-2.0.29.jar", LIB_DIR + "args4j.args4j-2.0.29.jar");
        map.put("org.simpleframework.simple-xml-2.7.1.jar", LIB_DIR + "org.simpleframework.simple-xml-2.7.1.jar");
        map.put("org.eclipse.tracecompass.common.core-2.0.0.201608051656.jar",
                LIB_DIR + "org.eclipse.tracecompass.common.core-2.0.0.201608051656.jar");
        map.put("commons-cli.commons-cli-1.2.jar", LIB_DIR + "commons-cli.commons-cli-1.2.jar");
        map.put("org.eclipse.tracecompass.ctf.parser-1.0.0.201608051656.jar", LIB_DIR + "org.eclipse.tracecompass.ctf.parser-1.0.0.201608051656.jar");
        map.put("com.fasterxml.jackson.core.jackson-annotations-2.6.0.jar", LIB_DIR + "com.fasterxml.jackson.core.jackson-annotations-2.6.0.jar");
        map.put("com.google.guava.guava-15.0.jar", LIB_DIR + "com.google.guava.guava-15.0.jar");
        map.put("com.ericsson.testtools.asn1.nbap-R6A.jar", LIB_DIR + "com.ericsson.testtools.asn1.nbap-R6A.jar");
        map.put("com.ericsson.testtools.ltetracetools.ltt-remote-R8C.jar", LIB_DIR + "com.ericsson.testtools.ltetracetools.ltt-remote-R8C.jar");
        map.put("com.ericsson.testtools.asn1.x2ap-R6H.jar", LIB_DIR + "com.ericsson.testtools.asn1.x2ap-R6H.jar");
        map.put("com.ericsson.testtools.ltng.jango-protocol-wcdma-R135B.jar", LIB_DIR + "com.ericsson.testtools.ltng.jango-protocol-wcdma-R135B.jar");
        map.put("org.hsqldb.hsqldb-2.3.2.jar", LIB_DIR + "org.hsqldb.hsqldb-2.3.2.jar");
        map.put("com.fasterxml.jackson.core.jackson-databind-2.6.3.jar", LIB_DIR + "com.fasterxml.jackson.core.jackson-databind-2.6.3.jar");
        map.put("com.ericsson.testtools.ltng.jango-reader-common-R135B.jar", LIB_DIR + "com.ericsson.testtools.ltng.jango-reader-common-R135B.jar");
        map.put("jaxen.jaxen-1.1.6.jar", LIB_DIR + "jaxen.jaxen-1.1.6.jar");
        RESOURCES = Collections.unmodifiableMap(map);
    }

    // Commands
    public static final String PERL_CMD = RESOURCES_DIR + PERL_NAME;
    public static final String LTNG_CMD = RESOURCES_DIR + LTNG_NAME + " -f ";

    /**
     * Returns a date formatter for handling time stamps in Japanese Standard Time (JST). This is the time zone used in the <b>names</b> of the CSL
     * output files; in this instance the time stamp will be formatted as yyyy-MM-dd HH:mm for reading convenience.
     *
     * @return a DateFormat object with the mask "yyyy-MM-dd HH:mm" and TimeZone of Japan applied
     */
    public static final DateFormat getJstFormat() {
        final SimpleDateFormat jstFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        jstFormat.setTimeZone(TimeZone.getTimeZone("Japan"));
        return jstFormat;
    }

    /**
     * Returns a date formatter for handling time stamps in Japanese Standard Time (JST). This is the time zone used in the <b>names</b> of the CSL
     * output files; in this instance the time stamp will be formatted as yyyyMMddHHmm, matching the exact mask used in the file names.
     *
     * @return a DateFormat object with the mask "yyyyMMddHHmm" and TimeZone of Japan applied
     */
    public static final DateFormat getCslFileNameTimeStampFormat() {
        final SimpleDateFormat cslFormat = new SimpleDateFormat("yyyyMMddHHmm");
        cslFormat.setTimeZone(TimeZone.getTimeZone("Japan"));
        return cslFormat;
    }

    /**
     * Returns a date formatter for handling time stamps in UTC time. This is the time zone used in the <b>names</b> of the CellTrace files; in this
     * instance the tiem stamp will be formatted as yyyyMMdd.HHmm, matching the date and start time of the ROP as in the CellTrace file name.
     *
     * @return a DateFormat object with the mask "yyyyMMdd.HHmm" and TimeZone of UTC applied
     */
    public static DateFormat getCellTraceFileNameTimeStampFormat() {
        final SimpleDateFormat utcFormat = new SimpleDateFormat("yyyyMMdd.HHmm");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return utcFormat;
    }

    /**
     * Returns a date formatter for handling time stamps assuming they are formatted as they are in the CSL <b>records</b>, i.e. yyyyMMddHHmmss.
     * Assumes no time zone, just used for manipulating date objects with this mask.
     *
     * @return a DateFormat object with the mask "yyyyMMddHHmmss" and no TimeZone applied
     */
    public static DateFormat getCslRecordTimeStampFormat() {
        return new SimpleDateFormat("yyyyMMddHHmmss");
    }
}
