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
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Main application and Tifixity API.
 */
public class Tifixity {

    protected static boolean verbose = false;                   // Verbose output required
    private static Properties properties = new Properties();    // Default properties. Contain details from POM.

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
     * Prints the Help menu
     * @param options
     */
    private static void printHelp(Options options){
        String usage  = "java -jar "+properties.getProperty("project.jar")+" <tiff>";
        String header = "Calculate the MD5 checksum of the image portion of the specified TIFF file\n\n";
        String footer = "\nPlease report issues at https://github.com/pmay/tifixity/issues";

        HelpFormatter helpformatter = new HelpFormatter();
        helpformatter.printHelp(usage, header, options, footer, true);
    }


    /**
     * Main application that checksums the image payload of the supplied TIFF file.
     * @param args
     */
    public static void main(String[] args) throws ParseException{
        // Load Properties file containing references to version/filenames
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classloader.getResourceAsStream(".properties")){
            properties.load(in);
        } catch (IOException ioe){
            System.err.println("Problem reading properties: "+ioe);
        }

        // Define options
        Options options = new Options();
        options.addOption("h", "help", false, "Print this message");
        options.addOption("v", "verbose", false, "Print verbose output");
        options.addOption("version", "Print version");

        // Parse the command line arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Process arguments
        if (cmd.hasOption("v")){
            verbose=true;
        }

        if (cmd.hasOption("h")){
            printHelp(options);
            System.exit(0);
        }

        if (cmd.hasOption("version")){
            System.out.println("version: "+properties.getProperty("project.version"));
            System.exit(0);
        }

        // Remaining arguments should be filenames
        String[] files = cmd.getArgs();
        if (files.length==0) {
            printHelp(options);
        }

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
