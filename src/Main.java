//import org.junit.Test;
//import org.junit.Test;
import spoon.Launcher;
import spoon.processing.ProcessingManager;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import static org.junit.Assert.assertEquals;

//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertThat;

public class Main {
	public static void main(String[] args) {
		System.out.print("\nWelcome to the Null-Pointer Exception Checker!\nAll your files will be scanned for potential null-pointer exceptions. You're welcome.\n");
		final String[] settings = {
				"-i", "",
				"-o", "target",
				"-c" // It's mandatory here to enable comments
		};

		// our processor will do static analysis on all files under directory "src/resources"
		List<String> files = new ArrayList<>();
		getFilesForFolder(new File("src/resources"), files);
		HashMap<String, HashMap<String, VarStatus>> classVarMaps = new HashMap<>();
//		for(String filePath: files){
//			settings[1] = filePath;
//			classFileFillingClassVarMaps(settings, classVarMaps);
//		}

		for(String filePath: files){
			settings[1] = filePath;
			classFileStaticAnalyze(settings, classVarMaps);
		}
	}

	// fill in classVar for each class so their children can inherit fields later when doing static analysis
	private static void classFileFillingClassVarMaps(String[] settings, HashMap<String, HashMap<String, VarStatus>> classVarMaps) {
		final Launcher launcher = new Launcher();
		launcher.setArgs(settings);
		launcher.run();

		final Factory factory = launcher.getFactory();
		final ProcessingManager processingManager = new QueueProcessingManager(factory);
		final ClassProcessorSetupForClassHierarchy processorNoPrintingErrors = new ClassProcessorSetupForClassHierarchy(classVarMaps);
		processingManager.addProcessor(processorNoPrintingErrors);
		processingManager.process(factory.Class().getAll());
	}

	private static void classFileStaticAnalyze(String[] settings, HashMap<String, HashMap<String, VarStatus>> classVarMaps) {
		final Launcher launcher = new Launcher();
		launcher.setArgs(settings);
		launcher.run();

		final Factory factory = launcher.getFactory();
		final ProcessingManager processingManager = new QueueProcessingManager(factory);
		final ClassProcessorSetupForClassHierarchy processorNoPrintingErrors = new ClassProcessorSetupForClassHierarchy(classVarMaps);
		final ClassProcessor processor = new ClassProcessor(classVarMaps);
		processingManager.addProcessor(processor);
		processingManager.process(factory.Class().getAll());
	}

	public static List<String> getFilesForFolder(File folder, List<String> files) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				getFilesForFolder(fileEntry, files);
			} else {
				files.add(fileEntry.getPath());
			}
		}
		return files;
	}



	//	@Test
//	public void testMethodProcessor() throws Exception {
//		final String[] args = {
//				"-i", "src/resources/InputJavaFile.java",
//				"-o", "target",
//				"-c" // It's mandatory here to enable comments
//		};
//
//		final Launcher launcher = new Launcher();
//		launcher.setArgs(args);
//		launcher.run();
//
//		final Factory factory = launcher.getFactory();
//		final ProcessingManager processingManager = new QueueProcessingManager(factory);
//		final MethodProcessor processor = new MethodProcessor();
//		processingManager.addProcessor(processor);
//		processingManager.process(factory.Class().getAll());
//
//		// implicit constructor is also counted
//		assertThat(processor.undocumentedElements.size(), is(7));
//	}
//
//	@Test
//	public void testClassProcessor() throws Exception{
//		final String[] args = {
//				"-i", "src/resources/InputJavaFile.java",
//				"-o", "target",
//				"-c" // It's mandatory here to enable comments
//		};
//
//		final Launcher launcher = new Launcher();
//		launcher.setArgs(args);
//		launcher.run();
//
//		final Factory factory = launcher.getFactory();
//		final ProcessingManager processingManager = new QueueProcessingManager(factory);
//		final ClassProcessor processor = new ClassProcessor();
//		processingManager.addProcessor(processor);
//		processingManager.process(factory.Class().getAll());
//
//	}

//	@Test
//	public void testMergeHashMap(){
//		ClassProcessor classProcessor = new ClassProcessor();
//		HashMap<String, VarStatus> map1 = new HashMap<>();
//		HashMap<String, VarStatus> map2 = new HashMap<>();
//
//		VarStatus varStatusA1 = new VarStatus("a", true, Arrays.asList(1,2));
//		VarStatus varStatusA2 = new VarStatus("a", null, Arrays.asList(1,2,3));
//
//		map1.put("a", varStatusA1);
//		map2.put("a", varStatusA2);
//
//		HashMap<String, VarStatus> expectedMap = new HashMap<>();
//		expectedMap.put("a", new VarStatus("a", true, Arrays.asList(1,2)));
//
////		assertEquals(classProcessor.mergeHashMaps(map1, map2), expectedMap);
//		HashMap<String, VarStatus> res = classProcessor.mergeHashMaps(map1, map2);
//
//		System.out.println(res.get("a").getCanBeNull());
//		System.out.println(classProcessor.mergeHashMaps(map1, map2).get("a"));
//	}

}
