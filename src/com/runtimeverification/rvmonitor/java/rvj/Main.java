/**
 * @author fengchen, Dongyun Jin, Patrick Meredith, Michael Ilseman
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package com.runtimeverification.rvmonitor.java.rvj;

import com.runtimeverification.rvmonitor.java.rvj.logicclient.LogicRepositoryConnector;
import com.runtimeverification.rvmonitor.java.rvj.output.CodeGenerationOption;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.RVMSpecFile;
import com.runtimeverification.rvmonitor.util.RVMException;
import com.runtimeverification.rvmonitor.util.Tool;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;

class JavaFileFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return name.endsWith(".java");
	}
}

class RVMFileFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return name.endsWith(Tool.getSpecFileDotExt());
	}
}

public class Main {

	static File outputDir = null;
	public static boolean debug = false;
	public static boolean noopt1 = false;
	public static boolean toJavaLib = false;
	public static boolean statistics = false;
	public static boolean statistics2 = false;
	public static String aspectname = null;
	public static boolean isJarFile = false;
	public static String jarFilePath = null;

	public static final int NONE = 0;
	public static final int HANDLERS = 1;
	public static final int EVENTS = 2;
	public static int logLevel = NONE;

	public static boolean dacapo = false;
	public static boolean dacapo2 = false;
	public static boolean silent = false;
	public static boolean empty_advicebody = false;

	public static boolean merge = false;
	public static boolean inline = false;

	public static boolean scalable = false;
	
	public static boolean internalBehaviorObserving = false;
	
	public static boolean useFineGrainedLock = false;
	public static boolean useWeakRefInterning = false;

	public static boolean generateVoidMethods = true;
	public static boolean stripUnusedParameterInMonitor = true;
	public static boolean eliminatePresumablyRemnantCode = true;
	public static boolean suppressActivator = false;
	public static boolean usePartitionedSet = true;

	static private File getTargetDir(ArrayList<File> specFiles) throws RVMException {
		if(Main.outputDir != null){
			return outputDir;
		}

		boolean sameDir = true;
		File parentFile = null;
		
		for(File file : specFiles){
			if(parentFile == null){
				parentFile = file.getAbsoluteFile().getParentFile();
			} else {
				if(file.getAbsoluteFile().getParentFile().equals(parentFile)){
					continue;
				} else {
					sameDir = false;
					break;
				}
			}
		}
		
		if(sameDir){
			return parentFile;
		} else {
			return new File(".");
		}
	}
	
	
	/**
	 * Process a java file including mop annotations to generate a runtime
	 * monitor file. The file argument should be an initialized file object.
	 * The location argument should contain the original file name, but it
	 * may have a different directory.
	 * 
	 * @param file
	 *            a File object containing the specification file
	 * @param location
	 *            an absolute path for result file
	 */
	public static void processJavaFile(File file, String location) throws RVMException {
		RVMNameSpace.init();
		String specStr = SpecExtractor.process(file);
		RVMSpecFile spec =  SpecExtractor.parse(specStr);
		
		RVMProcessor processor = new RVMProcessor(Main.aspectname == null ? Tool.getFileName(file.getAbsolutePath()) : Main.aspectname);

		String aspect = processor.process(spec);
		writeAspectFile(aspect, location);
	}

	/**
	 * Process a specification file to generate a runtime monitor file.
	 * The file argument should be an initialized file object. The location
	 * argument should contain the original file name, But it may have a
	 * different directory.
	 *
	 * @param file
	 *            a File object containing the specification file
	 * @param location
	 *            an absolute path for result file
	 */
	public static void processSpecFile(File file, String location) throws RVMException {
		RVMNameSpace.init();
		String specStr = SpecExtractor.process(file);
		RVMSpecFile spec =  SpecExtractor.parse(specStr);

		RVMProcessor processor = new RVMProcessor(Main.aspectname == null ? Tool.getFileName(file.getAbsolutePath()) : Main.aspectname);

		String output = processor.process(spec);

		if (toJavaLib) {
			writeJavaLibFile(output, location);
		} else {
			writeAspectFile(output, location);
		}
	}
	
	public static void processMultipleFiles(ArrayList<File> specFiles) throws RVMException {
		String aspectName;

		if(outputDir == null){
			outputDir = getTargetDir(specFiles);
		}

		if(Main.aspectname != null) {
			aspectName = Main.aspectname;
		} else {
			if(specFiles.size() == 1) {
				aspectName = Tool.getFileName(specFiles.get(0).getAbsolutePath());
			} else {
				int suffixNumber = 0;
				// generate auto name like 'MultiMonitorApsect.aj'
				
				File aspectFile;
				do{
					suffixNumber++;
					aspectFile = new File(outputDir.getAbsolutePath() + File.separator + "MultiSpec_" + suffixNumber + "RuntimeMonitor.java");
				} while(aspectFile.exists());
				
				aspectName = "MultiSpec_" + suffixNumber;
			}
		}
		
		RVMNameSpace.init();
		ArrayList<RVMSpecFile> specs = new ArrayList<RVMSpecFile>();
		for(File file : specFiles){
			String specStr = SpecExtractor.process(file);
			RVMSpecFile spec =  SpecExtractor.parse(specStr);
			
			specs.add(spec);
		}
		RVMSpecFile combinedSpec = SpecCombiner.process(specs);
		
		RVMProcessor processor = new RVMProcessor(aspectName);
		String output = processor.process(combinedSpec);
		
		writeCombinedAspectFile(output, aspectName);
	}

	protected static void writeJavaFile(String javaContent, String location) throws RVMException {
		if ((javaContent == null) || (javaContent.length() == 0))
			throw new RVMException("Nothing to write as a java file");
		if (!Tool.isJavaFile(location))
			throw new RVMException(location + "should be a Java file!");

		try {
			FileWriter f = new FileWriter(location);
			f.write(javaContent);
			f.close();
		} catch (Exception e) {
			throw new RVMException(e.getMessage());
		}
	}
	
	protected static void writeCombinedAspectFile(String aspectContent, String aspectName) throws RVMException {
		if (aspectContent == null || aspectContent.length() == 0)
			return;

		try {
			FileWriter f = new FileWriter(outputDir.getAbsolutePath() + File.separator + aspectName + "RuntimeMonitor.java");
			f.write(aspectContent);
			f.close();
		} catch (Exception e) {
			throw new RVMException(e.getMessage());
		}
		System.out.println(" " + aspectName + "RuntimeMonitor.java is generated");
	}

	protected static void writeAspectFile(String aspectContent, String location) throws RVMException {
		if (aspectContent == null || aspectContent.length() == 0)
			return;

		int i = location.lastIndexOf(File.separator);
		try {
			FileWriter f = new FileWriter(location.substring(0, i + 1) + Tool.getFileName(location) + "RuntimeMonitor.java");
			f.write(aspectContent);
			f.close();
		} catch (Exception e) {
			throw new RVMException(e.getMessage());
		}
		System.out.println(" " + Tool.getFileName(location) + "RuntimeMonitor.java is generated");
	}

	protected static void writeJavaLibFile(String javaLibContent, String location) throws RVMException {
		if (javaLibContent == null || javaLibContent.length() == 0)
			return;

		int i = location.lastIndexOf(File.separator);
		try {
			FileWriter f = new FileWriter(location.substring(0, i + 1) + Tool.getFileName(location) + "JavaLibMonitor.java");
			f.write(javaLibContent);
			f.close();
		} catch (Exception e) {
			throw new RVMException(e.getMessage());
		}
		System.out.println(" " + Tool.getFileName(location) + "JavaLibMonitor.java is generated");
	}

	// PM
	protected static void writePluginOutputFile(String pluginOutput, String location) throws RVMException {
		int i = location.lastIndexOf(File.separator);

		try {
			FileWriter f = new FileWriter(location.substring(0, i + 1) + Tool.getFileName(location) + "PluginOutput.txt");
			f.write(pluginOutput);
			f.close();
		} catch (Exception e) {
			throw new RVMException(e.getMessage());
		}
		System.out.println(" " + Tool.getFileName(location) + "PluginOutput.txt is generated");
	}

	public static String polishPath(String path) {
		if (path.indexOf("%20") > 0)
			path = path.replaceAll("%20", " ");

		return path;
	}

	public static ArrayList<File> collectFiles(String[] files, String path) throws RVMException {
		ArrayList<File> ret = new ArrayList<File>();

		for (String file : files) {
			String fPath = path.length() == 0 ? file : path + File.separator + file;
			File f = new File(fPath);

			if (!f.exists()) {
				throw new RVMException("[Error] Target file, " + file + ", doesn't exsit!");
			} else if (f.isDirectory()) {
				ret.addAll(collectFiles(f.list(new JavaFileFilter()), f.getAbsolutePath()));
				ret.addAll(collectFiles(f.list(new RVMFileFilter()), f.getAbsolutePath()));
			} else {
				if (Tool.isSpecFile(file)) {
					ret.add(f);
				} else if (Tool.isJavaFile(file)) {
					ret.add(f);
				} else
					throw new RVMException("Unrecognized file type! The RV " +
							"Monitor specification file should have .rvm as" +
							" the extension.");
			}
		}

		return ret;
	}

	public static void process(String[] files, String path) throws RVMException {
		ArrayList<File> specFiles = collectFiles(files, path);
		
		if(Main.aspectname != null && files.length > 1){
			Main.merge = true;
		}
		
		if (Main.merge) {
			System.out.println("-Processing " + specFiles.size() + " specification(s)");
			processMultipleFiles(specFiles);
		} else {
			for (File file : specFiles) {
				String location = outputDir == null ? file.getAbsolutePath() : outputDir.getAbsolutePath() + File.separator + file.getName();

				System.out.println("-Processing " + file.getPath());
				if (Tool.isSpecFile(file.getName())) {
					processSpecFile(file, location);
				} else if (Tool.isJavaFile(file.getName())) {
					processJavaFile(file, location);
				}
			}
		}
	}

	public static void process(String arg) throws RVMException {
		if(outputDir != null && !outputDir.exists())
			throw new RVMException("The output directory, " + outputDir.getPath() + " does not exist.");
		
		process(arg.split(";"), "");
	}

	// PM
	public static void print_help() {
		System.out.println("Usage: java [-cp rv_monitor_classpath] com.runtimeverification.rvmonitor.java.rvj.Main [-options] files");
		System.out.println("");
		System.out.println("where options include:");
		System.out.println(" Options enabled by default are prefixed with \'+\'");
		System.out.println("    -h -help\t\t\t  print this help message");
		System.out.println("    -v | -verbose\t\t  enable verbose output");
		System.out.println("    -debug\t\t\t  enable verbose error message");
		System.out.println();

		System.out.println("    -local\t\t\t+ use local logic engine");
		System.out.println("    -remote\t\t\t  use default remote logic engine");
		System.out.println("\t\t\t\t  " + Configuration.getServerAddr());
		System.out.println("\t\t\t\t  (You can change the default address");
		System.out.println("\t\t\t\t   in com/runtimeverification/rvmonitor/java/rvj/config/remote_server_addr.properties)");
		System.out.println("    -remote:<server address>\t  use remote logic engine");
		System.out.println();

		System.out.println("    -d <output path>\t\t  select directory to store output files");
		System.out.println("    -n | -aspectname <aspect name>\t  use the given aspect name instead of source code name");
		System.out.println();

		System.out.println("    -showevents\t\t\t  show every event/handler occurrence");
		System.out.println("    -showhandlers\t\t\t  show every handler occurrence");
		System.out.println();

		System.out.println("    -s | -statistics\t\t  generate monitor with statistics");
		System.out.println("    -noopt1\t\t\t  don't use the enable set optimization");
		System.out.println("    -javalib\t\t\t  generate a java library rather than an AspectJ file");
		System.out.println();
		
		System.out.println("    -finegrainedlock\t\t  use fine-grained lock for internal data structure");
		System.out.println("    -weakrefinterning\t\t  use WeakReference interning in indexing trees");
		System.out.println();

		System.out.println("    -aspect:\"<command line>\"\t  compile the result right after it is generated");
		System.out.println();
	}

	public static void main(String[] args) {
		ClassLoader loader = Main.class.getClassLoader();
		String mainClassPath = loader.getResource("com/runtimeverification/rvmonitor/java/rvj/Main.class").toString();
		if (mainClassPath.endsWith(".jar!/com/runtimeverification/rvmonitor/java/rvj/Main.class") && mainClassPath.startsWith("jar:")) {
			isJarFile = true;

			jarFilePath = mainClassPath.substring("jar:file:".length(), mainClassPath.length() - "!/com/runtimeverification/rvmonitor/java/rvj/Main.class".length());
			jarFilePath = polishPath(jarFilePath);
		}

		int i = 0;
		String files = "";

		while (i < args.length) {
			if (args[i].compareTo("-h") == 0 || args[i].compareTo("-help") == 0) {
				print_help();
				return;
			}

			if (args[i].compareTo("-d") == 0) {
				i++;
				outputDir = new File(args[i]);
			} else if (args[i].compareTo("-local") == 0) {
				LogicRepositoryConnector.serverName = "local";
			} else if (args[i].compareTo("-remote") == 0) {
				LogicRepositoryConnector.serverName = "default";
			} else if (args[i].startsWith("-remote:")) {
				LogicRepositoryConnector.serverName = args[i].substring(8);
			} else if (args[i].compareTo("-v") == 0 || args[i].compareTo("-verbose") == 0) {
				LogicRepositoryConnector.verbose = true;
				RVMProcessor.verbose = true;
			} else if (args[i].compareTo("-javalib") == 0) {
				toJavaLib = true;
			} else if (args[i].compareTo("-debug") == 0) {
				Main.debug = true;
			} else if (args[i].compareTo("-noopt1") == 0) {
				Main.noopt1 = true;
			} else if (args[i].compareTo("-s") == 0 || args[i].compareTo("-statistics") == 0) {
				Main.statistics = true;
			} else if (args[i].compareTo("-s2") == 0 || args[i].compareTo("-statistics2") == 0) {
				Main.statistics2 = true;
			} else if (args[i].compareTo("-n") == 0 || args[i].compareTo("-aspectname") == 0) {
				i++;
				Main.aspectname = args[i];
			} else if (args[i].compareTo("-showhandlers") == 0) {
				if (Main.logLevel < Main.HANDLERS)
					Main.logLevel = Main.HANDLERS;
			} else if (args[i].compareTo("-showevents") == 0) {
				if (Main.logLevel < Main.EVENTS)
					Main.logLevel = Main.EVENTS;
			} else if (args[i].compareTo("-dacapo") == 0) {
				Main.dacapo = true;
			} else if (args[i].compareTo("-dacapo2") == 0) {
				Main.dacapo2 = true;
			} else if (args[i].compareTo("-silent") == 0) {
				Main.silent = true;
			} else if (args[i].compareTo("-merge") == 0) {
				Main.merge = true;
			} else if (args[i].compareTo("-inline") == 0) {
				Main.inline = true;
			} else if (args[i].compareTo("-noadvicebody") == 0) {
				Main.empty_advicebody = true;
			} else if (args[i].compareTo("-scalable") == 0) {
				Main.scalable = true;
			} else if (args[i].compareTo("-internalbehavior") == 0) {
				Main.internalBehaviorObserving = true;
			} else if (args[i].compareTo("-finegrainedlock") == 0) {
				Main.useFineGrainedLock = true;
			} else if (args[i].compareTo("-nofinegrainedlock") == 0) {
				Main.useFineGrainedLock = false;
			} else if (args[i].compareTo("-weakrefinterning") == 0) {
				Main.useWeakRefInterning = true;
			} else if (args[i].compareTo("-noweakrefinterning") == 0) {
				Main.useWeakRefInterning = false;
			} else if (args[i].compareTo("-partitionedset") == 0) {
				Main.usePartitionedSet = true;
			} else if (args[i].compareTo("-nopartitionedset") == 0) {
				Main.usePartitionedSet = false;
			} else {
				if (files.length() != 0)
					files += ";";
				files += args[i];
			}
			++i;
		}

		if (files.length() == 0) {
			print_help();
			return;
		}
		
		CodeGenerationOption.initialize();
		
		try {
			process(files);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			if (Main.debug)
				e.printStackTrace();
		}
	}
}
