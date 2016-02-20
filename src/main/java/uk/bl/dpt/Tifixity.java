/**
 * Copyright 2016 Peter May
 * Author: Peter May
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.bl.dpt;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Main application and Tifixity API.
 */
public class Tifixity {

    public static boolean verbose = false;  // Verbose output required

    private static ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    /**
     * Returns the full checksum for the specified file.
     * @param file
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String checksumFile(String file) throws NoSuchAlgorithmException, IOException{
        MessageDigest md = MessageDigest.getInstance("MD5");
        MessageDigest partialmd = MessageDigest.getInstance("MD5");

        try (InputStream is = Files.newInputStream(Paths.get(file))) {
            DigestInputStream dis = new DigestInputStream(is, md);
            DigestInputStream pdis = new DigestInputStream(dis, partialmd);

            int count;
            while ((count=dis.read()) > -1) {
                count=pdis.read();
            }
        }

        StringBuilder digest = new StringBuilder();
        for (byte b: md.digest()){
            digest.append(String.format("%02x", b));
        }

//        digest.append("\n");
//        for (byte b: partialmd.digest()){
//            digest.append(String.format("%02x", b));
//        }
        return digest.toString();
    }

    private static int BUFFERSIZE = 100;

    /**
     * Returns the image payload checksumFile of the specified file's subfile.
     * @param file      the TIFF file to checksumFile
     * @param subfile   the subfile index (0 indexed)
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String checksumImage(String file, int subfile) throws IOException, NoSuchAlgorithmException {
        Tiff tiff = TiffFileHandler.loadTiffFromFile(Paths.get(file));

        Integer[] rgbIndexes = tiff.getImageDataOffsets(0);
        Integer[] rgbLengths = tiff.getImageDataLengths(0);

        assert(rgbIndexes.length == rgbLengths.length);

        MessageDigest md = MessageDigest.getInstance("MD5");

        try (SeekableByteChannel sbc = Files.newByteChannel(Paths.get(file))) {
            ByteBuffer buf = ByteBuffer.allocate(BUFFERSIZE);

            int totalBytesRead;
            int bytesRead;
            for(int j=0; j<rgbIndexes.length; j++){
                // Do not assume split data is in sequential order in the file.
                // jump to next position and read the data in
                sbc.position(rgbIndexes[j]);
                buf.clear();

                totalBytesRead=0;
                bytesRead=0;
                while(totalBytesRead < rgbLengths[j]){
                    bytesRead = sbc.read(buf);
                    buf.flip();

                    if(totalBytesRead+bytesRead>=rgbLengths[j]){
                        md.update(buf.array(), buf.position(), (rgbLengths[j]-totalBytesRead));
                    } else {
                        md.update(buf);
                    }

                    totalBytesRead += bytesRead;
                    buf.rewind();
                }
            }
        }

        StringBuilder digest = new StringBuilder();
        for (byte b: md.digest()){
            digest.append(String.format("%02x", b));
        }

        return digest.toString();
    }


    /**
     * Main application that checksums the image payload of the supplied TIFF file.
     * @param args
     */
    public static void main(String[] args) throws ParseException{
        // Define options
        Options options = new Options();
        options.addOption("v", "verbose", false, "Display verbose output");

        // Parse the command line arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Process arguments
        if (cmd.hasOption("v")){
            verbose=true;
        }

        // Remaining arguments should be filenames
        String[] files = cmd.getArgs();
        for(int i=0; i<files.length; i++){
            try {
                String hash = checksumImage(files[i], 0);
                System.out.println(hash);
            } catch (NoSuchFileException nsfe){
                System.err.println("No such file: "+files[i]);
                System.exit(-1);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
