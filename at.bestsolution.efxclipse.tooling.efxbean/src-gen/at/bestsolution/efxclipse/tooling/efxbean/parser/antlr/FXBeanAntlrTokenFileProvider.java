/*
* generated by Xtext
*/
package at.bestsolution.efxclipse.tooling.efxbean.parser.antlr;

import java.io.InputStream;
import org.eclipse.xtext.parser.antlr.IAntlrTokenFileProvider;

public class FXBeanAntlrTokenFileProvider implements IAntlrTokenFileProvider {
	
	public InputStream getAntlrTokenFile() {
		ClassLoader classLoader = getClass().getClassLoader();
    	return classLoader.getResourceAsStream("at/bestsolution/efxclipse/tooling/efxbean/parser/antlr/internal/InternalFXBean.tokens");
	}
}
