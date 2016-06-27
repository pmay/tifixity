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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Main application and Tifixity API.
 */
public class Tifixity {

    protected static boolean allChecksums   = false;               // calculate full and non-image data checksums
    protected static boolean verbose        = false;               // Verbose output required
    private static Properties properties    = new Properties();    // Default properties. Contain details from POM.

    private static int BUFFERSIZE = 100;

    /**
     * Calculates the full and non-image-data checksum for the specified file.
     * @param file  the TIFF file to checksum
     * @return  String[] first element is the full digest, 2nd element is the partial non-image data checksum
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String[] checksumFile(String file) throws NoSuchAlgorithmException, IOException{
        Tiff tiff = TiffFileHandler.loadTiffFromFile(file); //Paths.get(file));

        // get the full and partial digests
        return calculateFileDigest(tiff);
    }


    /**
     * Calculates the full and non-image-data checksums for the specified TIFF.
     * @param tiff  the {@link Tiff} object to calculate the checksum on
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static String[] calculateFileDigest(Tiff tiff) throws IOException, NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("MD5");            // full checksum
        MessageDigest md_rem = MessageDigest.getInstance("MD5");        // non-image-data checksum

        try (InputStream is = Files.newInputStream(tiff.getFilePath())){
            DigestInputStream dis = new DigestInputStream(is, md);
            DigestInputStream pdis = new DigestInputStream(dis, md_rem);

            TreeMap<Long, Boolean> structure = tiff.getStructure();
            Iterator<Long> indexes = structure.navigableKeySet().iterator();
            Long index = indexes.next();    // first index = 0

            int count=0;
            do {
                // check if DigestInputStream should be on or off then increment count
                if(count==index){
                    pdis.on(!structure.get(index));  // turn on if not image data
                    if (indexes.hasNext()) {
                        index = indexes.next();
                    }
                }
                count++;
            } while (pdis.read() > -1);
        }

        // Format to String[]
        String[] checksums = new String[2];
        checksums[0] = checksumAsString(md.digest());
        checksums[1] = checksumAsString(md_rem.digest());

        return checksums;
    }

    /**
     * Returns image payload checksums for each subfile within the specified TIFF
     * @param file  the TIFF file to checksum
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String[] checksumImage(String file) throws IOException, NoSuchAlgorithmException {
        //Path p = Paths.get(file);
        Tiff tiff = TiffFileHandler.loadTiffFromFile(file); //Paths.get(file));

        String[] checksums = new String[tiff.numberOfIFDs()];

        for(int i=0; i<tiff.numberOfIFDs(); i++){
            checksums[i] = calculateImageDigest(tiff, i);
        }

        return checksums;
    }

    /**
     * Returns the image payload checksum of the specified file's subfile.
     * @param file      the TIFF file to checksum
     * @param subFile   the subfile index (0 indexed)
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String checksumImage(String file, int subFile)
            throws IOException, NoSuchAlgorithmException {
        Tiff tiff = TiffFileHandler.loadTiffFromFile(file); //Paths.get(file));
        return calculateImageDigest(tiff, subFile);
    }

    /**
     * Calculates the checksum for the image in the specified file
     * @param tiff      the TIFF file to checksum
     * @param subFile   the subfile index (0 indexed)
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static String calculateImageDigest(Tiff tiff, int subFile)
            throws IOException, NoSuchAlgorithmException {
        if (tiff==null){
            System.err.println("No TIFF file");
            System.exit(-2);
        }

        Integer[] imageIndexes = tiff.getImageDataOffsets(subFile);
        Integer[] imageLengths = tiff.getImageDataLengths(subFile);

        assert(imageIndexes.length == imageLengths.length);

        // Message Digests
        MessageDigest md = MessageDigest.getInstance("MD5");

        try (SeekableByteChannel sbc = Files.newByteChannel(tiff.getFilePath())) {
            ByteBuffer buf = ByteBuffer.allocate(BUFFERSIZE);

            int totalBytesRead;
            int bytesRead;

            for(int j=0; j<imageIndexes.length; j++){
                // Do not assume split data is in sequential order in the file.
                // jump to next position and read the data in
                sbc.position(imageIndexes[j]);
                buf.clear();

                totalBytesRead=0;
                while(totalBytesRead < imageLengths[j]){
                    bytesRead = sbc.read(buf);
                    buf.flip();

                    if(totalBytesRead+bytesRead>=imageLengths[j]){
                        md.update(buf.array(), buf.position(), (imageLengths[j]-totalBytesRead));
                    } else {
                        md.update(buf);
                    }

                    totalBytesRead += bytesRead;
                    buf.rewind();
                }
            }
        }

        return checksumAsString(md.digest());
    }

    /**
     * Calculates the checksums for each IFD
     * @param file  the TIFF file to checksum
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String[] checksumIFDs(String file)
            throws IOException, NoSuchAlgorithmException {
        Tiff tiff = TiffFileHandler.loadTiffFromFile(file); //Paths.get(file));

        String[] checksums = new String[tiff.numberOfIFDs()];

        for(int i=0; i<tiff.numberOfIFDs(); i++){
            checksums[i] = calculateIFDDigest(tiff, i);
        }

        return checksums;
    }

    /**
     * Calculates the checksum for the specified IFD
     * @param file      the TIFF file to checksum
     * @param subFile   the subfile index (0 indexed)
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String checksumIFD(String file, int subFile)
            throws IOException, NoSuchAlgorithmException {
        Tiff tiff = TiffFileHandler.loadTiffFromFile(file); //Paths.get(file));
        return calculateIFDDigest(tiff, subFile);
    }

    /**
     * Calculates the checksum for the specified IFD
     * @param tiff      the TIFF file to checksum
     * @param subFile   the subfile index (0 indexed)
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static String calculateIFDDigest(Tiff tiff, int subFile)
            throws IOException, NoSuchAlgorithmException {
        if (tiff==null){
            System.err.println("No TIFF file");
            System.exit(-2);
        }

        // Get the IFD
        IFD ifd = tiff.getIFD(subFile);
        long ifdoffset = ifd.getOffset();
        int numDirs = ifd.numberOfDirectoryEntries();

        // Message Digests
        MessageDigest md = MessageDigest.getInstance("MD5");

        try (SeekableByteChannel sbc = Files.newByteChannel(tiff.getFilePath())) {
            // jump to the IFD offset
            sbc.position(ifdoffset);

            // read the IFD
            ByteBuffer buf = ByteBuffer.allocate(2+(numDirs*12)+4);
            buf.clear();
            int bytesRead;
            if((bytesRead=sbc.read(buf))>-1){
                buf.flip();
                md.update(buf);
            }

            // now read the pointed to data for each IFD
            Iterator<Integer> directoryIterator = ifd.getDirectoryKeys().iterator();
            while(directoryIterator.hasNext()){
                IFD.DirectoryEntry entry = ifd.getDirectoryEntry(directoryIterator.next());
                if(entry.isValuePointer()){
                    // jump to the pointer value
                    sbc.position(entry.getValueOffset());
                    buf = ByteBuffer.allocate(entry.getCount()*entry.getType().getNumBytes());
                    buf.clear();
                    if((bytesRead=sbc.read(buf))>-1){
                        buf.flip();
                        md.update(buf);
                    }
                }
            }
        }

        return checksumAsString(md.digest());
    }


    /**
     * Returns a String representation of the specified byte[] checksum.
     * @param checksum  the byte array checksum
     * @return
     */
    private static String checksumAsString(byte[] checksum){
        StringBuilder digest = new StringBuilder();
        for (byte b: checksum){
            digest.append(String.format("%02x", b));
        }

        return digest.toString();
    }

