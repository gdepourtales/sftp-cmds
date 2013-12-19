package ch.gadp.scripts.sftp;

import junit.framework.Assert;
import org.junit.Test;

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

        Assert.assertEquals(".", task.getLocalFolder());
        Assert.assertEquals("test.zip", task.getLocalFilename());
        Assert.assertEquals("localhost", task.getRemoteHost());
        Assert.assertEquals("upload", task.getUser());
        Assert.assertEquals("password", task.getPassword());
        Assert.assertEquals("upload", task.getRemoteFolder());
        Assert.assertEquals("remote.zip", task.getRemoteFilename());
        Assert.assertTrue(task.isNoHostCheck());

    }
}
