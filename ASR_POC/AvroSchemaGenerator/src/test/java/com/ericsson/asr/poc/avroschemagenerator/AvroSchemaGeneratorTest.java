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
package com.ericsson.asr.poc.avroschemagenerator;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.ericsson.asr.poc.avroschemagenerator.AvroSchemaGenerator;
import com.ericsson.asr.poc.avroschemagenerator.Event;
import com.ericsson.asr.poc.avroschemagenerator.General;
import com.ericsson.asr.poc.avroschemagenerator.Param;
import com.ericsson.asr.poc.avroschemagenerator.ParameterType;
import com.ericsson.asr.poc.avroschemagenerator.PredefinedEventGroup;
import com.ericsson.asr.poc.avroschemagenerator.Protocol;
import com.ericsson.asr.poc.avroschemagenerator.StructureType;

public class AvroSchemaGeneratorTest {

    private static final String LONG = "long";
	private static final String INT = "int";

	@Test
    public void getEvents() throws ParserConfigurationException, SAXException, IOException {

        List<Event> expected = new ArrayList<Event>();
        Event event1 = new Event();
        event1.setName("INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED");
        List<Param> event1Elements = new ArrayList<Param>();
        event1Elements.add(new Param("EVENT_PARAM_1_SECTOR_UE_DL_DISTR", "P_1_SECTOR_UE_DL_DISTR"));        
        event1Elements.add(new Param("EVENT_PARAM_1_SECTOR_UE_UL_DISTR", "P_1_SECTOR_UE_UL_DISTR"));
        event1Elements.add(new Param("EVENT_PARAM_3GPP_CAUSE_GROUP", "P_3GPP_CAUSE_GROUP"));
        event1.setElements(event1Elements);

        Event event2 = new Event();
        event2.setName("INTERNAL_EVENT_ADMISSION_BLOCKING_STOPPED");
        List<Param> event2Elements = new ArrayList<Param>();
        event2Elements.add(new Param("EVENT_PARAM_1_SECTOR_UE_DL_DISTR", "P_1_SECTOR_UE_DL_DISTR_1"));
        event2Elements.add(new Param("EVENT_PARAM_1_SECTOR_UE_DL_DISTR", "P_1_SECTOR_UE_DL_DISTR_2"));
        event2Elements.add(new Param("EVENT_PARAM_1_SECTOR_UE_UL_DISTR", "P_1_SECTOR_UE_UL_DISTR"));
        event2.setElements(event2Elements);

        expected.add(event1);
        expected.add(event2);

        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();

        cellTrace.convertXmlToJson("src/test/resources/", new File("src/test/resources/cell_trace_xmls/cell_trace_test.xml"), "celltrace");

        List<Event> actual = cellTrace.getEvents();

        assertEquals(expected, actual);
    }

    @Test
    public void getParameterTypes() throws ParserConfigurationException, SAXException, IOException {

        List<ParameterType> expected = new ArrayList<ParameterType>();
        ParameterType parameterType1 = new ParameterType();
        parameterType1.setName("EVENT_PARAM_1_SECTOR_UE_DL_DISTR");
        parameterType1.setType(LONG);
        parameterType1.setNoOfBits(32);
        parameterType1.setLow(0);
        parameterType1.setHigh(2147483650L);

        ParameterType parameterType2 = new ParameterType();
        parameterType2.setName("EVENT_PARAM_1_SECTOR_UE_UL_DISTR");
        parameterType2.setType(INT);
        parameterType2.setNoOfBits(32);
        parameterType2.setLow(0);
        parameterType2.setHigh(2147483647);

        ParameterType parameterType3 = new ParameterType();
        parameterType3.setName("EVENT_PARAM_3GPP_CAUSE_GROUP");
        parameterType3.setType(INT);
        parameterType3.setNoOfBits(8);
        parameterType3.setLow(0);
        parameterType3.setHigh(4);

        expected.add(parameterType1);
        expected.add(parameterType2);
        expected.add(parameterType3);

        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();

        cellTrace.convertXmlToJson("src/test/resources/", new File("src/test/resources/cell_trace_xmls/cell_trace_test.xml"), "celltrace");

        List<ParameterType> actual = cellTrace.getParameterTypes();

        assertEquals(expected, actual);
    }

