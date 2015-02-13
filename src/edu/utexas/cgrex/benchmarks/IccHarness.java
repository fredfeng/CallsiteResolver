package edu.utexas.cgrex.benchmarks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import soot.CompilationDeathException;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.queue.QueueReader;
import edu.utexas.cgrex.QueryManager;
import edu.utexas.cgrex.android.SetupApplication;

/**
 * The harness for sensitive APIs in Android.
 * @author yufeng
 *
 */
public class IccHarness {

	public static int benchmarkSize = 10;

	// we will collect the running time at each interval.
	public static int interval = 5;

	// 0: interactive mode; 1: benchmark mode
	public static int mode = 1;

	public static final Set<String> permissionAPIs = new HashSet<String>();

	public static final Set<String> apks = new HashSet<String>();

	public static final String sdk = "/home/yufeng/research/others/android-platforms/";

	public static final String defaultLoc = "/home/yufeng/research/benchmarks/malware/";

	public static String tmp = "/home/yufeng/research/benchmarks/malware/fse/DroidKungFu3/2cd9e65ec531feca6272c06e3552d0903be3e100.apk";

	public static final String apiMapping = "scripts/jellybean_mapping.txt";

	public static final String dummyMain = "<dummyMainClass: void dummyMainMethod()>";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException,
			CompilationDeathException {
		// recursively get all the apk files.
		findFiles(new File(defaultLoc));
		File fin = new File(apiMapping);
		FileInputStream fis = new FileInputStream(fin);
		// Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		while ((line = br.readLine()) != null) {
			int endIdx = line.indexOf(">");
			permissionAPIs.add(line.substring(0, endIdx + 1));
		}
		br.close();
//		for (String apk : apks) {
			runAnalysis(tmp, sdk);
//		}
	}

	public static void findFiles(File root) {
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(".apk")) {
				apks.add(file.getAbsolutePath());
			} else if (file.isDirectory()) {
				findFiles(file);
			}
		}
	}

	public static Set<Edge> getBs(Iterator it) {
		Set<Edge> result = new HashSet<Edge>();
		while (it.hasNext()) {
			result.add((Edge) it.next());
		}
		return result;
	}

	private static void runAnalysis(final String fileName,
			final String androidJar) {
		try {
			System.out.println("Analyzing app:" + fileName);
			SetupApplication app = new SetupApplication(androidJar, fileName);
			app.calculateEntryPoints();
			app.printEntrypoints();
			SootMethod main = Scene.v().getMethod(dummyMain);
			List<SootMethod> entries = new LinkedList<SootMethod>();
			entries.add(main);
			Scene.v().setEntryPoints(entries);
			QueryManager qm = new QueryManager(Scene.v().getCallGraph(), main);
			Set<String> querySet = new HashSet<String>();
			CFGToDotGraph cfgToDot = new CFGToDotGraph();
			QueueReader qe = qm.getReachableMethods().listener();
			CallGraph cg = Scene.v().getCallGraph();
			while (qe.hasNext()) {
				SootMethod meth = (SootMethod) qe.next();

				Set<Edge> inSet = getBs(cg.edgesInto(meth));
				Set<Edge> outSet = getBs(cg.edgesOutOf(meth));

				if (
//						inSet.size() > 1 && outSet.size() > 1
//						&& !meth.isConstructor()
						permissionAPIs.contains(meth.getSignature())) {
					for (Edge s : inSet) {
						SootMethod src = (SootMethod) s.getSrc();
						if (src.getSignature().equals(dummyMain))
							continue;

						for (Edge t : outSet) {
							SootMethod tgt = (SootMethod) t.getTgt();
							String query = dummyMain + ".*"
									+ src.getSignature() + ".*"
									+ tgt.getSignature();
							querySet.add(query);
						}
					}
				}

			}
			
			assert false : querySet.size();

			for (String q : querySet) {
				String regx = qm.getValidExprBySig(q);
				regx = regx.replaceAll("\\s+", "");
				boolean res1 = qm.queryRegx(regx);
			}

			for (String tgt : permissionAPIs) {
				if (!qm.isReachable(tgt))
					continue;

				String regx = dummyMain + ".*" + tgt;
				regx = qm.getValidExprBySig(regx);
				regx = regx.replaceAll("\\s+", "");

				System.out.println("App: " + fileName + " Random regx------"
						+ regx);
				boolean res1 = qm.queryRegx(regx);
				System.out.println("Query result:" + res1);
				if (!res1) {
					assert false : Scene.v().getMethod(tgt);
				}
			}

		} catch (IOException ex) {
			System.err.println("Could not read file: " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (XmlPullParserException ex) {
			System.err.println("Could not read Android manifest file: "
					+ ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

}
