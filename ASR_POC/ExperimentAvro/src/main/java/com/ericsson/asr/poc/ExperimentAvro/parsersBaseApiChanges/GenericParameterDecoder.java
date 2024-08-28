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
package com.ericsson.asr.poc.ExperimentAvro.parsersBaseApiChanges;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.parsersapi.base.meta.schema.EventParameter;
import com.ericsson.oss.mediation.parsersapi.base.util.DataConverterHelper;


public abstract class GenericParameterDecoder {
    
   
    protected DataConverterHelper dataConverterHelper;

    /**
     * Constructor
     * @param EventParameter @param 
     */
    public GenericParameterDecoder() {
        this.dataConverterHelper = new DataConverterHelper();
    }
    
    protected abstract Object decode(final byte[] data, final int offset, final EventParameter eventParameter);
    
    public Object getDecodeValue(final byte[] data, final int offset, final EventParameter eventParameter) {
        int offsetToUse = offset;
        if (eventParameter.isUseValid() || eventParameter.isOptional()) {
            if (isMarkedInvalid(data, offset)) {
                return null;
            }
            if (eventParameter.isValidLTEembeddedbitFlag()) {
                isValidLTEembeedingbitflag(data, offset);
            } else {
                offsetToUse = offset + 1;
            }
        }
     
        return decode(data, offsetToUse, eventParameter);
    }
    
    public byte[] getExperimentalDecodeValue(final byte[] data, final int offset, final EventParameter eventParameter) {
        int offsetToUse = offset;
        int paramNumberOfBytes = eventParameter.getNumberOfBytes();
        if (eventParameter.isUseValid() || eventParameter.isOptional()) {
            if (isMarkedInvalid(data, offset)) {
                return null;
            }
            if (eventParameter.isValidLTEembeddedbitFlag()) {
                isValidLTEembeedingbitflag(data, offset);
            } else {
                offsetToUse = offset + 1;
            }
        }

        if(paramNumberOfBytes == -1){
        	return Arrays.copyOfRange(data, offsetToUse, data.length - 1);
        }
        
        return Arrays.copyOfRange(data,offsetToUse, paramNumberOfBytes + offsetToUse);
    }

	/**
	 * @param data
	 * @param offset
	 */
	public void isValidLTEembeedingbitflag(final byte[] data, final int offset) {
		// get rest of 7 bit from bytes and replace original byte i.e. result byte has no validity bit 
		data[offset] = dataConverterHelper.getParamByte(data[offset], 1, 7);
	}
    
    public boolean isMarkedInvalid(final byte[] data, final int offset){
    	boolean isMarkedInvalid = false;
    	
    	if (dataConverterHelper.isMarkedInvalid(data, offset)) {
            return isMarkedInvalid = true;
        }
    	return isMarkedInvalid;
    	
    }

    public int adjustOffset(final int offset, final EventParameter eventParameter) {
        int adjustedOffset = offset;
        if ((eventParameter.isUseValid() || eventParameter.isOptional()) && !eventParameter.isValidLTEembeddedbitFlag()) {
            adjustedOffset++;
        }
        adjustedOffset += eventParameter.getNumberOfBytes();
        return adjustedOffset;

    }
}
