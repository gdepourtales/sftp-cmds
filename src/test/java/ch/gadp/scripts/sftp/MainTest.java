package ch.gadp.scripts.sftp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by guy on 18.12.13.
 */
public class MainTest {
    @Test
    public void testGetCommand() throws Exception {
        String path = MainTest.class.getResource("/parameters1.txt").getPath();
        SFTPTask task = Main.getCommand(new String[]{
                "--command", "download", "--config", path
        });

        assertEquals(".", task.getLocalFolder());
        assertEquals("test.zip", task.getLocalFilename());
        assertEquals("localhost", task.getRemoteHost());
        assertEquals("upload", task.getUser());
        assertEquals("password", task.getPassword());
        assertEquals("upload", task.getRemoteFolder());
        assertEquals("remote.zip", task.getRemoteFilename());
        assertTrue(task.isNoHostCheck());
        assertTrue(task.isFailSafe());
    }
}
