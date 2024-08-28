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

import java.io.File;
import java.net.InetSocketAddress;
import java.sql.Timestamp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SendMessage {
	private int port;
	private String host;
	private String cacheEntry;
	SchemaCacheMap schemaCacheMap;

	public SendMessage(int port, String host, SchemaCacheMap schemaCacheMap,
			String cacheEntry) {
		this.port = port;
		this.schemaCacheMap = schemaCacheMap;
		this.host = host;
		this.cacheEntry = cacheEntry;
	}

	public void run() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootStrap = new Bootstrap();
		bootStrap.group(group);
		bootStrap.channel(NioSocketChannel.class);
		bootStrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 512 * 128);
		bootStrap.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 512 * 64);
		bootStrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline channelPipeLine = ch.pipeline();
			}
		});
		ChannelFuture chFuture = bootStrap.connect(new InetSocketAddress(host, port));
		chFuture.awaitUninterruptibly();

		final SendMessageHandler sendMessageHandler = new SendMessageHandler(schemaCacheMap, cacheEntry);
		int i=0;
		while(true){
			sendMessageHandler.writeToChannel(chFuture.channel());
			i++;
			if(i % 100000 == 0){
				System.out.println("Time: "+new Timestamp(System.currentTimeMillis()).toString()+", Number of message sent: "+i);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		int port;
		String host;
		String directory = "";
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
			host = args[1];
			directory = args[2];
		} else {
			port = 101;
			host = "localhost";
		}
		String cacheEntry = loadAvroSchema(directory);
		new SendMessage(port, host, SchemaCacheMap.getInstance(), cacheEntry)
				.run();
	}

	public static String loadAvroSchema(String directory) {
		File dir1 = new File(directory);
		GenericRecordGenerator genericRecordGenerator = new GenericRecordGenerator();

		for (final String dir : dir1.list()) {
			final File fileDir = new File(directory + File.separator + dir);

			if (fileDir.isDirectory()) {
				for (final File f : fileDir.listFiles()) {
					if (f.toString().contains("INTERNAL_EVENT_NEIGHBREL_ADD")) {
						final String schemaName = f.getAbsolutePath();
						final String[] fileDirSplit = f.toString().split(
								"\\" + File.separator);
						final String eventName = (fileDirSplit[fileDirSplit.length - 1])
								.replace(".avsc", "");
						final String cacheEntry = fileDirSplit[fileDirSplit.length - 2]
								+ "." + eventName;
						genericRecordGenerator.loadAvroSchema(schemaName,
								cacheEntry);
						return cacheEntry;
					}
				}
			}
		}
		return null;

	}
}