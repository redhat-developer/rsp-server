/******************************************************************************* 
 * Copyright (c) 2010 - 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.test.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class IOUtil {
	public static byte[] getBytesFromInputStream(InputStream is) {
		if( is == null )
			return null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			int nRead;
			byte[] data = new byte[16384];
	
			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}
	
			buffer.flush();
			return buffer.toByteArray();
		} catch(IOException ioe) {
			return null;
		}
    }      


	public static void setContents(File file, String contents) throws IOException {
		byte[] buffer = new byte[65536];
		InputStream in = new ByteArrayInputStream(contents.getBytes());
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			int avail = in.read(buffer);
			while (avail > 0) {
				out.write(buffer, 0, avail);
				avail = in.read(buffer);
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static String getContents(File aFile) throws IOException {
		return new String(getBytesFromFile(aFile));
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        is.close();
        return bytes;
    }
	
	public static int countFiles(File root) {
		int count = 0;
		if( !root.isDirectory() )
			return 1;
		File[] children = root.listFiles();
		for( int i = 0; i < children.length; i++ ) 
			count += countFiles(children[i]);
		return count;
	}
	public static int countAllResources(File root) {
		int count = 0;
		if( !root.isDirectory() )
			return 1;
		File[] children = root.listFiles();
		for( int i = 0; i < children.length; i++ ) 
			count += countAllResources(children[i]);
		return 1 + count;
	}

	public static void unzipFile(File zipped, File toLoc) {
		toLoc.mkdirs();
		final int BUFFER = 2048;
		try {
			  BufferedOutputStream dest = null;
		      FileInputStream fis = new FileInputStream(zipped);
			  ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
	          ZipEntry entry;
	          while((entry = zis.getNextEntry()) != null) {
	             int count;
	             byte data[] = new byte[BUFFER];
	             // write the files to the disk
            	 String eName = entry.getName();
	             if( entry.isDirectory() ) {
	            	 File f = new File(toLoc, eName);
	            	 f.mkdirs();
	             } else {
	            	 File f = new File(toLoc, eName);
		             f.getParentFile().mkdirs();
		             if( !f.exists()) {
		            	 String out = f.getAbsolutePath();
			             FileOutputStream fos = new FileOutputStream(out);
			             dest = new BufferedOutputStream(fos, BUFFER);
			             while ((count = zis.read(data, 0, BUFFER)) != -1) {
			                dest.write(data, 0, count);
			             }
			             dest.flush();
			             dest.close();
		             }
	             }
	          }
	          zis.close();
	       } catch(Exception e) {
	          e.printStackTrace();
	       }
	}

	
	public static boolean isZip(File file) {
	      if(file.isDirectory()) {
	          return false;
	      }
	      if(!file.canRead()) {
	    	  return false;
	      }
	      if(file.length() < 4) {
	          return false;
	      }
	      try {
		      DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		      int test = in.readInt();
		      in.close();
		      return test == 0x504b0304;
	      }catch( IOException ioe) {
	    	  return false;
	      }
	}
	

	public static void copyFolder(File src, File dest)
			throws IOException{

		if(src.isDirectory()){

			//if directory not exists, create it
			if(!dest.exists()){
				dest.mkdirs();
			}

			//list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				//construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				//recursive copy
				copyFolder(srcFile,destFile);
			}

		}else{
			//if file, then copy it
			//Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest); 

			byte[] buffer = new byte[1024];

			int length;
			//copy the file content in bytes 
			while ((length = in.read(buffer)) > 0){
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}
	
	

	public static interface IFileUtilListener {
		public void fileDeleted(File file, boolean result, Exception e);
		public void folderDeleted(File file, boolean result, Exception e);
		public void fileCopied(File source, File dest, boolean result, Exception e);
	}
	
	
	// Delete the file. If it's a folder, delete all children.
	public static void safeDelete(File file) {
		safeDelete(file, null);
	}
	public static void safeDelete(File file, IFileUtilListener listener) {
		if( file.isDirectory() ) {
			File[] children = file.listFiles();
			if( children != null ) {
				for( int i = 0; i < children.length; i++ ) {
					safeDelete(children[i], listener);
				}
			}
			
			if( file.exists()) {
				try {
					boolean tmp = file.delete();
					if( listener != null ) listener.folderDeleted(file, tmp, null);
				} catch( SecurityException sex) {
					if( listener != null ) listener.folderDeleted(file, false, sex);
				}
			}
		}
		
		// files only
		if( file.exists() ) {
			try {
				boolean tmp = file.delete();
				if( listener != null ) listener.fileDeleted(file, tmp, null);
			} catch( SecurityException sex) {
				if( listener != null ) listener.fileDeleted(file, false, sex);
			}
		}
	}
	
	// calls safedelete, but also deletes empty parent folders
	public static void completeDelete(File file) {
		completeDelete(file, null);
	}
	
	public static void completeDelete(File file, IFileUtilListener listener) {
		completeDelete(file, null, listener);
	}
	public static void completeDelete(File file, File archiveRoot, IFileUtilListener listener) {
		safeDelete(file, listener);
		//delete all empty parent folders
		while(!file.getParentFile().equals(archiveRoot) && file.getParentFile().listFiles().length == 0 ) {
			file = file.getParentFile();
			try {
				boolean tmp = file.delete();
				if( listener != null ) listener.folderDeleted(file, tmp, null);
			} catch( SecurityException sex ) {
				if( listener != null ) listener.folderDeleted(file, false, sex);
			}
		}
	}
	
	public static boolean fileSafeCopy(File src, File dest) {
		return fileSafeCopy(src, dest, null);
	}
	public static boolean fileSafeCopy(File src, File dest, IFileUtilListener listener) {
		File parent = dest.getParentFile();
		parent.mkdirs();
		if( dest.exists()) 
			safeDelete(dest);
		
		if (src.isDirectory()) {
			File[] subFiles = src.listFiles();
			boolean copied = true;
			dest.mkdirs();
			for (int i = 0; i < subFiles.length; i++) {
				File newDest = new File(dest, subFiles[i].getName());
				copied = copied && fileSafeCopy(subFiles[i], newDest, listener);
			}
			return copied;
		} else {
			try {
			    FileInputStream fis  = new FileInputStream(src);
			    FileOutputStream fos = new FileOutputStream(dest);
			    byte[] buf = new byte[1024];
			    int i = 0;
			    while((i=fis.read(buf))!=-1) {
			      fos.write(buf, 0, i);
			      }
			    fis.close();
			    fos.close();
			    if( listener != null ) listener.fileCopied(src, dest, true, null);
				return true;
			} catch( Exception e ) {
			    if( listener != null ) listener.fileCopied(src, dest, false, e);
				return false;
			}
		}
	}

	public static void touch(FileFilter filter, File root, boolean recurse) {
		if( filter.accept(root)) 
			root.setLastModified(new Date().getTime());
		if( recurse && root.isDirectory() ) {
			File[] children = root.listFiles();
			if( children != null ) {
				for( int i = 0; i < children.length; i++ ) {
					touch(filter, children[i], recurse);
				}
			}
		}
	}
	
    public static void copyDir(File from, File to) {
        copyDir(from, to, false);
    }

    public static void copyDir(File from, File to, boolean mkdirs) {
        copyDir(from, to, mkdirs, true);
    }

    public static void copyDir(File from, File to, boolean mkdirs, boolean includeSubdirs) {
        copyDir(from, to, includeSubdirs, mkdirs, false);
    }

    public static void copyDir(File from, boolean includeSubdirs, File to) {
        copyDir(from, to, includeSubdirs, false, false);
    }

    public static void copyDir(File from, File to, boolean includeSubdirs, boolean mkdirs, boolean overwriteOnlyOlderFiles) {
    	copyDir(from, to, includeSubdirs, mkdirs, overwriteOnlyOlderFiles, null);
    }

    public static void copyDir(File from, File to, boolean includeSubdirs, boolean mkdirs, boolean overwriteOnlyOlderFiles, FileFilter filter) {
        if(filter != null && !filter.accept(from)) return;
        if (mkdirs) to.mkdirs();
        if(from == null || !from.isDirectory() || !to.isDirectory()) return;
        File[] fs = from.listFiles();
        if(fs == null) return;
        for (int i = 0; i < fs.length; i++) {
            String n = fs[i].getName();
            File c = new File(to, n);
            if (fs[i].isDirectory() && !includeSubdirs) continue;
        	if(filter != null && !filter.accept(new File(from, n))) continue;

            if(fs[i].isDirectory()) {
                c.mkdirs();
                copyDir(fs[i], c, includeSubdirs, mkdirs, overwriteOnlyOlderFiles, filter);
            } else if (overwriteOnlyOlderFiles && fs[i].isFile() && c.isFile()) {
                copyFile(fs[i], c, false, c.lastModified() < fs[i].lastModified());
            } else {
                copyFile(fs[i], c);
            }
        }
    }
    public static boolean copyFile(File source, File dest, boolean mkdirs) {
        return copyFile(source, dest, mkdirs, true);
    }

    public static boolean copyFile(File source, File dest) {
        return copyFile(source, dest, false, true);
    }

    public static boolean copyFile(File source, File dest, boolean mkdirs, boolean overwrite) {
        if (mkdirs) dest.getParentFile().mkdirs();
        if(!source.isFile()) return false;
        if(dest.isFile() && !isSameFile(dest)) dest.delete();
        if(dest.isFile() && !overwrite) return false;
        if(!dest.exists())
			try {
				dest.createNewFile();
			} catch (IOException e1) {
				log(e1); 
			}
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(new FileInputStream(source), 16 * 1024);
            os = new BufferedOutputStream(new FileOutputStream(dest), 16 * 1024);
            copyStream(is, os);
            return true;
        } catch (IOException e) {
        	log(e);
            return false;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
            	log(e);
            }
            try {
                if (os != null) os.close();
            } catch (IOException e) {
            	log(e);
            }
        }
    }
    
    private static void log(Exception e) {
    	// TODO 
    }

    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1<<14];
        while (true) {
            int r = is.read(buffer);
            if (r > 0) {
                os.write(buffer, 0, r);
            } else if (r == -1) break;
        }
        os.flush();
    }
    public static boolean isSameFile(File f) {
        if(!f.exists()) return false;
        String fn = f.getName();
        try {
           String cn = f.getCanonicalFile().getName();
           return fn.equals(cn);
        } catch (IOException e) {
            return false;
        }
    }

    
    // Copied from internal class FileUtils.java
	private static final int BUFFER = 65536;

	public static void writeTo(InputStream in, String fileName) throws IOException {
		writeTo(in, new File(fileName));
	}

	public static void writeTo(InputStream in, File file) throws IOException {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			writeTo(in, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static void writeTo(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[BUFFER];
		int avail = in.read(buffer);
		while (avail > 0) {
			out.write(buffer, 0, avail);
			avail = in.read(buffer);
		}
	}

	
}
