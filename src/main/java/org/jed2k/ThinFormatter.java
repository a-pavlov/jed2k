package org.jed2k;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by inkpot on 17.08.2016.
 */
public class ThinFormatter  extends Formatter {
    @Override
    public String format(LogRecord record) {
        return record.getLevel() + " " + record.getMessage() + "\n";
    }
}
