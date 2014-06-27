package ch.gadp.scripts.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;

/**
 * Created by guy on 18.12.13.
 */
public class SFTPDownload extends SFTPTask {


    private List<ChannelSftp.LsEntry> getFiles(ChannelSftp channel) throws SftpException {

        List<ChannelSftp.LsEntry> remoteFiles = new ArrayList<ChannelSftp.LsEntry>();

        Pattern pattern = Pattern.compile(this.getRemoteFilename());
        Vector<ChannelSftp.LsEntry> files = channel.ls(".");
        for (ChannelSftp.LsEntry f : files) {
            if(!(f instanceof com.jcraft.jsch.ChannelSftp.LsEntry)) {
                continue;
            }
            if (this.isRegex() && pattern.matcher(f.getFilename()).matches()) {
                remoteFiles.add(f);
            } else if (!this.isRegex() && f.getFilename().equals(this.getRemoteFilename())) {
                remoteFiles.add(f);
            }
        }

        return remoteFiles;
    }


    @Override
    public void runCommandOnChannel(ChannelSftp channel) throws SftpException, FileNotFoundException {
        try {
            channel.cd(this.getRemoteFolder());
        } catch (SftpException e) {
            if(e.id == SSH_FX_NO_SUCH_FILE && isFailSafe()) {
                System.out.println("Remote Folder '" + getRemoteFolder() + "' does not exist but command fails safe.");
                return;
            }
            throw e;
        }

        List<ChannelSftp.LsEntry> fileNames = this.getFiles(channel);

        String localFilename = this.getLocalFilename();
        if (fileNames.size() > 1) {
            localFilename = null;
        }


        for (ChannelSftp.LsEntry lsEntry : fileNames) {
            String targetName = localFilename;
            // Declare the local file
            if (targetName == null) {
                targetName = lsEntry.getFilename();
            }
            File output = new File(this.getLocalFolder(), targetName);

            // Do not overwrite local file
            if (!isOverwriteExistingTarget() && output.exists()) {
                continue;
            }

            // Effectively get the file
            OutputStream os = new FileOutputStream(output);
            channel.get(lsEntry.getFilename(), os);

            // Validate the download and delete the file if wrong
            if (lsEntry.getAttrs().getSize() != output.length()) {
                output.delete();
                throw new SftpException(1, "Downloaded file size does not match remote file size");
            }

            if (getArchiveAfterTransfer() != null) {
                channel.rename(lsEntry.getFilename(), getArchiveAfterTransfer() + "/" + lsEntry.getFilename() );
            }

            if (isDeleteAfterTransfer() && getArchiveAfterTransfer() == null) {
                channel.rm(lsEntry.getFilename());
            }

        }
    }

    protected SFTPDownload(String localFolder,
                           String localFilename,
                           String remoteHost,
                           int port,
                           String user,
                           String password,
                           String remoteFolder,
                           String remoteFilename,
                           boolean deleteAfterTransfer,
                           String archiveAfterTransfer,
                           boolean overwriteExistingTarget,
                           boolean regex,
                           boolean noHostCheck,
                           boolean failSafe) {
        super(localFolder, localFilename, remoteHost, port, user, password, remoteFolder, remoteFilename, deleteAfterTransfer, archiveAfterTransfer, overwriteExistingTarget, regex, noHostCheck, failSafe);
    }


}
