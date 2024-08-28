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
package com.ericsson.customnbiclient.enpointconfiguration;

public enum EndpointPort {

    S3(10101),
    PROTOBUF(10102),
    STRING(10103);


    private int port;

    private EndpointPort(final int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

}
