/**
 *
 */
package com.alcatel_lucent.dms.util;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mozilla.intl.chardet.HtmlCharsetDetector;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

import com.alcatel_lucent.dms.SystemError;

/**
 * @author guoshunw
 */
public class Util {

    public static final int UTF8_BOM_LENGTH = 3;
    public static final int UTF16_BOM_LENGTH = 2;

    private static String dctFileExtsPattern = ".di?ct?";
    private static String zipFileExtsPattern = ".(?:zip)|(?:jar)";
    private static String mdcFileExtsPattern = ".conf";


    private static Logger log = Logger.getLogger(Util.class);

    /**
     * <p>
     * Return the value collection of the specified property of the specified
     * bean in collection, no matter which property reference format is used,
     * with no type conversions.
     * </p>
     *
     * @param collection   the specified collection which contain beans
     * @param propertyName the property name need to be added in.
     * @return the property name collection
     */
    @SuppressWarnings("unchecked")
    public static List getObjectProperiesList(Collection collection,
                                              String propertyName) {
        List propertiesList = new ArrayList<Object>();
        for (Object obj : collection) {
            Object value = null;
            try {
                value = PropertyUtils.getProperty(obj, propertyName);
            } catch (IllegalAccessException e) {
                throw new SystemError(e.getMessage());
            } catch (InvocationTargetException e) {
                throw new SystemError(e.getMessage());
            } catch (NoSuchMethodException e) {
                throw new SystemError(e.getMessage());
            }
            propertiesList.add(value);
        }
        return propertiesList;
    }

    /**
     * Generate the specified number of space as a String.
     *
     * @param count the number of spaces.
     * @return String of the concatenated space
     */
    public static String generateSpace(int count) {
        if (count < 0) {
            throw new IllegalArgumentException(
                    "count must be greater than or equal 0.");
        }
        char[] chs = new char[count];
        for (int i = 0; i < count; i++) {
            chs[i] = ' ';
        }
        return new String(chs);
    }

    /**
     * Detect the encoding of a File by BOM(byte order mark).
     *
     * @param file given File
     * @return file encoding
     * @author Guoshun.Wu Date: 2012-07-01
     */
    public static String detectEncoding(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        byte[] buf = new byte[UTF8_BOM_LENGTH];
        is.read(buf);
        is.close();
        return detectEncoding(buf);
    }

