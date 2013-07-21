package ar.com.tadp.xml.rinzo.core.highlighting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * 
 * @author serranoc
 */
public class SingleCharacterWordDetector implements IWordDetector {

	private List<Character> _chars = new ArrayList<Character>();
    
    public void addChar(char c) {
        _chars.add(c);
    }
     
    public boolean isWordPart(char c) {
        return false;
    }
 
    public boolean isWordStart(char c) {
        return _chars.contains(c);
    }

}
