package logSense;

import java.io.File;

public class FileParams
{
	// log file
	String fileName = null;
	
	File file = null; // log file with full file Name
	
	Boolean ssl = null;
	Integer machineNum = null;
	String dataCenter = null; 
	String env = null;
	
	String website = null;
	
	String backendServer = null;
	
	
	public static enum LogType { tomcat_wwwssl, tomcat_www, wsapi, avocado, backend1, rtcc, L2, atg, access_wwwssl, access_www, access_wsapi, access_avocado, access_atg, logmon_www};
	
	LogType logType; 
	
	public FileParams() {}
	
	public FileParams(Arguments arguments)
	{
		// use values from arguments only if directory is emtpy
		if (arguments != null && CommonUtil.o.isEmpty(arguments.directory))
		{
			this.fileName = arguments.fileName;
			this.ssl = arguments.ssl;
			this.machineNum = arguments.machineNum;
			this.dataCenter = arguments.dataCenter;
			this.env = arguments.env;
			this.logType = arguments.logType;
			
			if (this.logType == null)
				this.logType = LogType.tomcat_wwwssl; // default
			
			website = ssl != null && ssl.booleanValue() ? "wwwssl" : "na";
		}
		
		
	}
	
	// currently, only processing www and wwwssl logs
	public boolean isValid()
	{
		return true;
		/*
		if (website != null && (website.toLowerCase().startsWith("www") || website.toLowerCase().startsWith("wsapi")) )
		{
			return true;
		}
		else
			return false;
			
		*/
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Boolean getSsl() {
		return ssl;
	}

	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}

	public Integer getMachineNum() {
		return machineNum;
	}

	public void setMachineNum(Integer machineNum) {
		this.machineNum = machineNum;
	}

	public String getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(String dataCenter) {
		this.dataCenter = dataCenter;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getDalOrL2Server() {
		return backendServer;
	}

	public void setDalOrL2Server(String dalOrL2Server) {
		this.backendServer = dalOrL2Server;
	}

	public LogType getLogType() {
		return logType;
	}

	public void setLogType(LogType logType) {
		this.logType = logType;
	}

}
