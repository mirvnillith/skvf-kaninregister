package se.skvf.tools;

import static org.apache.commons.io.FileUtils.readLines;
import static org.apache.commons.io.FileUtils.writeLines;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * The all-in-one WSDL for Visma Addo (https://vismaaddo.net/WebService/v2.0/SigningService.svc?singleWsdl)
 * has multiple schemas and that's not supported by jaxws-maven-plugin.
 * This tool splits VismaAddo.wsdl.original into separate XSDs and
 * re-writes the stripped WSDL as VismaAddo.wsdl.
 */
public class ExtractWsdlSchemas {

	public static void main(String[] args) throws Exception {
		
		File wsdl = new File("src/main/resources/wsdl/VismaAddo.wsdl.original");
		
		List<String> lines = readLines(wsdl, Charset.forName("UTF-8"));
		while (removeUntil(lines, "<xs:schema")) {
			List<String> schema = readUntil(lines, "</xs:schema>");
			importSchemaLocations(schema);
			String nameSpace = getNameSpaceId(schema);
			System.out.println(nameSpace + " ...");
			writeLines(new File(wsdl.getParentFile(), nameSpace + ".xsd"), schema);
		}
		
		System.out.println("VismaAddo.wsdl ...");
		lines = readLines(wsdl, Charset.forName("UTF-8"));
		importSchemaLocations(lines);
		int firstSchemaEnd = find(lines, "</xs:schema>", 0);
		while (!lines.get(firstSchemaEnd+1).contains("</wsdl:types>")) {
			lines.remove(firstSchemaEnd+1);
		}
		writeLines(new File(wsdl.getParentFile(), "VismaAddo.wsdl"), lines);
	}

	private static void importSchemaLocations(List<String> schema) {
		int xsImport = find(schema, "<xs:import", 0);
		while (xsImport != -1) {
			schema.set(xsImport, schema.get(xsImport) + schemaLocation(schema.get(xsImport+1)));
			xsImport = find(schema, "<xs:import", xsImport + 1);
		}
	}

	private static String schemaLocation(String namespace) {
		namespace = namespace.substring(namespace.indexOf('"')+1);
		namespace = namespace.substring(0, namespace.lastIndexOf('"'));
		return " schemaLocation=\""+getNameSpaceId(namespace)+".xsd\"";
	}

	private static int find(List<String> lines, String find, int beginIndex) {
		for (int i=beginIndex; i<lines.size(); i++) {
			if (lines.get(i).contains(find)) {
				return i;
			}
		}
		return -1;
	}

	private static String getNameSpaceId(List<String> schema) {
		for (String line : schema) {
			if (line.contains("targetNamespace")) {
				String targetNamespace = line.substring(line.indexOf('"')+1);
				targetNamespace = targetNamespace.substring(0, targetNamespace.lastIndexOf('"'));
				return getNameSpaceId(targetNamespace);
			}
		}
		throw new NullPointerException("targetNamespace not found in "+schema);
	}

	private static String getNameSpaceId(String namespace) {
		return namespace
				.replace('/', '_')
				.replace(':', '_');
	}

	private static boolean removeUntil(List<String> lines, String until) {
		while (lines.size() > 0 &&
				!lines.get(0).contains(until)) {
			lines.remove(0);
		}
		return lines.size() > 0;
	}
	
	private static List<String> readUntil(List<String> lines, String until) {
		List<String> read = new ArrayList<String>();
		while (lines.size() > 0 &&
				!lines.get(0).contains(until)) {
			read.add(lines.remove(0));
		}
		if (lines.size()>0) {
			read.add(lines.remove(0));
		}
		return read;
	}
}