    /**
     * Formats the output depending on user request. Default is to output string with just the checksum
     * @return
     */
    private static String formatOutput(String fullCS, String partialCS, String[] imageCS, String[] ifdCS, String format){
        StringBuilder output = new StringBuilder();
        if(allChecksums) {
            output.append("Full MD5: ").append(fullCS).append("\n");
            output.append("Remaining MD5: ").append(partialCS).append("\n");
        }

        for(int i=0; i<imageCS.length; i++){
            output.append("Image MD5 [").append(i).append("]: ");
            output.append(imageCS[i]).append("\n");
        }

        if(allChecksums) {
            for (int i = 0; i < ifdCS.length; i++) {
                output.append("IFD MD5 [").append(i).append("]: ");
                output.append(ifdCS[i]).append("\n");
            }
        }
        return output.toString();
    }

    /**
     * Prints the Help menu
     * @param options   the CLI Options
     */
    private static void printHelp(Options options){
        String usage  = "java -jar "+properties.getProperty("project.jar")+" <tiffs>";
        String header = "Calculate the MD5 checksum of the image portion of the specified TIFF file(s)\n\n";
        String footer = "\nPlease report issues at https://github.com/pmay/tifixity/issues";

        HelpFormatter helpformatter = new HelpFormatter();
        helpformatter.printHelp(usage, header, options, footer, true);
    }


    /**
     * Main application that checksums the image payload of the supplied TIFF file.
     * @param args  main CLI arguments list
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
        options.addOption("a", "all", false, "Additionally, calculate full and partial checksums (non-image data, IFDs)");
        options.addOption("v", "verbose", false, "Print verbose output");
        options.addOption("version", "Print version");

        // Parse the command line arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Process arguments
        if (cmd.hasOption("a")){
            allChecksums=true;
        }

        // Verbose output
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
                String[] cs = new String[2];
                String[] ifdCS = null;

                if(allChecksums){
                    cs = checksumFile(files[i]);
                    ifdCS = checksumIFDs(files[i]);
                }

                String[] imageCS = checksumImage(files[i]);
                System.out.println(formatOutput(cs[0], cs[1], imageCS, ifdCS, "String"));


            } catch (NoSuchFileException nsfe){
                System.err.println("No such file: "+files[i]);
                System.exit(-1);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
