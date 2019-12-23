package de.ecconia.mclauncher.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.download.DownloadInfo;

public class DownloadSection
{
	private final Map<String, DownloadInfo> downloads = new HashMap<>();
	
	public DownloadSection(JSONObject object)
	{
		for(Entry<String, Object> entry : object.getEntries().entrySet())
		{
			downloads.put(entry.getKey(), new DownloadInfo(JSONObject.asObject(entry.getValue())));
		}
	}
	
	public DownloadInfo get(String type)
	{
		return downloads.get(type);
	}
}