    @Test
    public void getPredefinedEventGroups() throws ParserConfigurationException, SAXException, IOException {
        List<PredefinedEventGroup> expected = new ArrayList<PredefinedEventGroup>();

        PredefinedEventGroup eventGroup1 = new PredefinedEventGroup();
        eventGroup1.setName("SESSION_ESTABLISHMENT_EVALUATION");
        List<String> events1 = new ArrayList<String>();
        events1.add("INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED");
        events1.add("INTERNAL_EVENT_ADMISSION_BLOCKING_STOPPED");
        eventGroup1.setEvents(events1);

        PredefinedEventGroup eventGroup2 = new PredefinedEventGroup();
        eventGroup2.setName("SESSION_ESTABLISHMENT_EVALUATION_2");
        List<String> events2 = new ArrayList<String>();
        events2.add("INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED");
        events2.add("INTERNAL_EVENT_ADMISSION_BLOCKING_STOPPED");
        eventGroup2.setEvents(events2);

        expected.add(eventGroup1);
        expected.add(eventGroup2);

        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();

        cellTrace.convertXmlToJson("src/test/resources/", new File("src/test/resources/cell_trace_xmls/cell_trace_test.xml"), "celltrace");

        List<PredefinedEventGroup> actual = cellTrace.getEventGroups();

        assertEquals(expected, actual);
    }

    @Test
    public void getGeneral() throws ParserConfigurationException, SAXException, IOException {
        General expected = new General();
        expected.setDocNumber("CXC1735777/21");
        expected.setRevision("R5A");
        expected.setDate("2013-11-14");
        expected.setAuthor("Generated from 10/ CAH1091864/21-R5A, revision: R5A, by OMF");
        expected.setFfv("T");
        List<Protocol> protocols = new ArrayList<Protocol>();
        Protocol p1 = new Protocol("RRC", "36.331", "11.4.0");
        Protocol p2 = new Protocol("S1AP", "36.413", "10.5.0");
        Protocol p3 = new Protocol("X2AP", "36.423", "11.4.0");
        Protocol p4 = new Protocol("MBMS-M3", "36.444", "11.0.0");
        protocols.add(p1);
        protocols.add(p2);
        protocols.add(p3);
        protocols.add(p4);

        expected.setProtocols(protocols);

        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();

        cellTrace.convertXmlToJson("src/test/resources/", new File("src/test/resources/cell_trace_xmls/cell_trace_test.xml"), "celltrace");
        General actual = cellTrace.getGeneral();

        assertEquals(expected, actual);
    }

    @Test
    public void getStructureTypes() throws ParserConfigurationException, SAXException, IOException {

        List<StructureType> expected = new ArrayList<StructureType>();
        StructureType structureType1 = new StructureType();
        structureType1.setName("TIMESTAMP");
        List<Param> params1 = new ArrayList<Param>();        
        params1.add(new Param("BYTE_HOUR", "BYTE_HOUR"));
        params1.add(new Param("BYTE_MINUTE", "BYTE_MINUTE"));
        params1.add(new Param("BYTE_SECOND", "BYTE_SECOND"));
        structureType1.setElements(params1);

        StructureType structureType2 = new StructureType();
        structureType2.setName("GUMMEI");
        List<Param> params2 = new ArrayList<Param>();
        params2.add(new Param("PLMN_IDENTITY", "PLMN_IDENTITY"));
        params2.add(new Param("MMEGI", "MME_GROUP_ID"));
        params2.add(new Param("MMEC", "MME_CODE"));
        structureType2.setElements(params2);

        StructureType structureType3 = new StructureType();
        structureType3.setName("ENODEB_ID");
        List<Param> params3 = new ArrayList<Param>();
        params3.add(new Param("MACRO_ENODEB_ID", "MACRO_ENODEB_ID"));
        params3.add(new Param("HOME_ENODEB_ID", "HOME_ENODEB_ID"));
        structureType3.setElements(params3);

        expected.add(structureType1);
        expected.add(structureType2);
        expected.add(structureType3);

        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();

        cellTrace.convertXmlToJson("src/test/resources/", new File("src/test/resources/ctum_xmls/ctum_test.xml"), "ctum");

        List<StructureType> actual = cellTrace.getStructuretypes();

        assertEquals(expected, actual);
    }

    @Test
    public void validateCreatedJsonStartedEvent() throws ParserConfigurationException, SAXException, IOException {

        String testFile = "src/test/resources/INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED.avsc";
        String realFile = "src/test/resources/celltrace.t.r5a.v21/INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED.avsc";

        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();

        cellTrace.convertXmlToJson("src/test/resources/", new File("src/test/resources/cell_trace_xmls/cell_trace_test.xml"), "celltrace");
        String expected = readFromFile(testFile);
        String actual = readFromFile(realFile);

        assertEquals(expected, actual);
    }

