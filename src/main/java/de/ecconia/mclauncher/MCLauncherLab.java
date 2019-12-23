package de.ecconia.mclauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.data.OnlineVersionList;
import de.ecconia.mclauncher.data.OnlineVersionList.OnlineVersion;
import de.ecconia.mclauncher.data.VersionInfo;

public class MCLauncherLab
{
	public static void main(String[] args)
	{
		String targetVersion = "1.15.1";
		
		OnlineVersionList onlineList = new OnlineVersionList();
		OnlineVersion targetVersionEntry = onlineList.getVersion(targetVersion);
		
		EasyRequest request = new EasyRequest(targetVersionEntry.getUrl());
		JSONObject object = (JSONObject) JSONParser.parse(request.getBody());
		VersionInfo version = new VersionInfo(object, targetVersionEntry.getUrl());
		
		//Custom options:
//		Locations.rootFolder.mkdirs(); //Ensure the root folder is ready.
//		VersionDownloader.download(version, request.asBytes());
//		installNatives(version);
		run(version);
	}
	
	@SuppressWarnings("unused")
	private static void installNatives(VersionInfo version)
	{
		File nativesFolder = new File(new File(Locations.versionsFolder, version.getInfo().getId()), version.getInfo().getId() + "-natives");
		version.getLibraryInfo().installNatives(Locations.librariesFolder, nativesFolder);
	}
	
	public static void run(VersionInfo version)
	{
		File versionFolder = new File(Locations.versionsFolder, version.getInfo().getId());
		//> which java
		//> l /usr/bin/java
		//> l /etc/alternatives/java
		//> --> /usr/lib/jvm/java-8-oracle/jre/bin/java
		//Find: /usr/lib/jvm/java-8-oracle/jre/bin/java
		// Why not just "java" then
		
		//Why: -Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M
		
		//Create classpath:
		String classpath = version.getLibraryInfo().genClasspath(Locations.librariesFolder);
		classpath += ':' + new File(versionFolder, version.getInfo().getId() + ".jar").getAbsolutePath();
		
		//Create natives directory:
		File nativesFolder = new File(versionFolder, version.getInfo().getId() + "-natives");
		
		List<String> arguments = new ArrayList<>();
		arguments.add("java"); //Here?
		arguments.add("-Xmx1G");
		arguments.add("-XX:+UseConcMarkSweepGC");
		arguments.add("-XX:+CMSIncrementalMode");
		arguments.add("-XX:-UseAdaptiveSizePolicy");
		arguments.add("-Xmn128M");
		arguments.addAll(version.getArguments().build(version, classpath, nativesFolder.getAbsolutePath()));
		
		for(String arg : arguments)
		{
			System.out.println(arg);
		}
		
		ProcessBuilder builder = new ProcessBuilder(arguments);
		builder.directory(new File("data"));
		
		try
		{
			Process process = builder.start();
			new Thread(() -> {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String tmp;
				try
				{
					while((tmp = reader.readLine()) != null)
					{
						System.out.println(">> " + tmp);
					}
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}).start();
			System.out.println("Started");
			process.waitFor();
			System.out.println("Terminated...");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
