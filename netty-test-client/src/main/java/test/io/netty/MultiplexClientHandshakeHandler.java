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
package test.io.netty;

import java.sql.Timestamp;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.netty.protocol.StreamOutProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handler for decoding and interacting with the stream terminator as a client. Provides {@code com.yammer.metrics.core.Meter} for protocol events and
 * errors.
 *
 */
public class MultiplexClientHandshakeHandler extends ChannelInboundHandlerAdapter {
    private static final String FILTER_SET_IN_USE_PROPERTIES_KEY = "FILTER_SET_IN_USE";
    private static final String CLIENT_ID_PROPERTIES_KEY = "CLIENT_ID";
    private static final String CLIENT_GROUP_ID_PROPERTIES_KEY = "CLIENT_GROUP_ID";
    private static final String NUMBER_OF_EVENTS_PER_LOG_OUTPUT_PROPERTIES_KEY = "NUMBER_OF_EVENTS_PER_LOG_OUTPUT";
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiplexClientHandshakeHandler.class);
    private static final byte REGULAR_EVENT = 0x00;
    private static final byte HEADER_TYPE_PROTOCOL_INITIALIZATION = 0x01;
    private static final byte HEADER_TYPE_CONNECTION_EVENT = 0x02;
    private static final byte HEADER_TYPE_DISCONNECTION_EVENT = 0x03;
    private static final byte HEADER_TYPE_DROPPED_EVENTS = 0x04;
    private final int clientGroupId;
    private final short clientId;
    private final short filterSetId;
    private final int numberOfEventsPerLogOutput;
    private final StreamOutProtocol protocol;

    private long totalNumberOfEvents;
    private long regEvents;
    private long initEvents;
    private long conEvents;
    private long disconEvents;
    private long dropEvents;
    private long unknownEvents;

    public MultiplexClientHandshakeHandler(final StreamOutProtocol protocol) {
        final Properties prop = ConfigUtils.getProperties(NettyTestClient.getPropertiesFilePath());
        this.protocol = protocol;
        this.numberOfEventsPerLogOutput = Integer.parseInt(prop.getProperty(NUMBER_OF_EVENTS_PER_LOG_OUTPUT_PROPERTIES_KEY));
        this.clientGroupId = Integer.parseInt(prop.getProperty(CLIENT_GROUP_ID_PROPERTIES_KEY));
        this.clientId = Short.parseShort(prop.getProperty(CLIENT_ID_PROPERTIES_KEY));
        this.filterSetId = Short.parseShort(prop.getProperty(FILTER_SET_IN_USE_PROPERTIES_KEY));

    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Client connected! Sending protocol initialization.");
        ctx.channel().writeAndFlush(this.protocol.createInitServerProtocolEvent(ctx.alloc(), this.clientGroupId, this.clientId, this.filterSetId));
        try {
            super.channelActive(ctx);
        } catch (final Exception e1) {
            LOGGER.error("Channel not connected");
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final ByteBuf message = (ByteBuf) msg;
        final short headerType = message.getUnsignedByte(0);
        logNumberOfEvents(headerType);
        super.channelRead(ctx, msg);
    }

    private void logNumberOfEvents(final short headerType) {
        final String currentTime = new Timestamp(System.currentTimeMillis()).toString();
        if (++totalNumberOfEvents % numberOfEventsPerLogOutput == 0) {
            LOGGER.info("Time: " + currentTime + ", Number of TOTAL messages received: " + totalNumberOfEvents);
        }
        if (headerType == REGULAR_EVENT && ++regEvents % numberOfEventsPerLogOutput == 0) {
            LOGGER.debug("Time: " + currentTime + ", Number of REGULAR EVENT messages received: " + regEvents);
        } else if (headerType == HEADER_TYPE_PROTOCOL_INITIALIZATION && ++initEvents % numberOfEventsPerLogOutput == 0) {
            LOGGER.debug("Time: " + currentTime + ", Number of INIT messages received: " + initEvents);
        } else if (headerType == HEADER_TYPE_CONNECTION_EVENT && ++conEvents % numberOfEventsPerLogOutput == 0) {
            LOGGER.debug("Time: " + currentTime + ", Number of CONNECTION messages received: " + conEvents);
        } else if (headerType == HEADER_TYPE_DISCONNECTION_EVENT && ++disconEvents % numberOfEventsPerLogOutput == 0) {
            LOGGER.debug("Time: " + currentTime + ", Number of DISCONNECTION messages received: " + disconEvents);
        } else if (headerType == HEADER_TYPE_DROPPED_EVENTS && ++dropEvents % numberOfEventsPerLogOutput == 0) {
            LOGGER.debug("Time: " + currentTime + ", Number of DROPPED messages received: " + dropEvents);
        } else {
            unknownEvents++;
            LOGGER.debug("Time: " + currentTime + ", Number of UNKNOWN messages received: " + unknownEvents);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    public long getTotalNumberOfEvents() {
        return totalNumberOfEvents;
    }

}
