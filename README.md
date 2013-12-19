# sftp-cmds

sftp-cmds is a bunch of CLI commands. They've been written because sftp cannot be run in batch mode with password and
does not contain any validity check of the transfer.

Initial version  only has "download" command


# Options

usage: java -jar sftp-cmds-<version>.jar
 -?,--help                  help
 -a,--archive <arg>         archive the file in the folder after
                            downloading and validate the transfer. If 'd'
                            and 'a' are specified, 'a' is discarded
 -c,--command <arg>         command (required)
 -d,--delete                delete source file after processing and
                            validate the transfer
 -g,--config <arg>          configuration file: configuration file let you
                            put the arguments in a file
 -h,--remote-host <arg>     remote host
 -i,--local-folder <arg>    local folder.
 -l,--local <arg>           local filename
 -o,--remote-folder <arg>   remote folder
 -P,--password <arg>        password
 -p,--remote-port <arg>     remote port (default: 22)
 -r,--remote <arg>          remote filename
 -u,--user <arg>            user
 -w,--overwrite             overwrite existing file if exists. If not
                            specified the transfer is cancelled if the
                            target file already exists
 -x,--regex                 if mentioned, the source should be handled as
                            regular expression, potentially
                            dowloading/uploading several files
 -y,--no-host-check         do not check hosts certificates

