import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static spoon.reflect.declaration.ModifierKind.PROTECTED;
import static spoon.reflect.declaration.ModifierKind.PUBLIC;

/**
 * Reports warnings for undocumented elements
 */
public class MethodProcessor extends AbstractProcessor<CtElement> {
	// used in an assertion
	public final List<CtElement> undocumentedElements = new ArrayList<>();
	public CtElement ctClass;

	public void process(CtElement element) {
		// visit every node and add method node without comments to undocumentedElements
		if (element instanceof CtType || element instanceof CtField || element instanceof CtExecutable) {
			Set<ModifierKind> modifiers = ((CtModifiable) element).getModifiers();
			if (modifiers.contains(PUBLIC) || modifiers.contains(PROTECTED)) {
				String docComment = element.getDocComment();
				if (docComment == null || docComment.equals("")) {
					System.out.println("undocumented element at " + element.getPosition());
					undocumentedElements.add(element);
				}
			}
		}

		if(element instanceof CtClass){
			ctClass = element;
		}
	}

	// printing out all nodes. Call this within process method
	//		int n = 0;
//		CtElement parent = element.getParent();
//		while (parent != null) {
//			n++;
//			parent = parent.getParent();
//		}
//
//		// Print the element
//		try {
//			String s = "";
//			if (n > 0) s = String.format("%0" + n + "d", 0).replace("0","-");
//			System.out.println(s + element.getClass().getSimpleName());
//		} catch (NullPointerException ex) {
//			System.out.println("Unknown Element");
//		}
//
//		if (element.getClass().getSimpleName().equals("CtJavaDocImpl")){
//			rootElement = element;
//		}

}
