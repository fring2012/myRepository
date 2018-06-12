package com.abupdate.mqtt_libs.mqttv3.persist;

import java.io.File;
import java.io.FilenameFilter;

public class PersistanceFileNameFilter implements FilenameFilter{
	
	private final String fileExtension;
	
	public PersistanceFileNameFilter(String fileExtension){
		this.fileExtension = fileExtension;
	}

	@Override
    public boolean accept(File dir, String name) {
		return name.endsWith(fileExtension);
	}

}
