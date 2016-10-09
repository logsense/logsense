/**
 * 
 */
package logSense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author am
 *
 */
public class ParserResult 
{
	List<NameValue> nvPairs;
	
	// given an input record, we found a value that matches an field-name. rest of the String
	// is in remainder
	String remainder;
	
	/*
	 * in some cases, we may want to append some strings to the original "record" before indexing. e.g., for backend server logs, if 
	 * we discovered an app-server SessionId that is not in the log line, but a mapping is known between backend session and tomcat sessionID (from a
	 * previously matched record
	 */
	List<String> discovered;
	
	/*
	 * we may want to use a key to route stuff to a specific shard -- e.g., if we have to join on that. join does not work across shards
	 */
	String shardRouteKey;
	
	boolean isParent; // if true requestId will go to parentId. else, requestId will to to childId
	String requestId;
	
	/*
	 * a string that marks an exception uniquely. at the point where this is included, it's
	 * only a potential exception class. 
	 */
	String potentialExceptionClass;
	
	Boolean hasDate; // used in some logs
	
	public static class NameValue
	{
		String name;
		String value;
		
		public NameValue(String name, String value)
		{
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
	}
	
	public boolean isEmpty()
	{
		if (CommonUtil.o.isEmpty(nvPairs) && CommonUtil.o.isEmpty(remainder))
			return true;
		
		else
			return false;
	}
	
	public boolean hasMatch()
	{
		return !CommonUtil.o.isEmpty(nvPairs) ;
	}
	
	/**
	 * @param fieldName
	 * @param matchedValue
	 */
	public void add(String fieldName, String matchedValue)
	{
		if (nvPairs == null)
			nvPairs = new ArrayList<NameValue>();
		
		nvPairs.add(new NameValue(fieldName, matchedValue));
	}
	
	public void addAll(Collection<NameValue> values)
	{
		if (nvPairs == null)
			nvPairs = new ArrayList<NameValue>();
		nvPairs.addAll(values);
	}
	
	public void addDiscovered(String newString)
	{
		if (discovered == null)
			discovered = new ArrayList<String>();
		discovered.add(newString);
	}
	
	public void addAllDiscovered(List<String> newStrings)
	{
		if (CommonUtil.o.isEmpty(newStrings))	
			return;
		
		if (discovered == null)
			discovered = new ArrayList<String>();
		discovered.addAll(newStrings);
	}
	
	public List<String> getDiscovered()
	{
		return discovered;
	}
	
	/*
	 * in some cases such as exceptions, need to suppress duplicates
	 */
	public void addIfUniqueValue(String fieldName, String matchedValue)
	{
		if (nvPairs == null)
			nvPairs = new ArrayList<NameValue>();
		
		boolean found = false;
		for (NameValue nv : nvPairs)
		{
			if (nv.value.equalsIgnoreCase(matchedValue))
			{
				found = true;
				break;
			}
				
		}
		if (!found)
			nvPairs.add(new NameValue(fieldName, matchedValue));
		
	}

	public List<NameValue> getNvPairs() {
		return nvPairs;
	}

	public String getRemainder() {
		return remainder;
	}

	public String getShardRouteKey() {
		return shardRouteKey;
	}

	public void setShardRouteKey(String shardRouteKey) {
		this.shardRouteKey = shardRouteKey;
	}

	public void setRemainder(String remainder) {
		this.remainder = remainder;
	}

	public String getPotentialExceptionClass() {
		return potentialExceptionClass;
	}

	public void setPotentialExceptionClass(String potentialExceptionClass) {
		this.potentialExceptionClass = potentialExceptionClass;
	}

	public Boolean getHasDate() {
		return hasDate;
	}

	public void setHasDate(Boolean hasDate) {
		this.hasDate = hasDate;
	}

}
