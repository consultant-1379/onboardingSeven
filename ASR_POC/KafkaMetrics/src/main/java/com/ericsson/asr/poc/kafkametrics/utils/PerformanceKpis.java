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
package com.ericsson.asr.poc.kafkametrics.utils;

public class PerformanceKpis {

    private double numberOfKilobytes, totalSecs;
    private long numberOfRecords;
    private final int numberOfInstances;

    public PerformanceKpis(final int numberOfInstances) {
        this.numberOfKilobytes = 0.0;
        this.totalSecs = 0.0;
        this.numberOfRecords = 0;
        this.numberOfInstances = numberOfInstances;
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public double getNumberOfKilobytes() {
        return numberOfKilobytes;
    }

    public long getNumberOfRecordsSent() {
        return numberOfRecords;
    }

    public double getTotalSecs() {
        return totalSecs;
    }

    public double calculateAvgKilobyteThroughput() {
        return numberOfKilobytes / totalSecs;
    }

    public double calculateAvgRecordSize() {
        return numberOfKilobytes / numberOfRecords;
    }

    public double calculateAvgRecordThroughput() {
        return numberOfRecords / totalSecs;
    }

    public void updateKpis(final double aNumberOfKilobytes, final double aTotalSecs, final long aNumberOfRecords) {
        numberOfKilobytes += aNumberOfKilobytes;
        totalSecs += aTotalSecs;
        numberOfRecords += aNumberOfRecords;
    }

}
