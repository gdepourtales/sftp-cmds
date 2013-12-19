package ch.gadp.scripts.sftp;

import com.jcraft.jsch.*;

import org.apache.commons.io.FileUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by guy on 18.12.13.
 */
public class SFTPDownloadTest implements PasswordAuthenticator {

    private final static String USER = "user";
    private final static String PASSWORD = "password";
    private final static String HOST = "localhost";
    private final static String REMOTE_FOLDER = "upload-test";
    private final static String ARCHIVE_FOLDER = "archive-test";
    private final static int PORT = 22222;

    private SshServer sshd;

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        return USER.equals(username) && PASSWORD.equals(password);
    }

    @Before
    public void setUp() throws Exception {

        FileUtils.deleteDirectory(new File(REMOTE_FOLDER));
        new File("hostkey.ser").delete();

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(PORT);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));

        sshd.setPasswordAuthenticator(this);
        sshd.setCommandFactory(new ScpCommandFactory());

        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);

        sshd.start();

        JSch jsch = new JSch();
        Session session = jsch.getSession(USER, HOST, PORT);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(PASSWORD);

        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp sftpChannel = (ChannelSftp) channel;
        final String testFileContents = "some file contents";
        String uploadedFileName1 = "test1.file";
        String uploadedFileName2 = "test2.file";
        sftpChannel.mkdir(REMOTE_FOLDER);
        sftpChannel.cd(REMOTE_FOLDER);
        sftpChannel.put(new ByteArrayInputStream(testFileContents.getBytes()), uploadedFileName1);
        sftpChannel.put(new ByteArrayInputStream(testFileContents.getBytes()), uploadedFileName2);
        sftpChannel.mkdir(ARCHIVE_FOLDER);
    }

    @After
    public void tearDown() throws Exception {
        sshd.stop(true);
        FileUtils.deleteDirectory(new File(REMOTE_FOLDER));
        new File("hostkey.ser").delete();
    }



    @Test
    public void test01() throws FileNotFoundException, JSchException, SftpException {
        SFTPDownload command = new SFTPDownload(
                ".",
                "test.file",
                HOST,
                PORT,
                USER,
                PASSWORD,
                REMOTE_FOLDER,
                "test1.file",
                false,
                null,
                false,
                false,
                true
        );
        command.execute();
        File f = new File(".", "test.file");
        assertTrue(f.exists());
        f.delete();
    }

    @Test
    public void testNoOverwrite() throws IOException, JSchException, SftpException {
        File f = new File(".", "test.file");
        assertFalse(f.exists());
        f.createNewFile();

        SFTPDownload command = new SFTPDownload(
                ".",
                "test.file",
                HOST,
                PORT,
                USER,
                PASSWORD,
                REMOTE_FOLDER,
                "test1.file",
                false,
                null,
                false,
                false,
                true
        );
        command.execute();
        assertTrue(f.exists());
        assertEquals(0, f.length());
        f.delete();
    }

    @Test
    public void testOverwrite() throws IOException, JSchException, SftpException {
        File f = new File(".", "test.file");
        assertFalse(f.exists());
        f.createNewFile();

        SFTPDownload command = new SFTPDownload(
                ".",
                "test.file",
                HOST,
                PORT,
                USER,
                PASSWORD,
                REMOTE_FOLDER,
                "test1.file",
                false,
                null,
                true,
                false,
                true
        );
        command.execute();
        assertTrue(f.exists());
        assertTrue(f.length() != 0);
        f.delete();
    }

    @Test
    public void testRegex() throws IOException, JSchException, SftpException {

        SFTPDownload command = new SFTPDownload(
                ".",
                null,
                HOST,
                PORT,
                USER,
                PASSWORD,
                REMOTE_FOLDER,
                "t[\\w]+[1,2]\\.file",
                false,
                null,
                false,
                true,
                true
        );
        command.execute();
        File f1 = new File(".", "test1.file");
        assertTrue(f1.exists());
        assertTrue(f1.length() != 0);
        f1.delete();
        File f2 = new File(".", "test2.file");
        assertTrue(f2.exists());
        assertTrue(f2.length() != 0);
        f2.delete();

    }

    @Test
    public void testDelete() throws IOException, JSchException, SftpException {

        SFTPDownload command = new SFTPDownload(
                ".",
                "test.file",
                HOST,
                PORT,
                USER,
                PASSWORD,
                REMOTE_FOLDER,
                "test1.file",
                true,
                null,
                false,
                false,
                true
        );
        command.execute();
        File f = new File(".", "test.file");
        assertTrue(f.exists());
        f.delete();

        // If we rerun the same command the file should not be downloaded because away
        command.execute();
        f = new File(".", "test.file");
        assertFalse(f.exists());
    }

    @Test
    public void testArchive() throws IOException, JSchException, SftpException {

        SFTPDownload command = new SFTPDownload(
                ".",
                "test.file",
                HOST,
                PORT,
                USER,
                PASSWORD,
                REMOTE_FOLDER,
                "test1.file",
                false,
                ARCHIVE_FOLDER,
                false,
                false,
                true
        );
        command.execute();
        File f = new File(".", "test.file");
        assertTrue(f.exists());
        f.delete();

        // If we rerun the same command the file should be downloaded from the archive because
        command.setRemoteFolder(REMOTE_FOLDER + "/" + ARCHIVE_FOLDER);
        command.setArchiveAfterTransfer(null);
        command.execute();
        f = new File(".", "test.file");
        assertTrue(f.exists());
        f.delete();
    }



}
