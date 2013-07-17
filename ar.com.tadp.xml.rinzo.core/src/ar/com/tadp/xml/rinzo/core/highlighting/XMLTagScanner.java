/*****************************************************************************
 * This file is part of Rinzo
 *
 * Author: Claudio Cancinos
 * WWW: https://sourceforge.net/projects/editorxml
 * Copyright (C): 2008, Claudio Cancinos
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; If not, see <http://www.gnu.org/licenses/>
 ****************************************************************************/
package ar.com.tadp.xml.rinzo.core.highlighting;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

/**
 * 
 * @author ccancinos
 */
public class XMLTagScanner extends RuleBasedScanner {
    private IToken tokenTag;
    private IToken tokenString;
    private IToken tokenAttribute;
    private IToken tokenDeclaration;
    private IToken tokenProcInst;

    public XMLTagScanner(ColorManager manager) {
        tokenTag = null;
        tokenString = null;
        tokenAttribute = null;
        tokenDeclaration = null;
        tokenProcInst = null;
        tokenTag = new Token(new TextAttribute(manager.getColor(IXMLColorConstants.TAG), null, manager.isBold(IXMLColorConstants.TAG)));
        tokenString = new Token(new TextAttribute(manager.getColor(IXMLColorConstants.STRING), null, manager.isBold(IXMLColorConstants.STRING)));
        tokenAttribute = new Token(new TextAttribute(manager.getColor(IXMLColorConstants.ATTRIBUTE), null, manager.isBold(IXMLColorConstants.ATTRIBUTE)));
        tokenDeclaration = new Token(new TextAttribute(manager.getColor(IXMLColorConstants.DECLARATION), null, manager.isBold(IXMLColorConstants.DECLARATION)));
        tokenProcInst = new Token(new TextAttribute(manager.getColor(IXMLColorConstants.PROC_INSTR), null, manager.isBold(IXMLColorConstants.PROC_INSTR)));
        IRule rules[] = new IRule[4];
        rules[0] = new SingleLineRule("\"", "\"", tokenString, '\\');
        rules[1] = new SingleLineRule("'", "'", tokenString, '\\');
        rules[2] = new WhitespaceRule(new XMLWhitespaceDetector());
        rules[3] = new IRule() {

            private static final int STATE_UNDEFINED = 0;
            private static final int STATE_TAGSTART = 1;
            private static final int STATE_TAGEND = 2;
            private static final int STATE_ATTRIBUTE = 3;
            private int state;

            private void switchState(int newState) {
                state = newState;
            }

            public IToken getSuccessToken() {
                switch(state) {
                case STATE_TAGSTART: // '\001'
                    return tokenTag;
                case STATE_TAGEND: // '\002'
                    return tokenTag;
                case STATE_ATTRIBUTE: // '\003'
                    return tokenAttribute;
                }
                return Token.EOF;
            }

            public IToken evaluate(ICharacterScanner scanner) {
				boolean done = false;
				switchState(0);
				int previous = -1;
				int character = -1;
				while (!done) {
					previous = character;
					character = scanner.read();
					if (character == -1) {
						done = true;
					} else {
						switch (state) {
						case STATE_UNDEFINED: // '\0'
							done = handleUndefined(scanner, character, previous);
							break;
						case STATE_TAGSTART: // '\001'
							done = handleTagStart(scanner, character, previous);
							break;
						case STATE_TAGEND: // '\002'
							done = handleTagEnd(scanner, character, previous);
							break;
						case STATE_ATTRIBUTE: // '\003'
							done = handleAttribute(scanner, character, previous);
							break;
						}
					}
				}
				return getSuccessToken();
            }

            private boolean handleTagEnd(ICharacterScanner scanner, int character, int previous) {
                return character == 62;
            }

            private boolean handleAttribute(ICharacterScanner scanner, int character, int previous) {
				if (character == 61 || character == 32 || character == 47 || character == 62) {
					scanner.unread();
					return true;
				} else {
					return false;
				}
            }

            private boolean handleTagStart(ICharacterScanner scanner, int character, int previous) {
				if (character == 32 || character == 34 || character == 39 || character == 62) {
					scanner.unread();
					return true;
				} else {
					return false;
				}
            }

            private boolean handleUndefined(ICharacterScanner scanner, int character, int previous) {
				if (character == 60) {
					switchState(STATE_TAGSTART);
				} else if (character == 47 || character == 62) {
					switchState(STATE_TAGEND);
				} else {
					switchState(STATE_ATTRIBUTE);
				}
				return false;
            }

        };
        setRules(rules);
    }

}
