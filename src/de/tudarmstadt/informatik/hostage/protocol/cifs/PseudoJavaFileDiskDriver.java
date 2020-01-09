package de.tudarmstadt.informatik.hostage.protocol.cifs;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.filesys.FileExistsException;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.smb.server.disk.JavaFileDiskDriver;
import org.alfresco.jlan.smb.server.disk.JavaNetworkFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.tudarmstadt.informatik.hostage.logging.MessageRecord;
import de.tudarmstadt.informatik.hostage.protocol.SMB;

/**
 * HostageV3
 * ================
 * @author Alexander Brakowski
 * @author Daniel Lazar
 *
 * This is a pseudo file disk driver, which overwrites the libs JavaFileDiskDriver,
 * so that we can get more information about the attack
 */
public class PseudoJavaFileDiskDriver extends JavaFileDiskDriver {
    private static class PseudoJavaNetworkFile extends JavaNetworkFile {
        protected final SMB SMB;
        private final SrvSession sess;
        boolean wasWrittenTo = false;

        public PseudoJavaNetworkFile(File file, String netPath, SMB SMB, SrvSession sess) {
            super(file, netPath);
            this.SMB = SMB;
            this.sess = sess;
        }

        /**
         * method that checks if the file was just written, then gets the MD5 checksum of the
         * file and logs it. Afterwards the file gets deleted.
         * @throws java.io.IOException
         */
        public void closeFile() throws java.io.IOException {
            super.closeFile();
            if(wasWrittenTo){
                try {
                    MessageDigest digest = MessageDigest.getInstance("MD5");
                    FileInputStream fis = new FileInputStream(m_file);

                    byte[] buffer = new byte[8192];
                    int numOfBytesRead;
                    while( (numOfBytesRead = fis.read(buffer)) > 0){
                        digest.update(buffer, 0, numOfBytesRead);
                    }

                    byte[] hash = digest.digest();
                    String checksum = new BigInteger(1, hash).toString(16);

                    String message = "File received: " + m_file.getName() + "\n\nCHECKSUM:\n" + checksum;

                    SMB.log(MessageRecord.TYPE.RECEIVE, message, 445, sess.getRemoteAddress(), 445);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                m_file.delete();
                wasWrittenTo = false;
            }
        }

        public void writeFile(byte[] buf, int len, int pos)
                throws java.io.IOException {
            super.writeFile(buf, len, pos);
            wasWrittenTo = true;
        }

        public void writeFile(byte[] buf, int len, int pos, long offset)
                throws java.io.IOException {
            super.writeFile(buf, len, pos, offset);
            wasWrittenTo = true;
        }
    }

    private final SMB SMB;

    public PseudoJavaFileDiskDriver(SMB SMB) {
        this.SMB = SMB;
    }

    public NetworkFile createFile(SrvSession sess, TreeConnection tree, FileOpenParams params)
            throws java.io.IOException {
        DeviceContext ctx = tree.getContext();
        String fname = FileName.buildPath(ctx.getDeviceName(), params.getPath(), null, java.io.File.separatorChar);

        //  Check if the file already exists

        File file = new File(fname);
        if (file.exists())
            throw new FileExistsException();

        //  Create the new file

        FileWriter newFile = new FileWriter(fname, false);
        newFile.close();

        //  Create a Java network file

        file = new File(fname);
        PseudoJavaNetworkFile netFile = new PseudoJavaNetworkFile(file, params.getPath(), SMB, sess);
        netFile.setGrantedAccess(NetworkFile.READWRITE);
        netFile.setFullName(params.getPath());

        //  Return the network file

        return netFile;
    }
}
