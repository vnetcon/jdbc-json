package com.vnetcon.jdbc.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.json.JSONObject;
import org.json.XML;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Json2DBConverter {
	
	private JsonParser gp = new JsonParser();
	private Connection con = null;
	private Map<String,List<String>> createdTables = new LinkedHashMap<String, List<String>>();
	private String defaultSchema = "main";
	private boolean bTruncateTables = true;
	private boolean isGenForeignConstraints = false;

	
	// autogenerated columns
	private String genpk = "genpk";
	private String genfk = "_genfk";
	private String loadtime = "loadtime";
	private String srcfile = "srcfile";
	
	
	private String dbConfig = null;
	private Properties dbConf = null;

	
	public Json2DBConverter(String dbConfig, Properties dbConf) {
		this.dbConfig = dbConfig;
		this.dbConf = dbConf;
	}
	
	public void setGenerateForeignKeyConstraints(boolean b) {
		this.isGenForeignConstraints = b;
	}
	
	public void setTruncateTables(boolean truncate) {
		bTruncateTables = truncate;
	}

	public boolean getTruncateTables() {
		return bTruncateTables;
	}
	
	public String getGenpk() {
		return genpk;
	}

	public void setGenpk(String genpk) {
		this.genpk = genpk;
	}

	public String getGenfk() {
		return genfk;
	}

	public void setGenfk(String genfk) {
		this.genfk = genfk;
	}

	public String getLoadtime() {
		return loadtime;
	}

	public void setLoadtime(String loadtime) {
		this.loadtime = loadtime;
	}

	public String getSrcfile() {
		return srcfile;
	}

	public void setSrcfile(String srcfile) {
		this.srcfile = srcfile;
	}
	
	public void setConnection(Connection extcon) {
		con = extcon;
	}
	
    private String readLines(File fileName, String encoding) throws Exception {
        StringBuilder sb = new StringBuilder();
        FileInputStream fIn = new FileInputStream(fileName);
        InputStreamReader isrIn = new InputStreamReader(fIn, encoding);
        BufferedReader bfIn = new BufferedReader(isrIn);
        String line = null;
        
        while((line = bfIn.readLine()) != null) {
        	sb.append(line);
        }
 
        fIn.close();
        return sb.toString();
    }	

    
    private void createTable(String prefix, String table, Map<String, String> cols, String schema) throws Exception {
    	StringBuilder sql = new StringBuilder();
    	List<String> alters = new ArrayList<String>();
    	String delim = "";
    	String ptable = prefix + table;
    	sql.append("CREATE TABLE IF NOT EXISTS \"" + schema + "\".\"" + ptable + "\" (");
    	
    	
    	for(String s : cols.keySet()) {
    		if(s.equals(loadtime)) {
    			sql.append(delim + "\"" + s + "\" " + dbConf.getProperty(dbConfig + ".json.timestamptype"));
    		} else {
        		if(s.indexOf(genfk) > 0 && isGenForeignConstraints) {
        			String p[] = s.split("_");
        			String referenceTable = p[0];
        			sql.append(delim + "\"" + s + "\" " + dbConf.getProperty(dbConfig + ".json.longtexttype") + " REFERENCES \"" + referenceTable + "\"(" + genpk + ")");
        		}else {
        			sql.append(delim + "\"" + s + "\" " + dbConf.getProperty(dbConfig + ".json.longtexttype"));
        		}
    		}
    		delim = ", ";
    		
    		if(s.indexOf(genfk) > 0  && isGenForeignConstraints) {
    			String p[] = s.split("_");
    			String referenceTable = p[0];
        		alters.add("ALTER TABLE \"" + schema + "\".\"" + ptable + "\" ADD COLUMN \"" + s + "\" " + dbConf.getProperty(dbConfig + ".json.longtexttype") + " REFERENCES \"" + referenceTable + "\"(" + genpk + ")");
    		}else {
        		alters.add("ALTER TABLE \"" + schema + "\".\"" + ptable + "\" ADD COLUMN \"" + s + "\" " + dbConf.getProperty(dbConfig + ".json.longtexttype"));
    		}

    	}
    	
    	
    	sql.append(")");
    	Statement stmt = con.createStatement();
    	if(!createdTables.containsKey(schema + ptable)) {
    		stmt.executeUpdate(sql.toString());
    		if(bTruncateTables) {
    			stmt.executeUpdate("DELETE FROM \"" + schema + "\".\"" + ptable +  "\"");
    		}
    		createdTables.put(schema + ptable, new ArrayList<String>());
    	}
    	for(String alter : alters) {
    		if(!createdTables.get(schema + ptable).contains(alter)) {
    			// this due to reason that we cannot use if not exists in SQLite.
    			// So we just try to add the column and if it exists then we get exception
    			try {
    				stmt.executeUpdate(alter);
    			}catch(Exception e) {
    				
    			}
    			createdTables.get(schema + ptable).add(alter);
    		}
    	}
    	stmt.close();
    }
    
    private void insertIntoTable(String prefix, String table, Map<String, String> cols, String schema) throws Exception {
    	createTable(prefix, table, cols, schema);
    	PreparedStatement pstmt = null;
    	StringBuilder sb = new StringBuilder();
    	StringBuilder sb2 = new StringBuilder();
    	List<String> values = new ArrayList<String>();
    	String ptable = prefix + table;
    	    	
    	sb.append("INSERT INTO \"" + schema + "\".\"" + ptable + "\" (");
    	String delim = "";
    	for(String s : cols.keySet()) {
    		if(!s.equals(loadtime)) {
	    		sb.append(delim + "\"" + s + "\"");
	    		sb2.append(delim + "?");
	    		delim = ", ";
	    		values.add(cols.get(s));
    		}
    	}
    	sb.append(") VALUES (");
    	sb.append(sb2);
    	sb.append(")");
    	
    	pstmt = con.prepareStatement(sb.toString());
    	for(int i = 0; i < values.size(); i++) {
    		pstmt.setString(i+1, values.get(i));
    	}
    	pstmt.execute();
    	pstmt.close();
    }
    
    private void arrayToRows(String foreighkey, String parentkey, String parentid, JsonArray po, String schema, String prefix) throws Exception {
    	for(int i = 0; i < po.size(); i++) {
    		String rowid = UUID.randomUUID().toString();
    		JsonElement elemat = po.get(i);
    		Map<String, String> cols = new LinkedHashMap<String, String>();
    		JsonObject jo = null;
    		jo = elemat.getAsJsonObject();

    		Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();

    		cols.put(genpk, rowid);
    		cols.put(foreighkey + genfk, parentid);
    		
    		for (Map.Entry<String, JsonElement> entry: entries) {
    			String key = entry.getKey();
    			JsonElement elem = entry.getValue();
    			if(elem.isJsonArray()) {
    				JsonArray a = elem.getAsJsonArray();
    				if(a.size() > 0 && a.get(0).isJsonObject()) {
    					arrayToRows(parentkey, key, rowid, a, schema, prefix);
    				} else {
    					//System.out.println("UNHANDLED ARRAY: " + a.toString());
    					cols.put(key, a.toString());
    				}
    			}
    			if(elem.isJsonPrimitive()) {
    				cols.put(key, elem.getAsString());
    			}
    			if(elem.isJsonNull()) {
    				cols.put(key, "null");
    			}
    			if(elem.isJsonObject()) {
    				JsonObject o = elem.getAsJsonObject();
    				elementToRows(parentkey, key, rowid, o, schema, prefix);
    			}
    		}
    		insertIntoTable(prefix, parentkey, cols, schema);
    	}
    }

    private void elementToRows(String foreignkey, String parentkey, String parentid, JsonObject jo, String schema, String prefix) throws Exception {
		Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();
		Map<String, String> cols = new LinkedHashMap<String, String>();
		String rowid = UUID.randomUUID().toString();
		cols.put(genpk, rowid);
		cols.put(foreignkey + genfk, parentid);
		
		for (Map.Entry<String, JsonElement> entry: entries) {
			String key = entry.getKey();
			JsonElement elem = entry.getValue();
			if(elem.isJsonArray()) {
				JsonArray a = elem.getAsJsonArray();
				if(a.size() > 0 && a.get(0).isJsonObject()) {
					arrayToRows(parentkey, key, rowid, a, schema, prefix);
				} else {
					//System.out.println("UNHANDLED ARRAY: " + a.toString());
					cols.put(key, a.toString());
				}
			}
			if(elem.isJsonPrimitive()) {
				cols.put(key, elem.getAsString());
			}
			if(elem.isJsonNull()) {
				cols.put(key, "null");
			}
			if(elem.isJsonObject()) {
				JsonObject o = elem.getAsJsonObject();
				elementToRows(parentkey, key, rowid, o, schema, prefix);
			}
		}
		insertIntoTable(prefix, parentkey, cols, schema);
    }
    
    
    private void writeJsonString2DB(String json, String schema, String filename) throws Exception {
    	Map<String, String> cols = new LinkedHashMap<String, String>();
		JsonElement je = gp.parse(json);
		JsonObject jo = je.getAsJsonObject();
		Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();
		String rowid = UUID.randomUUID().toString();
		File f = new File(filename);
		String tableprefix = "";
		// these are unnecessary here - garbage from previous implementation
		tableprefix = tableprefix.replace("_work", "");
		String logfilename = filename.replace("_work", "");
		String rootname = f.getName();
		String parentkey = rootname;
		cols.put(genpk, rowid);
		cols.put(srcfile, logfilename);
		cols.put(loadtime, "");
		
		for (Map.Entry<String, JsonElement> entry: entries) {
			String key = entry.getKey();
			JsonElement elem = entry.getValue();
			if(elem.isJsonArray()) {
				JsonArray a = elem.getAsJsonArray();
				arrayToRows(parentkey, key, rowid, a, schema, tableprefix);
			}
			if(elem.isJsonPrimitive()) {
				cols.put(key, elem.getAsString());
			}
			if(elem.isJsonNull()) {
				cols.put(key, "null");
			}
			if(elem.isJsonObject()) {
				JsonObject o = elem.getAsJsonObject();
				elementToRows(parentkey, key, rowid, o, schema, tableprefix);
			}
		}
		insertIntoTable(tableprefix, parentkey, cols, schema);
		
	}
	
    private void writeJSON2DB(File filename, String encoding, String schema) throws Exception {
		String json = readLines(filename, encoding);
		writeJsonString2DB(json, schema, filename.getName());
    }
    
	private void writeXML2DB(File fileName, String encoding, String schema) throws Exception {
		String xml = readLines(fileName, encoding);
		JSONObject jo = XML.toJSONObject(xml);
		String json = jo.toString();
		writeJsonString2DB(json, schema, fileName.getName());
	}
	
	public void parseFile(File fileName, String encoding, String schema) throws Exception {
		
		if(fileName.getName().toLowerCase().endsWith(".xml")) {
			writeXML2DB(fileName, encoding, schema);
		} else {
			writeJSON2DB(fileName, encoding, schema);
		}
	}

	public void makeHttpGetRequest(String url, String encoding, String user, String outFile) throws Exception {
		if(user != null) {
			ProcessBuilder p = new ProcessBuilder("curl", "-u", user, url);
			p.inheritIO();
			p.redirectOutput(new File(outFile));
			Process proc = p.start();
			proc.waitFor();
		} else {
			ProcessBuilder p = new ProcessBuilder("curl", url);
			p.inheritIO();
			p.redirectOutput(new File(outFile));
			Process proc = p.start();
			proc.waitFor();
		}
		File ooutFile = new File(outFile);
		parseFile(ooutFile, encoding, defaultSchema);
		
	}
	
}