package org.endeavourhealth.msgsender.sender;

import com.google.common.base.Strings;
import org.endeavourhealth.msgsender.dal.HL7MessageInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampHelper {
    private static final Logger LOG = LoggerFactory.getLogger(TimestampHelper.class);

    /**
     * extracts the timestamp from the HL7v2 which is needed by the Messaging API
     */
    public static Date findMessageTimestamp(HL7MessageInstance message) {
        String rawInbound = message.getHl7message();

        //if we ever have an empty message, that's OK and we just don't provide a timestamp
        if (Strings.isNullOrEmpty(rawInbound)) {
            return null;
        }

        //the raw HL7v2 will contain the issuing timestamp in the first line, the message header
        int ix = rawInbound.indexOf("\n");
        if (ix == -1) {
            LOG.error("Failed to find newline in message " + message.getId());
            return null;
        }

        String firstLine = rawInbound.substring(0, ix);
        String[] toks = firstLine.split("\\|");
        String firstTok = toks[0];
        if (!firstTok.equalsIgnoreCase("MSH")) {
            LOG.error("MSH isn't first segment in message " + message.getId());
            return null;
        }

        String timestampTok = toks[6];

        if (timestampTok.length() < 14) {
            LOG.error("Element 6 is too short for timestamp [" + timestampTok + "] in message " + message.getId());
            return null;
        }

        //if timestamp has extra timezone info, just strip off
        if (timestampTok.length() > 14) {
            timestampTok = timestampTok.substring(0, 14);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date d = dateFormat.parse(timestampTok);
            LOG.trace("Calculated message timestamp " + d + " for message " + message.getId());
            return d;

        } catch (ParseException pe) {
            LOG.error("Failed to parse timestamp from [" + timestampTok + "] in message " + message.getId());
            return null;
        }
    }
}