    @Test
    public void validateCreatedJsonStoppedEvent() throws ParserConfigurationException, SAXException, IOException {

        String testFile = "src/test/resources/INTERNAL_EVENT_ADMISSION_BLOCKING_STOPPED.avsc";
        String realFile = "src/test/resources/celltrace.t.r5a.v21/INTERNAL_EVENT_ADMISSION_BLOCKING_STOPPED.avsc";

        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();

        cellTrace.convertXmlToJson("src/test/resources/", new File("src/test/resources/cell_trace_xmls/cell_trace_test.xml"), "celltrace");
        String expected = readFromFile(testFile);
        String actual = readFromFile(realFile);

        assertEquals(expected, actual);
    }
    
    
    @Test
    public void validateTestOrderingRealEvent() throws ParserConfigurationException, SAXException, IOException {

        String testFile = "src/test/resources/INTERNAL_EVENT_ADV_CELL_SUP_RECOVERY_ATTEMPT.avsc";
        String realFile = "src/test/resources/celltrace.t.r5a.v21/INTERNAL_EVENT_ADV_CELL_SUP_RECOVERY_ATTEMPT.avsc";

        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();

        cellTrace.convertXmlToJson("src/test/resources/", new File("src/test/resources/cell_trace_xmls/cell_trace_test_3.xml"), "celltrace");
        String expected = readFromFile(testFile);
        String actual = readFromFile(realFile);

        assertEquals(expected, actual);
    }

    @Test
    public void getNodeDirectoryVersion_CellTrace() throws ParserConfigurationException, SAXException, IOException {
        // format is celltrace_<ffv>.<revision>.<the text after the forward slash in the docNo tag>

        String[] expectedDirs = { "celltrace.t.r5a.v21" };
        String xmlFile = "src/test/resources/cell_trace_xmls/cell_trace_test.xml";
        String feature = "celltrace";

        getNodeDirectoryVersion(expectedDirs, xmlFile, feature);
    }

    @Test
    public void getNodeDirectoryVersion_CellTrace_2() throws ParserConfigurationException, SAXException, IOException {
        // format is celltrace_<ffv>.<revision>.<the text after the forward slash in the docNo tag>

        String[] expectedDirs = { "celltrace.t.r5a" };
        String xmlFile = "src/test/resources/cell_trace_xmls/cell_trace_test_2.xml";
        String feature = "celltrace";

        getNodeDirectoryVersion(expectedDirs, xmlFile, feature);
    }

    @Test
    public void getNodeDirectoryVersion_CTUM() throws ParserConfigurationException, SAXException, IOException {
        // format is ctum.<ffv>.<fiv>
        String[] expectedDirs = { "ctum.v2.v2" };
        String xmlFile = "src/test/resources/ctum_xmls/ctum_test.xml";
        String feature = "ctum";

        getNodeDirectoryVersion(expectedDirs, xmlFile, feature);
    }

    public void getNodeDirectoryVersion(String[] expectedDirs, String xmlFile, String feature) throws ParserConfigurationException, SAXException,
            IOException {
        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();
        cellTrace.convertXmlToJson("src/test/resources/", new File(xmlFile), feature);

        File file = new File("src/test/resources");
        String[] directories = file.list(new FilenameFilter() {
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        String[] actualDirs = directories;

        int expected = expectedDirs.length;
        int actual = 0;

        for (String expectedDir : expectedDirs) {
            for (String actualDir : actualDirs) {
                if (expectedDir.equals(actualDir)) {
                    actual++;
                }
            }
        }

        assertEquals(expected, actual);
    }

    private String readFromFile(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        StringBuilder expected = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                expected.append(scanner.nextLine() + lineSeparator);
            }
        } finally {
            scanner.close();
        }

        return expected.toString();
    }

    @After
    public void deleteTestDirs() {
        String[] dirs = { "src/test/resources/celltrace.t.r5a.v21", "src/test/resources/ctum.v2.v2",
                "src/test/resources/celltrace.t.r5a.v1022_HRB105500", "src/test/resources/celltrace.t.r5a" };

        for (String dirName : dirs) {
            File dir = new File(dirName);

            if (dir.exists()) {
                deleteFiles(dir);
            }
        }
    }

    private void deleteFiles(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteFiles(f);
            }
        }
        file.delete();
    }
}
