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

import java.net.InetSocketAddress;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.netty.protocol.DefaultStreamOutProtocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class NettyTestClient {
    private static final String NUMBER_OF_CONNECT_ATTEMPTS_PROPERTIES_KEY = "NUMBER_OF_CONNECTION_ATTEMPTS";
    private static final String MAX_FRAME_LENGTH_PROPERTIES_KEY = "MAX_FRAME_LENGTH";
    private static final String INITIAL_BYTES_TO_STRIP_PROPERTIES_KEY = "INITIAL_BYTES_TO_STRIP";
    private static final String LENGTH_ADJUSTMENT_PROPERTIES_KEY = "LENGTH_ADJUSTMENT";
    private static final String LENGTH_FIELD_LENGTH_PROPERTIES_KEY = "LENGTH_FIELD_LENGTH";
    private static final String LENGTH_FIELD_OFFSET_PROPERTIES_KEY = "LENGTH_FIELD_OFFSET";
    private static final String LOCAL_HOST_PROPERTIES_KEY = "LOCAL_HOST";
    private static final String DEFAULT_PORT_PROPERTIES_KEY = "DEFAULT_PORT";
    private final static Logger LOGGER = LoggerFactory.getLogger(NettyTestClient.class);

    private final int numberOfConnectRetrys;
    private final int port;
    private final String ipAddress;
    private final int lengthFieldOffset;
    private final int lengthFieldLength;
    private final int lengthAdjustment;
    private final int initialBytesToStrip;
    private final int maxFrameLength;
    private static String propertiesFilePath;

    public NettyTestClient() {
        final Properties prop = ConfigUtils.getProperties(propertiesFilePath);
        port = Integer.parseInt(prop.getProperty(DEFAULT_PORT_PROPERTIES_KEY));
        ipAddress = prop.getProperty(LOCAL_HOST_PROPERTIES_KEY);
        numberOfConnectRetrys = Integer.parseInt(prop.getProperty(NUMBER_OF_CONNECT_ATTEMPTS_PROPERTIES_KEY));
        lengthFieldOffset = Integer.parseInt(prop.getProperty(LENGTH_FIELD_OFFSET_PROPERTIES_KEY));
        lengthFieldLength = Integer.parseInt(prop.getProperty(LENGTH_FIELD_LENGTH_PROPERTIES_KEY));
        lengthAdjustment = Integer.parseInt(prop.getProperty(LENGTH_ADJUSTMENT_PROPERTIES_KEY));
        initialBytesToStrip = Integer.parseInt(prop.getProperty(INITIAL_BYTES_TO_STRIP_PROPERTIES_KEY));
        maxFrameLength = Integer.parseInt(prop.getProperty(MAX_FRAME_LENGTH_PROPERTIES_KEY));
        logValues();
    }

    private void logValues() {
        LOGGER.debug("PORT: " + port + " IP: " + ipAddress + "Number Of Connection Attempts: " + numberOfConnectRetrys + " Offset: "
                + lengthFieldOffset + " Field Length: " + lengthFieldLength + "Length Adjustment: " + lengthAdjustment + " Bytes to strip: "
                + initialBytesToStrip + " Frame Length: " + maxFrameLength);
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("Starting Test Client");
        propertiesFilePath = args[0];
        new NettyTestClient().run();
    }

    public void run() throws Exception {
        LOGGER.info("Running Test Client");
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(new OioEventLoopGroup());
            bootstrap.channel(OioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(final SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment,
                            initialBytesToStrip));
                    ch.pipeline().addLast(new MultiplexClientHandshakeHandler(new DefaultStreamOutProtocol()));
                }
            }).option(ChannelOption.SO_BACKLOG, 128);
            connect(bootstrap);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private void connect(final Bootstrap bootstrap) throws InterruptedException {
        for (int numberOfConnectionAttempts = 0; numberOfConnectionAttempts < numberOfConnectRetrys; numberOfConnectionAttempts++) {
            final ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(ipAddress, port));
            if (channelFuture.isSuccess()) {
                channelFuture.sync();
                channelFuture.channel().closeFuture().sync();
            } else if (numberOfConnectionAttempts == (numberOfConnectRetrys - 1)) {
                LOGGER.error("Connection Failed on Port: " + port + ", IpAddress: " + ipAddress);
            } else {
                LOGGER.warn("Connection not available, retrying...");
            }
        }
    }

    public static String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public static void setPropertiesFilePath(String filePath) {
        NettyTestClient.propertiesFilePath = filePath;
    }
}