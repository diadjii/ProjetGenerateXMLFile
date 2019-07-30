import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {

    static String TYPE_COMPLEX = "xs:complexType";

    public static void main(String[] args) {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	try {
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    factory.setIgnoringElementContentWhitespace(true);

	    File xmlFile = new File("test.xsd");

	    Document xml = builder.parse(xmlFile);
	    Element root = xml.getDocumentElement();
	    NodeList list = root.getElementsByTagName("xs:element");

	    for (int i = 0; i < list.getLength(); i++) {
		Element e = (Element) list.item(i);

		if (list.item(i) instanceof Element) {
		    if (e.hasChildNodes()) {

			ClassItem classItem = new ClassItem();

			classItem.setClassName(e.getAttribute("name"));

			NodeList childList = e.getChildNodes();

			createProppertyFromElement(childList, classItem);

			ClassItem.listClass.add(classItem);

		    } else {
			/*
			 * Dans cette section on verifie d'abord si l'element a un attribut #name 1-Si
			 * c'est le cas on regarde s'il n'existe pas deja dans la liste des attributs
			 * deja créé sinon on l'ajoute dans la liste.Et s'il existe, on regarde dans la
			 * liste si le type du propriete dans la liste ressemble à celui de l'element en
			 * cours sinon on le remplace par celui de l'element en cours.
			 */
			ClassProperty property = new ClassProperty();

			if (e.hasAttribute("name")) {
			    if (!checkPropertyExist(e.getAttribute("name"))) {

				property.setName(e.getAttribute("name"));
				property.setType(e.getAttribute("type").split(":")[1]);

				ClassProperty.listPropreties.add(property);
			    } else {

				for (ClassProperty p : ClassProperty.listPropreties) {
				    if (p.getName().equals(e.getAttribute("name"))
					    && p.getType().equals(e.getAttribute("name"))) {
					p.setType(e.getAttribute("type").split(":")[1]);
				    }
				}
			    }

			} else {
			    if (!checkPropertyExist(e.getAttribute("ref"))) {
				property.setName(e.getAttribute("ref"));
				property.setType(e.getAttribute("ref"));

				ClassProperty.listPropreties.add(property);
			    }
			}
		    }
		}
	    }
	    ClassProperty.listPropreties
		    .forEach(prop -> System.out.println(prop.getName() + " Type : " + prop.getType()));
	    ClassItem.listClass.forEach(cl -> System.out.println(cl));
	    createJavaClasse();
	} catch (ParserConfigurationException | SAXException | IOException e) {
	    e.printStackTrace();
	}
    }

    public static boolean checkPropertyExist(String name) {
	boolean response = false;

	for (ClassProperty prop : ClassProperty.listPropreties) {
	    if (prop.getName().equals(name)) {
		response = true;
		break;
	    }
	}
	return response;
    }

    public static void createProppertyFromElement(NodeList list, ClassItem currentClass) {
	for (int j = 0; j < list.getLength(); j++) {

	    if (list.item(j) instanceof Element) {
		Element def = (Element) list.item(j);

		if (def.getTagName().equals(TYPE_COMPLEX)) {
		    NodeList defChild = def.getChildNodes();

		    Element sequenceNode = (Element) defChild.item(1);

		    for (int k = 0; k < sequenceNode.getChildNodes().getLength(); k++) {
			NodeList els = sequenceNode.getChildNodes();

			if (els.item(k) instanceof Element) {
			    Element item = (Element) els.item(k);

			    ClassProperty property = new ClassProperty();

			    if (item.hasAttribute("name")) {
				if (!checkPropertyExist(item.getAttribute("name"))) {

				    property.setName(item.getAttribute("name"));
				    property.setType(item.getAttribute("type").split(":")[1]);

				    currentClass.addProperty(property);
				}
			    } else {
				if (!checkPropertyExist(item.getAttribute("ref"))) {
				    property.setName(item.getAttribute("ref"));
				    property.setType(item.getAttribute("ref"));

				    currentClass.addProperty(property);
				}
			    }

			    ClassProperty.listPropreties.add(property);
			}
		    }
		}
	    }
	}
    }

    public static void createJavaClasse() {
	for (ClassItem currentClass : ClassItem.listClass) {

	    String className = currentClass.getClassName();

	    className = className.substring(0, 1).toUpperCase() + className.substring(1);

	    ArrayList<ClassProperty> listAttributs = currentClass.getListProperty();
	    StringBuilder header = new StringBuilder();

	    header.append("package com.gn;" + "public class " + className + "{\n");

	    for (ClassProperty att : listAttributs) {

		StringBuilder atts = new StringBuilder();
		String typeAtt = att.getType().substring(0, 1).toUpperCase() + att.getType().substring(1);
		String nameAtt = att.getName();

		atts.append("private " + typeAtt + " " + nameAtt + ";\n");
		header.append(atts);

		header.append(generateGetteurAndSetteur(typeAtt, nameAtt));

	    }

	    header.append("public " + className + "(){}" + "}");

	    List<StringBuilder> lines = Arrays.asList(header);

	    Path file = Paths.get(className + ".java");

	    try {
		Files.write(file, lines, StandardCharsets.UTF_8);

		Path monFichierCopie = Paths.get("src/com/gn/" + className + ".java");
		Path files = Files.move(file, monFichierCopie);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    private static StringBuilder generateGetteurAndSetteur(String typeAtt, String name) {
	StringBuilder setAndGet = new StringBuilder();
	String capName = name.substring(0, 1).toUpperCase() + name.substring(1);

	// creation du Setteur
	setAndGet.append(
		"public void set" + capName + " (" + typeAtt + " " + name + "){this." + name + " = " + name + ";}");
	// creation du Getteur
	setAndGet.append("public " + typeAtt + " get" + capName + " (){return this." + name + ";}");

	return setAndGet;
    }
}
