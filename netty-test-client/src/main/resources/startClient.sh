#!/bin/bash
echo "Executing netty test client..."
echo "------------------------------------------------------------------------"
JAVA_CMD="java -cp .:netty-test-client-1.0.0-SNAPSHOT.jar test.io.netty.NettyTestClient connectionConfig.properties"
echo $JAVA_CMD
echo "------------------------------------------------------------------------"
exec "$JAVA_CMD"