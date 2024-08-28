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

import java.util.List;

public class General {

    private String docNumber;
    private String revision;
    private String date;
    private String author;
    private String ffv;
    private String fiv;
    private List<Protocol> protocols;

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFfv() {
        return ffv;
    }

    public void setFfv(String ffv) {
        this.ffv = ffv;
    }

    public String getFiv() {
        return fiv;
    }

    public void setFiv(String fiv) {
        this.fiv = fiv;
    }

    public List<Protocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<Protocol> protocols) {
        this.protocols = protocols;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result
                + ((docNumber == null) ? 0 : docNumber.hashCode());
        result = prime * result + ((ffv == null) ? 0 : ffv.hashCode());
        result = prime * result
                + ((protocols == null) ? 0 : protocols.hashCode());
        result = prime * result
                + ((revision == null) ? 0 : revision.hashCode());
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
        General other = (General) obj;
        if (author == null) {
            if (other.author != null)
                return false;
        } else if (!author.equals(other.author))
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (docNumber == null) {
            if (other.docNumber != null)
                return false;
        } else if (!docNumber.equals(other.docNumber))
            return false;
        if (ffv == null) {
            if (other.ffv != null)
                return false;
        } else if (!ffv.equals(other.ffv))
            return false;
        if (protocols == null) {
            if (other.protocols != null)
                return false;
        } else if (!protocols.equals(other.protocols))
            return false;
        if (revision == null) {
            if (other.revision != null)
                return false;
        } else if (!revision.equals(other.revision))
            return false;
        return true;
    }

}
