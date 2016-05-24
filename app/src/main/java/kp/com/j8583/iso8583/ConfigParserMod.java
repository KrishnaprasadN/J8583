package kp.com.j8583.iso8583;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Krishnaprasad.n on 4/18/2016.
 */
public class ConfigParserMod extends ConfigParser {

    private final static Logger log = LoggerFactory.getLogger(ConfigParserMod.class);

    public static MessageFactory<IsoMessage> createFromInputStream(
            InputStream ins) throws IOException {
        MessageFactory<IsoMessage> mfact = new MessageFactory<>();

        if (ins != null) {
            parse(mfact, ins);
        } else {
            log.error("ISO8583 Input stream is null");
        }

        return mfact;
    }
}
