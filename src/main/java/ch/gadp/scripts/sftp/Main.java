package ch.gadp.scripts.sftp;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by guy on 18.12.13.
 */
public class Main {

    private static final String CONFIG_FILE = "config";
    private static final String COMMAND = "command";
    private static final String DELETE = "delete";
    private static final String ARCHIVE = "archive";
    private static final String LOCAL_FOLDER = "local-folder";
    private static final String LOCAL_FILE = "local";
    private static final String REMOTE_HOST = "remote-host";
    private static final String REMOTE_PORT = "remote-port";
    private static final String REMOTE_USER = "user";
    private static final String REMOTE_PASSWORD = "password";
    private static final String REMOTE_FOLDER = "remote-folder";
    private static final String REMOTE_FILE = "remote";
    private static final String OVERWRITE = "overwrite";
    private static final String REGEX = "regex";
    private static final String NO_HOST_CHECK = "no-host-check";
    private static final String FAIL_SAFE = "fail-safe";


    private static Options getOptions() {
        Options options = new Options();


        options.addOption("?", "help", false, "help");

        options.addOption("g", CONFIG_FILE, true, "configuration file: configuration file let you put the arguments in a file");
        options.addOption("c", COMMAND, true, "command (required)");
        options.getOption(COMMAND).setRequired(true);

        options.addOption("i", LOCAL_FOLDER, true, "local folder.");
        options.addOption("l", LOCAL_FILE, true, "local filename");

        options.addOption("h", REMOTE_HOST, true, "remote host");
        options.getOption(REMOTE_HOST).setRequired(true);
        options.addOption("p", REMOTE_PORT, true, "remote port (default: 22)");
        options.addOption("u", REMOTE_USER, true, "user");
        options.addOption("P", REMOTE_PASSWORD, true, "password");

        options.addOption("o", REMOTE_FOLDER, true, "remote folder");
        options.addOption("r", REMOTE_FILE, true, "remote filename");

        options.addOption("d", DELETE, false, "delete source file after processing and validate the transfer");
        options.addOption("a", ARCHIVE, true, "archive the file in the folder after downloading and validate the transfer. If 'd' and 'a' are specified, 'a' is discarded");
        options.addOption("w", OVERWRITE, false, "overwrite existing file if exists. If not specified the transfer is cancelled if the target file already exists");
        options.addOption("x", REGEX, false, "if mentioned, the source should be handled as regular expression, potentially dowloading/uploading several files");
        options.addOption("y", NO_HOST_CHECK, false, "do not check hosts certificates");
        options.addOption("f", FAIL_SAFE, false, "fail safe if remote directory does not exist");

        return options;
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar sftp-cmds-<version>.jar", getOptions());
    }

    protected static SFTPTask getCommand(String[] args) throws ParseException, IOException {

        CommandLineParser parser = new GnuParser();

        Options options = getOptions();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption("help")) {
            printHelp();
            return null;
        }

        if (line.hasOption(CONFIG_FILE)) {
            StringWriter sw = new StringWriter();
            FileReader fr = new FileReader(line.getOptionValue(CONFIG_FILE));
            int c;

            while ((c = fr.read()) != -1) {
                sw.write(c);
            }
            line = parser.parse(options, sw.getBuffer().toString().split("\\s"));
        }

        if (!line.hasOption(COMMAND)) {
            printHelp();
            System.exit(1);
        }

        String commandName = line.getOptionValue(COMMAND);

        SFTPTask command = null;
        if (commandName.equals("download")) {
            command = new SFTPDownload(
                    line.getOptionValue(LOCAL_FOLDER, "."),
                    line.getOptionValue(LOCAL_FILE, null),
                    line.getOptionValue(REMOTE_HOST, null),
                    Integer.parseInt(line.getOptionValue(REMOTE_PORT, "22")),
                    line.getOptionValue(REMOTE_USER, null),
                    line.getOptionValue(REMOTE_PASSWORD, null),
                    line.getOptionValue(REMOTE_FOLDER, null),
                    line.getOptionValue(REMOTE_FILE, null),

                    line.hasOption(DELETE),
                    line.getOptionValue(ARCHIVE, null),
                    line.hasOption(OVERWRITE),
                    line.hasOption(REGEX),
                    line.hasOption(NO_HOST_CHECK),
                    line.hasOption(FAIL_SAFE)
            );
        }
        return command;
    }


    public final static void main(String[] args) {


        try {

            SFTPTask task = getCommand(args);
            if (task != null) {
                task.execute();
            }

        } catch (ParseException pe) {
            System.out.println( "Unexpected exception:" + pe.getMessage() );
            System.exit(1);
        } catch (SftpException e) {
            System.out.println("Unexpected exception:" + e.getMessage());
            System.exit(1);
        } catch (JSchException e) {
            System.out.println("Unexpected exception:" + e.getMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.out.println("Unexpected exception:" + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Unexpected exception:" + e.getMessage());
            System.exit(1);
        }
    }

}