    /**
     * Detect the encoding of a File by BOM(byte order mark).
     *
     * @param is given InputStream
     * @return file encoding
     * @author Guoshun.Wu Date: 2012-07-01
     */
    public static String detectEncoding(InputStream is) throws IOException {
        final StringBuilder encoding = new StringBuilder();

        nsDetector det = new nsDetector(nsPSMDetector.ALL);
        det.Init(new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
                HtmlCharsetDetector.found = true;
                log.info("CHARSET = " + charset);
                encoding.append(charset);
            }
        });
        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = true;

        while ((len = is.read(buf, 0, buf.length)) != -1) {

            // Check if the stream is only ascii.
            if (isAscii)
                isAscii = det.isAscii(buf, len);
            // DoIt if non-ascii and not done yet.
            if (!isAscii && !done)
                done = det.DoIt(buf, len, false);
        }
        det.DataEnd();
        if (encoding.toString().isEmpty()) {
            encoding.append("ISO-8859-1");
        }
        return encoding.toString();
    }

    public static String detectEncoding(byte[] bom) {
        byte[] utf8BOM = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf,};
        byte[] utf16LEBOM = new byte[]{(byte) 0xff, (byte) 0xfe};
        byte[] utf16BEBOM = new byte[]{(byte) 0xfe, (byte) 0xff};

        if (Arrays.equals(utf8BOM, bom)) {
            return "UTF-8";
        }
        if (Arrays.equals(utf16LEBOM, Arrays.copyOf(bom, UTF16_BOM_LENGTH))
                || Arrays.equals(utf16BEBOM,
                Arrays.copyOf(bom, UTF16_BOM_LENGTH))) {
            return "UTF-16LE";
        }
        return "ISO-8859-1";
    }

    /**
     * Tell if a specific file is a DCT file.
     */
    public static boolean isDCTFile(String fileName) {
        return isSpecificFile(fileName, dctFileExtsPattern);
    }

    public static boolean isMDCFile(String fileName) {
        return isSpecificFile(fileName, mdcFileExtsPattern);
    }

    /**
     * Tell if a specific file is a Zip file.
     */
    public static boolean isZipFile(String fileName) {
        return isSpecificFile(fileName, zipFileExtsPattern);
    }

    public static boolean isDCTFile(File file) {
        return isDCTFile(file.getName());
    }

    public static boolean isZipFile(File file) {
        return isZipFile(file.getName());
    }

    public static boolean isMDCFile(File file) {
        return isMDCFile(file.getName());
    }

    public static boolean isPropFile(File file) {
        return isSpecificFile(file.getName(), ".properties");
    }

    public static boolean isXmlFile(File file) {
        return isSpecificFile(file.getName(), ".xml");
    }

    private static boolean isSpecificFile(String fileName, String fileExtPattern) {
        Pattern pattern = Pattern.compile(fileExtPattern,
                Pattern.CASE_INSENSITIVE);
        int dotPos = fileName.lastIndexOf('.');
        String fileExt = fileName.substring(dotPos);
        return pattern.matcher(fileExt).matches();
    }

    public static String jsonFormat(String uglyJSONString) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(uglyJSONString);
        String prettyJsonString = gson.toJson(je);
        return prettyJsonString;
    }

    public static void unzip(File zip, String unzipFilePath) throws Exception {
        if (!zip.exists()) return;
        ZipFile zipFile = new ZipFile(zip);

        Enumeration<? extends ZipEntry> enumZip = zipFile.entries();
        File rpath = new File(unzipFilePath);
        if (!rpath.exists() || !rpath.isDirectory()) {
            rpath.mkdirs();
        }
        while (enumZip.hasMoreElements()) {
            ZipEntry entry = enumZip.nextElement();
            String name = entry.getName();
            File dFile = new File(unzipFilePath, name);
            if (entry.isDirectory()) {
                dFile.mkdirs();
            } else {
                InputStream is = zipFile.getInputStream(entry);
                FileUtils.writeByteArrayToFile(dFile, IOUtils.toByteArray(is));
            }
        }
        zipFile.close();
    }

    public static void unzip(String zipFilePath, String unzipFilePath) throws Exception {
        File zip = new File(zipFilePath);
        if (!zip.exists()) return;
        unzip(zip, unzipFilePath);
    }

    /**
     * @param file the file to be add to zos
     */
    private static void writeZip(File file, String parentPath, ZipOutputStream zos) throws IOException {
        if (!file.exists()) return;
        if (file.isFile()) {
            zos.putNextEntry(new ZipEntry(parentPath + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] buf = new byte[1024];
            int len;
            while (-1 != (len = bis.read(buf))) {
                zos.write(buf, 0, len);
                zos.flush();
            }
            bis.close();
        } else {
            File[] files = file.listFiles();
            parentPath += file.getName() + File.separator;
            for (File f : files) {
                writeZip(f, parentPath, zos);
            }
        }
    }

    public static void createZip(File src, File zipFile) throws IOException {
        if (!zipFile.exists()) zipFile.createNewFile();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        writeZip(src, "", zos);
        zos.close();
    }

    public static void createZip(String srcPath, String zipPath) throws IOException {
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) return;
        createZip(srcFile, new File(zipPath));
    }
    
    public static boolean validateFileCharset(File file, String charsetName) {
    	boolean result = true;
		char[] buf = new char[65536];
		Charset charset = Charset.forName(charsetName);
		CharsetDecoder decoder = charset.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPORT);
		decoder.onUnmappableCharacter(CodingErrorAction.REPORT);

		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(file), decoder);
			int chars;
			while ((chars = reader.read(buf)) >= 0);
		} catch (CharacterCodingException ex) {
			ex.printStackTrace();
			result = false;
		} catch (IOException ex) {
			result = false;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException ex) {
					// dummy
				}
		}
		return result;

    }
    
    public static boolean isASCII(File file) {
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(file), "ISO-8859-1");
			int chars;
			char[] buf = new char[65536];
			while ((chars = reader.read(buf)) >= 0) {
				for (int i = 0; i < chars; i++) {
					if (buf[i] < 0x20 || buf[i] > 0x7e)
						return false;
				}
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			throw new SystemError(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException ex) {
					// dummy
				}
		}
    }
}
