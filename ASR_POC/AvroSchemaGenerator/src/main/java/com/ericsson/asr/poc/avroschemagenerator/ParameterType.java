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

public class ParameterType {
    private String name;
    private String type;
    private int noOfBits;
    private long low;
    private long high;

    public long getLow() {
		return low;
	}

	public void setLow(long low) {
		this.low = low;
	}

	public long getHigh() {
		return high;
	}

	public void setHigh(long high) {
		this.high = high;
	}

	public int getNoOfBits() {
        return noOfBits;
    }

    public void setNoOfBits(int noOfBits) {
        this.noOfBits = noOfBits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (high ^ (high >>> 32));
		result = prime * result + (int) (low ^ (low >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + noOfBits;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParameterType other = (ParameterType) obj;
		if (high != other.high)
			return false;
		if (low != other.low)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (noOfBits != other.noOfBits)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
