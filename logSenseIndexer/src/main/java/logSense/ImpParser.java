package logSense;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class ImpParser extends BaseFieldParserImpl
{
	
	/*
	 * search for
	 * 
	 * SEVERE (all upper case)
	 *  <space>IMP... <space> -- all upper case letters or under score
	 *  
	 *  
	 * capture the text pieces that are matched.
	 * 
	 * multiple matches possible
	 *  
	 *  
	 */
	

	public ImpParser()
	{
		super(SolrField.IMP);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[2];
		
		// SEVERE 
		ownRegexStr[0] = "\\b(SEVERE)\\b";
			
		// space IMP<upper-case and under-score> space  \sIMP[A-Z_]+\s
		ownRegexStr[1] = "\\b(IMP[A-Z_]+)\\b";
		
		// init the Patterns for this class
		initOwnPatterns();
		
		return this;
		
	}
}
