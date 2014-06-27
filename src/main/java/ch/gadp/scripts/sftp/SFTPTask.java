package ch.gadp.scripts.sftp;

import com.jcraft.jsch.*;

import java.io.FileNotFoundException;

/**
 * Created by guy on 18.12.13.
 */
abstract class SFTPTask {

    private String localFolder;
    private String localFilename;
    private String remoteHost;

    private int port;
    private String user;
    private String password;
    private String remoteFolder;

    private String remoteFilename;
    private boolean deleteAfterTransfer = false;

    private String archiveAfterTransfer = null;
    private boolean overwriteExistingTarget = false;
    private boolean regex = false;
    private boolean noHostCheck = false;
    private boolean failSafe = false;

    protected Session getSession() throws JSchException {
        JSch jsch = new JSch();

        Session session = jsch.getSession(getUser(), getRemoteHost(), getPort());
        session.setPassword(getPassword());

        if (isNoHostCheck()) {
            session.setConfig("StrictHostKeyChecking", "no");
        }

        return session;
    }


    protected ChannelSftp getSFTPChannel() throws JSchException {
        Session session = this.getSession();

        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();

        return (ChannelSftp) channel;
    }

    public abstract void runCommandOnChannel(ChannelSftp channel) throws SftpException, FileNotFoundException;

    public final void execute() throws JSchException, SftpException, FileNotFoundException {
        ChannelSftp sftpChannel = this.getSFTPChannel();

        runCommandOnChannel(sftpChannel);

        sftpChannel.exit();
        sftpChannel.getSession().disconnect();

    }

    protected SFTPTask(String localFolder, String localFilename, String remoteHost, String user, String password, String remoteFolder, String remoteFilename) {
        this(localFolder, localFilename, remoteHost, 22, user, password, remoteFolder, remoteFilename, false, null, false, false, false, false);
    }


    protected SFTPTask(String localFolder, String localFilename, String remoteHost, int port, String user, String password, String remoteFolder, String remoteFilename, boolean deleteAfterTransfer, String archiveAfterTransfer, boolean overwriteExistingTarget, boolean regex, boolean noHostCheck, boolean failSafe) {
        this.localFolder = localFolder;
        this.localFilename = localFilename;
        this.remoteHost = remoteHost;
        this.port = port;
        this.user = user;
        this.password = password;
        this.remoteFolder = remoteFolder;
        this.remoteFilename = remoteFilename;
        this.deleteAfterTransfer = deleteAfterTransfer;
        this.archiveAfterTransfer = archiveAfterTransfer;
        this.overwriteExistingTarget = overwriteExistingTarget;
        this.regex = regex;
        this.noHostCheck = noHostCheck;
        this.failSafe = failSafe;
    }

    public String getLocalFolder() {
        return localFolder;
    }

    public void setLocalFolder(String localFolder) {
        this.localFolder = localFolder;
    }

    public String getLocalFilename() {
        return localFilename;
    }

    public void setLocalFilename(String localFilename) {
        this.localFilename = localFilename;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemoteFolder() {
        return remoteFolder;
    }

    public void setRemoteFolder(String remoteFolder) {
        this.remoteFolder = remoteFolder;
    }

    public String getRemoteFilename() {
        return remoteFilename;
    }

    public void setRemoteFilename(String remoteFilename) {
        this.remoteFilename = remoteFilename;
    }

    public boolean isDeleteAfterTransfer() {
        return deleteAfterTransfer;
    }

    public void setDeleteAfterTransfer(boolean deleteAfterTransfer) {
        this.deleteAfterTransfer = deleteAfterTransfer;
    }

    public String getArchiveAfterTransfer() {
        return archiveAfterTransfer;
    }

    public void setArchiveAfterTransfer(String archiveAfterTransfer) {
        this.archiveAfterTransfer = archiveAfterTransfer;
    }

    public boolean isOverwriteExistingTarget() {
        return overwriteExistingTarget;
    }

    public void setOverwriteExistingTarget(boolean overwriteExistingTarget) {
        this.overwriteExistingTarget = overwriteExistingTarget;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public boolean isNoHostCheck() {
        return noHostCheck;
    }

    public void setNoHostCheck(boolean noHostCheck) {
        this.noHostCheck = noHostCheck;
    }

    public boolean isFailSafe() {
        return failSafe;
    }

    public void setFailSafe(boolean failSafe) {
        this.failSafe = failSafe;
    }
}
