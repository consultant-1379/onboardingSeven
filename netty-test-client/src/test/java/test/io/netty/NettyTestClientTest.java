package test.io.netty;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.ericsson.oss.mediation.netty.protocol.DefaultStreamOutProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class NettyTestClientTest {

    private static final String CONNECTION_CONFIG_PROPERTIES_FILE = "src/main/resources/connectionConfig.properties";
    private static MultiplexClientHandshakeHandler clientHandshakeHandler;
    private static ChannelHandlerContext ctx;

    @BeforeClass
    public static void setup() {
        NettyTestClient.setPropertiesFilePath(CONNECTION_CONFIG_PROPERTIES_FILE);
        ctx = getMockChannelHandlerContext();
        clientHandshakeHandler = new MultiplexClientHandshakeHandler(new DefaultStreamOutProtocol());
    }

    @Test
    public void testChannelReadForMultiplexClientHandshakeHandlerAtInfoLogLevel() throws Exception {
        final byte[] sampleByteArray1 = new byte[] { 2 };
        final byte[] sampleByteArray2 = new byte[] { 3 };
        final ByteBuf sampleByteBuf1 = Unpooled.copiedBuffer(sampleByteArray1);
        final ByteBuf sampleByteBuf2 = Unpooled.copiedBuffer(sampleByteArray2);

        clientHandshakeHandler.channelRead(ctx, sampleByteBuf1);
        clientHandshakeHandler.channelRead(ctx, sampleByteBuf2);

        assertEquals("The number of events read did not match the expected amout of reads", 2, clientHandshakeHandler.getTotalNumberOfEvents());
    }

    private static ChannelHandlerContext getMockChannelHandlerContext() {
        final ChannelHandlerContext mockContext = Mockito.mock(ChannelHandlerContext.class);
        final Channel mockChannel = Mockito.mock(Channel.class);
        Mockito.when(mockContext.channel()).thenReturn(mockChannel);
        return mockContext;
    }

}
