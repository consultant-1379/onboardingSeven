package test.io.netty;

import java.io.*;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);

    public static Properties getProperties(String propertyFilePath) {
        final Properties prop = new Properties();
        try (final InputStream inputStream = new FileInputStream(propertyFilePath)) {
            prop.load(inputStream);
        } catch (final FileNotFoundException e) {
            LOGGER.error("Exception occured while creating input stream: " + e.getMessage());
        } catch (final IOException e) {
            LOGGER.error("Exception occured while trying to load properties file: " + e.getMessage());
        }
        return prop;
    }

}
