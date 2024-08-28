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
package com.ericsson.customnbiclient.integration.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Test;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

public class CodedStreamTest {

    final static String TEMPORARY_FILE_NAME = "file1.txt";
    final static long ORIGINAL_LONG_VALUE = 5987654321l;
    final static int ORIGINAL_INT_VALUE = 59876;

    @Test
    public void conversionTestInt32UsingByteArray() throws IOException {

        byte[] convertedBytes = new byte[4];
        CodedOutputStream outputStream = CodedOutputStream.newInstance(convertedBytes);
        outputStream.writeInt32NoTag(ORIGINAL_INT_VALUE);
        outputStream.flush();

        CodedInputStream inputStream = CodedInputStream.newInstance(convertedBytes);
        long readInt32 = inputStream.readRawVarint32();
        assertEquals(readInt32, ORIGINAL_INT_VALUE);
    }

    @Test
    public void conversionTestInt32UsingFile() throws IOException {

        final OutputStream outputStream = new FileOutputStream(TEMPORARY_FILE_NAME);
        final CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
        codedOutputStream.writeInt32NoTag(ORIGINAL_INT_VALUE);
        codedOutputStream.flush();
        outputStream.close();

        final InputStream inputStream = new FileInputStream(TEMPORARY_FILE_NAME);
        final CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        long readInt32 = codedInputStream.readRawVarint32();
        inputStream.close();

        assertEquals(readInt32, ORIGINAL_INT_VALUE);
    }
    
    @Test
    public void conversionTestInt64UsingByteArray() throws IOException {

        byte[] convertedBytes = new byte[8];
        CodedOutputStream outputStream = CodedOutputStream.newInstance(convertedBytes);
        outputStream.writeInt64NoTag(ORIGINAL_LONG_VALUE);
        outputStream.flush();

        CodedInputStream inputStream = CodedInputStream.newInstance(convertedBytes);
        long readInt64 = inputStream.readRawVarint64();
        assertEquals(readInt64, ORIGINAL_LONG_VALUE);
    }

    @Test
    public void conversionTestInt64UsingFile() throws IOException {

        final OutputStream outputStream = new FileOutputStream(TEMPORARY_FILE_NAME);
        final CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
        codedOutputStream.writeInt64NoTag(ORIGINAL_LONG_VALUE);
        codedOutputStream.flush();
        outputStream.close();

        final InputStream inputStream = new FileInputStream(TEMPORARY_FILE_NAME);
        final CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        long readInt64 = codedInputStream.readRawVarint64();
        inputStream.close();

        assertEquals(readInt64, ORIGINAL_LONG_VALUE);
    }
    
    @Test
    public void conversionTestByteArray() throws IOException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(ORIGINAL_LONG_VALUE);

        byte[] convertedBytes = new byte[8];
        final CodedOutputStream outputStream = CodedOutputStream.newInstance(convertedBytes);
        outputStream.writeRawBytes(byteBuffer.array());
        outputStream.flush();

        final CodedInputStream inputStream = CodedInputStream.newInstance(convertedBytes);
        final byte[] readRawBytes = inputStream.readRawBytes(convertedBytes.length);
        long readInt64 = ByteBuffer.wrap(readRawBytes).getLong();

        assertEquals(readInt64, ORIGINAL_LONG_VALUE);
    }

    @After
    public void cleanUp() {
        final File temporaryFile = new File(TEMPORARY_FILE_NAME);
        if(temporaryFile.exists()) {
            temporaryFile.delete();
        }
    }

}
