package test.io.netty;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;

public class ByteBufUtils {
    private static Logger logger = LoggerFactory.getLogger(ByteBufUtil.class);
    public static final ByteBufAllocator DEFAULT;

    static {

        String allocType = SystemPropertyUtil.get("io.netty.allocator.type", PlatformDependent.isAndroid() ? "unpooled" : "pooled");
        allocType = allocType.toLowerCase(Locale.US).trim();

        ByteBufAllocator alloc;
        if ("unpooled".equals(allocType)) {
            alloc = UnpooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: {}", allocType);
        } else if ("pooled".equals(allocType)) {
            alloc = PooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: {}", allocType);
        } else {
            alloc = PooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: pooled (unknown: {})", allocType);
        }
        DEFAULT = alloc;
    }

    private ByteBufUtils() {

    }

    /**
     * @param size
     *        of array to allocate
     * @return
     */
    public static ByteBuf allocate(final int size) {
        return DEFAULT.buffer(size);
    }

    public static ByteBuf allocate() {
        return DEFAULT.buffer();
    }

    /**
     * some buffer implementations are not backed by byte[] thus we have to check if there is buffer {@code ByteBuf#hasArray()} before we can call
     * {@code ByteBuf#array()}.
     *
     * @param buffer
     *        to convert to byte[]
     * @return
     */
    public static byte[] toArray(final ByteBuf buffer) {
        if (buffer.hasArray()) {
            return buffer.array();
        } else {
            final byte[] result = new byte[buffer.readableBytes()];
            buffer.readBytes(result);
            return result;
        }
    }

}
