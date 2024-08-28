package com.ericsson.asr.poc.avroschemagenerator;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class AvroSchemaGenerator {
    private String LONG = "long";
	private String STRING = "string";
	private String NAMESPACE = "namespace";
	private String RECORD = "record";
	private String NAME = "name";
	private String TYPE = "type";
	
	private List<Event> events = new ArrayList<Event>();
    private List<ParameterType> parameterTypes = new ArrayList<ParameterType>();
    private List<PredefinedEventGroup> eventGroups = new ArrayList<PredefinedEventGroup>();
    private List<StructureType> structuretypes = new ArrayList<StructureType>();
    private General general = new General(); 
    
    private String AVRO_FILE_EXTENSION = ".avsc";
    private String XML_EXTENSION = ".xml";
    private String INDENT = "    ";
    private String UINT = "UINT";
	private String ENUM = "ENUM";
	private String LONG_DATA_TYPE = "LONG";
	private String INTEGER = "INTEGER";
	private String FS = File.separator;

	private int MIN_INTEGER= -2147483648;
	private int MAX_INTEGER= 2147483647;
	
	public static void main(String args[]) throws ParserConfigurationException, SAXException, IOException {   	
        File inputDir = new File(args[0]);
        String outputDir = args[1];
        String feature = args[2];
        File[] dirList = inputDir.listFiles();
        AvroSchemaGenerator cellTrace = new AvroSchemaGenerator();
        cellTrace.convertXmlsToJson(cellTrace, dirList, outputDir, feature);
    }

    public void convertXmlsToJson(AvroSchemaGenerator cellTrace, File[] dirList, String outputDir, String feature) throws ParserConfigurationException,
            SAXException, IOException {
        for (File xmlFile : dirList) {
            if (xmlFile.getName().endsWith(XML_EXTENSION)) {
            	System.out.println("Generating Avro Schema from: " + xmlFile.getAbsolutePath());            	
                cellTrace.convertXmlToJson(outputDir, xmlFile, feature);
            }
        }        
    }

    protected void convertXmlToJson(String outputDir, File xmlFile, String feature) throws ParserConfigurationException, SAXException, IOException {
        extractXmlContent(xmlFile);
        String dirName = createDirectoryName(outputDir, feature);
        createDirectory(dirName);
        createJsonFiles(dirName);
        
        System.out.println("Schema Files generated in: " + dirName);
        System.out.println();
    }

    private void extractXmlContent(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);

        NodeList nodeList = document.getDocumentElement().getChildNodes();

        /** 
         * TODO: Refactor this to use an interface
         */
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node instanceof Element) {
                if (node.getNodeName().trim().equals("events")) {
                    events = getEvents(node);
                    continue;
                }

                if (node.getNodeName().trim().equals("parametertypes")) {
                    parameterTypes = getParameterTypes(node);
                    continue;
                }

                if (node.getNodeName().trim().equals("predefinedeventgroups")) {
                    eventGroups = getEventGroups(node);
                    continue;
                }

                if (node.getNodeName().trim().equals("general")) {
                    general = getGeneral(node);
                    continue;
                }

                if (node.getNodeName().trim().equals("structuretypes")) {
                    structuretypes = getStructureTypes(node);
                }
            }
        }
    }

    private String createDirectoryName(String userDefinedOutputDir, String feature) {
        String delimiter = ".";
        String ffv = prefixDigitWithVersion(general.getFfv().toLowerCase());
        String fiv = prefixDigitWithVersion(general.getFiv());

        String schemaDirName = getSchemaDirName(feature, delimiter, ffv, fiv);
        
        String defaultSchemaDirName = "src" + FS + "main" + FS + "resources" + FS + schemaDirName.toString();                
        String userDefinedOutputSchemaDirName = getUserDefinedOutputDir(userDefinedOutputDir, schemaDirName);

        String schemaOutputDirName = defaultSchemaDirName;
        
        if (!userDefinedOutputSchemaDirName.isEmpty()){
        	schemaOutputDirName = userDefinedOutputSchemaDirName;
        }

        return schemaOutputDirName;
    }

	private String getUserDefinedOutputDir(String userDefinedOutputDir, String schemaDirName) {
		
		String outputdirName = "";
		if (!userDefinedOutputDir.isEmpty()) {			
        	String seperator = FS;
        	if (userDefinedOutputDir.endsWith(FS)){
        		seperator = "";
        	}
        	outputdirName = userDefinedOutputDir + seperator + schemaDirName;
        }
		
		return outputdirName;
	}

	private String getSchemaDirName(String feature, String delimiter, String ffv, String fiv) {
		String schemaDirName;
		if (fiv == null) {
            String revision = prefixDigitWithVersion(general.getRevision().toLowerCase());
            String iteration = prefixDigitWithVersion(getIteration(general.getDocNumber()));

            schemaDirName = feature + delimiter + ffv + delimiter + revision;
            if (iteration != null) {
            	schemaDirName += delimiter + iteration;
            }
        } else {
        	schemaDirName = feature + delimiter + ffv + delimiter + fiv;
        }
		return schemaDirName;
	}

    private String getIteration(final String docNo) {
        if (docNo == null) {
            return null;
        }
        final String iterationNumber = docNo.substring(docNo.indexOf("/") + 1);

        return iterationNumber.matches("\\d+") ? iterationNumber : null;
    }

    private String prefixDigitWithVersion(String value) {
        if (value != null) {
            char firstCharacter = value.charAt(0);
            if (Character.isDigit(firstCharacter)) {
                value = "v" + value;
            }
        }

        return value;
    }

    private void createDirectory(String dirName) throws IOException {

        File dir = new File(dirName);

        if (dir.exists()) {
            deleteFiles(dir);
        }

        if (!dir.mkdirs()) {
            throw new IOException("Unable to create " + dirName);
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

    protected List<Event> getEvents() {
        return events;
    }

    protected List<ParameterType> getParameterTypes() {
        return parameterTypes;
    }

    protected List<PredefinedEventGroup> getEventGroups() {
        return eventGroups;
    }

    protected General getGeneral() {
        return general;
    }

    protected List<StructureType> getStructuretypes() {
        return structuretypes;
    }

    private void createJsonFiles(String dirName) throws FileNotFoundException {
        for (Event event : getEvents()) {
            createJsonFile(dirName, event);
        }
    }

    private void createJsonFile(String dirName, Event event) throws FileNotFoundException {
        String jsonText = createHeader(dirName, event);
        jsonText += createFields(event);
        int indent = 0;
        jsonText = wrapInCurlyBraces(jsonText, indent);
        String fileName = dirName + FS + event.getName() + AVRO_FILE_EXTENSION;

        File file = new File(fileName.toString());
        PrintWriter out = new PrintWriter(file);

        out.println(jsonText);
        out.flush();
        out.close();
    }

    private String createHeader(String dirName, Event event) {
    	int indent = 1;
    	
    	String type = setField(TYPE, RECORD, true, indent, true);
    	String name = setField(NAME, event.getName(), true, indent, true);
        String nameSpace = setField(NAMESPACE, getNameSpace(dirName), true, indent, true);

        String header = type + name + nameSpace;
        
        return header.toString();
    }

    private String getNameSpace(String dirName) {
        String nameSpace = dirName.substring(dirName.lastIndexOf(FS) + 1, dirName.length());

        return nameSpace;
    }

    private String createFields(Event event) {
        String values = getDefaultFields();
        int indent = 3;
        
        for (Param element : event.getElements()) {
            for (ParameterType paramType : parameterTypes) {
                String temp = "";
                
                if (element.getType().equals(paramType.getName())) {
                    
                    temp += setField(NAME, element.getName(), true, indent, true);
                    temp += setField(TYPE, paramType.getType(), false, indent, true);
                    temp = wrapInCurlyBraces(temp, (indent-1)) + ",";

                    values += temp;
                }
            }
        }

        indent = 1;
        String tmp = values.toString();
        String tmpFields = tmp.substring(0, tmp.length() - 1);
        String fields = setField("fields", wrapInSquareBrackets(tmpFields, (indent)), false, indent, false);

        return fields;
    }

    private String getDefaultFields() {
        String defaultFields = new String();
        int indent = 3;
        String temp = "";
        temp += setField(NAME, "_NE", true, indent, true);
        temp += setField(TYPE, STRING, false, indent, true);
        temp = wrapInCurlyBraces(temp, (indent-1)) + ",";
        defaultFields += temp;
        
        temp = "";
        temp += setField(NAME, "_TIMESTAMP", true, indent, true);
        temp += setField(TYPE, LONG, false, indent, true);
        temp = wrapInCurlyBraces(temp, (indent-1)) + ",";
        defaultFields += temp;

        return defaultFields;
    }

    private String setField(String key, String value, boolean setComma, int indentCount, boolean hasQuotes) {
        String indent = setIndent(indentCount);
        String comma = ",";
        if (!setComma) {
            comma = "";
        }
        
        if (hasQuotes){
        	value = "\"" + value + "\"";
        }
        
        String text = indent + "\"" + key + "\" : " + value + comma + "\n";

        return text;
    }
   

    private String wrapInCurlyBraces(String text, int indentCount) {
        String indent = setIndent(indentCount);
        
        return "\n" + indent + "{\n" + text + indent + "}";
    }

    /**
     * @param indentCount
     * @return
     */
    private String setIndent(int indentCount) {
        String indent = "";
        
        for (int i = 0; i < indentCount; i++){
            indent += INDENT;
        }
        return indent;
    }

    private String wrapInSquareBrackets(String text, int indentCount) {
        String indent = setIndent(indentCount);
        return "[" + text + "\n" + indent + "]";
    }

    private General getGeneral(Node node) {
        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                if (childNode.getNodeName().equals("docno")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    general.setDocNumber(content);
                    continue;
                }

                if (childNode.getNodeName().equals("revision")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    general.setRevision(content);
                    continue;
                }

                if (childNode.getNodeName().equals("date")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    general.setDate(content);
                    continue;
                }

                if (childNode.getNodeName().equals("author")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    general.setAuthor(content);
                    continue;
                }

                if (childNode.getNodeName().equals("ffv")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    general.setFfv(content);
                    continue;
                }

                if (childNode.getNodeName().equals("fiv")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    general.setFiv(content);
                    continue;
                }

                if (childNode.getNodeName().equals("protocols")) {
                    general.setProtocols(getProtocols(childNode));
                }
            }
        }
        return general;
    }

    private List<Protocol> getProtocols(Node node) {
        List<Protocol> protocols = new ArrayList<Protocol>();
        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                if (childNode.getNodeName().trim().equals("protocol")) {
                    Protocol protocol = getProtocol(childNode);
                    protocols.add(protocol);
                }
            }
        }

        return protocols;
    }

    private Protocol getProtocol(Node node) {
        Protocol protocol = new Protocol();

        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode instanceof Element) {
                if (childNode.getNodeName().trim().equals(NAME)) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    protocol.setName(content);
                    continue;
                }

                if (childNode.getNodeName().trim().equals("specification")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    protocol.setSpecification(content);
                    continue;
                }

                if (childNode.getNodeName().trim().equals("version")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    protocol.setVersion(content);
                }
            }
        }

        return protocol;
    }

    private List<PredefinedEventGroup> getEventGroups(Node node) {
        List<PredefinedEventGroup> eventGroups = new ArrayList<PredefinedEventGroup>();

        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode instanceof Element) {
                if (childNode.getNodeName().trim().equals("predefinedeventgroup")) {
                    PredefinedEventGroup eventGroup = getEventGroup(childNode);
                    eventGroups.add(eventGroup);
                }
            }
        }

        return eventGroups;
    }

    private PredefinedEventGroup getEventGroup(Node node) {
        PredefinedEventGroup eventGroup = new PredefinedEventGroup();
        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                if (childNode.getNodeName().equals(NAME)) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    eventGroup.setName(content);
                    continue;
                }
                if (childNode.getNodeName().equals("events")) {
                    eventGroup.setEvents(getEventGroupEvents(childNode));                    
                }
            }
        }
        return eventGroup;
    }

    private List<String> getEventGroupEvents(Node node) {
        List<String> eventElements = new ArrayList<String>();

        NodeList childNodes = node.getChildNodes();

        for (int k = 0; k < childNodes.getLength(); k++) {
            Node childNode = childNodes.item(k);

            if (childNode instanceof Element) {
                String content = childNode.getLastChild().getTextContent().trim();
                eventElements.add(content);
            }
        }

        return eventElements;
    }

    private List<ParameterType> getParameterTypes(Node node) {
        List<ParameterType> parameterTypes = new ArrayList<ParameterType>();

        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode instanceof Element) {
                if (childNode.getNodeName().trim().equals("parametertype")) {
                    ParameterType parameterType = getParameterType(childNode);
                    parameterTypes.add(parameterType);
                }
            }
        }

        return parameterTypes;
    }

    private ParameterType getParameterType(Node node) {
        ParameterType parameterType = new ParameterType();
        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                if (childNode.getNodeName().equals(NAME)) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    parameterType.setName(content);
                }
                else if (childNode.getNodeName().equals(TYPE)) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    parameterType.setType(content);
                }
                else if (childNode.getNodeName().equals("numberofbytes")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    parameterType.setNoOfBits(Integer.parseInt(content) * 8);                    
                }
                else if (childNode.getNodeName().equals("numberofbits")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    parameterType.setNoOfBits(Integer.parseInt(content));                    
                }
                else if (childNode.getNodeName().equals("range")) {
                    setParameterRange(parameterType, childNode);            
                }                
            }
        }

        parameterType.setType(convertParameterType(parameterType.getType(), parameterType.getLow(), parameterType.getHigh()));

        return parameterType;
    }
    
    
    private void setParameterRange(ParameterType parameterType, Node node){
    	NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                if (childNode.getNodeName().equals("low")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    parameterType.setLow(Long.parseLong(content));
                }
                
                if (childNode.getNodeName().equals("high")) {
                    String content = childNode.getLastChild().getTextContent().trim();
                    parameterType.setHigh(Long.parseLong(content));
                }
            }            
        }
    }

    private String updateType(String type, long rangeLow, long rangeHigh) {    	
        if (type.equals(ENUM) || type.equals(UINT)) {
            if (rangeLow >= MIN_INTEGER && rangeHigh <= MAX_INTEGER) {
                type = INTEGER;
            } else {
                type = LONG_DATA_TYPE;
            }
        }

        return type;
    }

    private String convertParameterType(String type, long rangeLow, long rangeHigh) { 
        String updatedType = "";
        updatedType = updateType(type, rangeLow, rangeHigh);

        String avroBoolean = "boolean";
        String avroDouble = "double";
        String avroFloat = "float";
        String avroInt = "int";
        String avroLong = "long";
        String avroBytes = "bytes";
        String avroString = "string";
        
        
        final Map<String, String> types = new HashMap<String, String>();
        types.put(LONG_DATA_TYPE, avroLong);        
		types.put("BOOLEAN", avroBoolean);
        types.put("STRING", avroString);
		types.put("DOUBLE", avroDouble);        
		types.put("FLOAT", avroFloat);        
		types.put(INTEGER, avroInt);
        types.put(LONG_DATA_TYPE, avroLong);
        types.put("BYTE", avroInt);
		types.put("BYTE_ARRAY", avroBytes);
        types.put("IBCD_BYTE", avroBytes);
        types.put("IBCD_INTEGER", avroInt);
        types.put("IBCD_LONG", avroLong);
        types.put(UINT, avroInt);
        types.put("UINT_BYTE", avroInt);
        types.put("UINT_INTEGER", avroInt);
        types.put("UINT_LONG", avroLong);
        types.put("LONG_BYTE", avroInt);
        types.put("LONG_INTEGER", avroInt);
        types.put("LONG_LONG", avroLong);
        types.put("TBCD", avroString);
        types.put("IPADDRESS", avroString);
        types.put("IPADDRESSV6", avroString);
        types.put("DNSNAME", avroString);
        types.put("HEXSTRING", avroString);
        types.put("CCSTRING", avroString);
        types.put("BINARY", avroBytes);
        types.put("FROREF", avroBytes);

        String avroType = "";

        for (String key : types.keySet()) {
            if (updatedType.equals(key)) {
                avroType = types.get(key);
            }
        }

        return avroType;
    }

    private List<Event> getEvents(Node node) {

        List<Event> events = new ArrayList<Event>();

        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode instanceof Element) {
                if (childNode.getNodeName().trim().equals("event")) {
                    Event event = getEvent(childNode);                    
                    events.add(event);
                }
            }
        }

        return events;
    }

    private Event getEvent(Node node) {
        Event event = new Event();

        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                String content = childNode.getLastChild().getTextContent().trim();

                if (childNode.getNodeName().equals(NAME)) {
                    event.setName(content);
                    continue;
                }

                if (childNode.getNodeName().equals("elements")) {
                    event.setElements(getEventElements(childNode));
                }
            }
        }
        return event;
    }

    private List<Param> getEventElements(Node node) {
        List<Param> eventElements = new ArrayList<Param>();
        Map<Param, Integer> paramCounts = new HashMap<Param, Integer>();

        NodeList childNodes = node.getChildNodes();

        for (int k = 0; k < childNodes.getLength(); k++) {
            Node childNode = childNodes.item(k);

            if (childNode instanceof Element) {
                if (childNode.getNodeName().equals("param")) {
                    Param param = getParam(childNode);
                    eventElements.add(param);
                    countParameters(param, paramCounts);
                    continue;
                }

                // not used in Cell Trace
                if (childNode.getNodeName().equals("struct")) {
                    Node structNode = childNode.getAttributes().getNamedItem(TYPE);
                    List<Param> params = getParamsFromStruct(structNode);
                    for (Param param : params) {
                        eventElements.add(new Param(param.getType(), structNode.getNodeValue() + "_" + param.getName()));
                    }
                }
            }
        }
        
        addInstanceCounts(eventElements, paramCounts);
        
        return eventElements;
    }

    /**
     * @param eventElements
     * @param paramCounts
     */
    private void addInstanceCounts(List<Param> eventElements, Map<Param, Integer> paramCounts) {
        for (Param tempParam : paramCounts.keySet()){
            int instances = paramCounts.get(tempParam);
            int count = 1;
            if (instances > 1){
                for (int i = 0; i < eventElements.size(); i++){
                    if (count <= instances){
                        Param param = eventElements.get(i);
                        if (param.getType().equals(tempParam.getType())){
                            param.setName(param.getName() + "_" + count);                            
                            eventElements.remove(i);
                            eventElements.add(i, param);
                            count ++;
                        }
                    }
                }
            }            
        }
    }
    

     private void countParameters(Param param, Map<Param, Integer> paramCounts) {
        if (paramCounts.get(param) == null) {
            paramCounts.put(param, 1);
        } else {
            int count = paramCounts.get(param);
            paramCounts.put(param, count + 1);
        }
    }

    private Param getParam(Node childNode) {
        String name = childNode.getLastChild().getTextContent().trim();
        Node structNode = childNode.getAttributes().getNamedItem(TYPE);

        String type;
        if (structNode == null) {
            type = name;
        } else {
            type = structNode.getNodeValue();
        }

        name = removeEventPreamble(name); 
        return new Param(type, name);
    }

    private String removeEventPreamble(String name) {
        name = name.replaceFirst("EVENT_PARAM_", "");
        if (Character.isDigit(name.charAt(0))) {
            name = "P_" + name;
        }
        return name;
    }

    private List<Param> getParamsFromStruct(Node structNode) {
        String name = structNode.getNodeValue();

        for (StructureType structureType : structuretypes) {
            if (name.equals(structureType.getName())) {
                return structureType.getElements();
            }
        }

        return null;
    }

    private List<StructureType> getStructureTypes(Node node) {
        List<StructureType> structureTypes = new ArrayList<StructureType>();

        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode instanceof Element) {
                if (childNode.getNodeName().trim().equals("structuretype")) {
                    StructureType structureType = getStructureType(childNode);
                    structureTypes.add(structureType);
                }
            }
        }

        return structureTypes;
    }

    private StructureType getStructureType(Node node) {
        StructureType structureType = new StructureType();

        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                String content = childNode.getLastChild().getTextContent().trim();

                if (childNode.getNodeName().equals(NAME)) {
                    structureType.setName(content);
                    continue;
                }

                if (childNode.getNodeName().equals("elements")) {
                    structureType.setElements(getEventElements(childNode));                    
                }
            }
        }
        return structureType;
    }
}
