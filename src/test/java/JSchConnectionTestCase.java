import com.pressassociation.LogListener;
import com.pressassociation.Scp;
import org.junit.Test;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JSchConnectionTestCase {

    private static Logger logger = Logger.getLogger(JSchConnectionTestCase.class.getCanonicalName());

    @Test
    public void testSimpleConnection() throws Exception {
        final File f = File.createTempFile("prefix-", ".xml");

        Scp session = new Scp("merwilwen.local");
        session.setUsername("bobb");
        session.setKeyfile("/Users/bobb/.ssh/id_rsa");
        session.setTrust(true);
        session.setVerbose(true);
        session.setRemoteDirectory("/tmp");
        session.setListener(new LogListener() {
            public void log(Level level, String message) {
                logger.log(level, message);
            }
        });
        session.setVerbose(true);
        session.upload(f);
    }

}
