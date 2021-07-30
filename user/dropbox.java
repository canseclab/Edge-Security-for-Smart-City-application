import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.RetryException;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.util.IOUtil.ProgressListener;
import com.dropbox.core.v1.DbxEntry;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishErrorException;
import com.dropbox.core.v2.files.UploadSessionLookupErrorException;
import com.dropbox.core.v2.files.WriteMode;

public class dropbox {
	private static final String ACCESS_TOKEN = "";

    public static void main(String args[]) throws DbxException {
        // Create Dropbox client
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
    }
    
    public static void uploadFile(String file, String path) {
    	File localFile = new File(path);
        try (InputStream in = new FileInputStream(localFile)) {
        	DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            DbxClientV2 dbxClient = new DbxClientV2(config, ACCESS_TOKEN);
            ProgressListener progressListener = l -> printProgress(l, localFile.length());
            FileMetadata metadata = dbxClient.files().uploadBuilder("/"+file)
                .withMode(WriteMode.ADD)
                .withClientModified(new Date(localFile.lastModified()))
                .uploadAndFinish(in, progressListener);
            	
            System.out.println(metadata.toStringMultiline());
        } catch (UploadErrorException ex) {
            System.err.println("Error uploading to Dropbox:- " + ex.getMessage());
            System.exit(1);
        } catch (DbxException ex) {
            System.err.println("Error uploading to Dropbox:- " + ex.getMessage());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
            System.exit(1);
        }
    }
    public static void getFile(String file, String path) {
        	DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            DbxClientV2 dbxClient = new DbxClientV2(config, ACCESS_TOKEN);
            FileOutputStream outputStream = null;
            File outputFile = new File(path);
            try {
              outputStream = new FileOutputStream(outputFile);
              dbxClient.files().download("/"+ file).download(outputStream);
            } catch (DbxException | IOException ex) {
            	System.err.println("Error uploading to Dropbox: " + ex.getMessage());
            }
    }
    private static void printProgress(long uploaded, long size) {
        System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
    }
}
