package controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import view.ClassItem;
import view.ClassProperty;
import view.HelperClass;
import view.MyCompiler;

public class Controller implements Initializable {
    @FXML
    private Button btnGenerateClasse;

    @FXML
    private Button btnOk;

    @FXML
    private Pane firstPane;

    @FXML
    private Pane secondPane;

    @FXML
    public VBox listFields = new VBox();

    @FXML
    private TextArea textArea;

    private ArrayList<TextField> l = new ArrayList<>();
    private int nn = 0;
    private int instanceNumber = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
	textArea.setEditable(false);

	btnGenerateClasse.setDisable(false);
	btnOk.setVisible(false);
	secondPane.setVisible(false);
    }

    @FXML
    void generateClasse(ActionEvent event) {
	HelperClass.generateClassFromXSD();

	for (int i = (HelperClass.listFiles.size() - 1); i > -1; i--) {
	    MyCompiler.compile(HelperClass.listFiles.get(i));
	}

	String text = new String();
	for (ClassItem c : ClassItem.listClass) {
	    text += c;
	}
	// ClassItem.listClass.forEach(cl ->
	// Controller.textArea.setText(cl.toString()));
	textArea.setText(text);

    }

    @FXML
    void chooseXSDFile(ActionEvent event) {
	Node node = (Node) event.getSource();

	FileChooser fileChooser = new FileChooser();

	File selectedFile = fileChooser.showOpenDialog(node.getScene().getWindow());

	Path d = Paths.get(selectedFile.getPath());
	Path monFichierCopie = Paths.get("fichiers/" + selectedFile.getName());

	try {
	    Files.copy(d, monFichierCopie);

	    btnGenerateClasse.setDisable(false);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (RuntimeException e) {
	    System.out.println("erreur");
	}
    }

    @FXML
    void createXMLFile() {
	textArea.setVisible(false);
	listFields.setVisible(true);
	btnOk.setVisible(true);
	instanceNumber++;
	ArrayList<String> typeNames = new ArrayList<>();

	for (ClassItem prop : ClassItem.listClass) {
	    typeNames.add(prop.getClassName());
	}
	int i = 0;
	for (ClassItem c : ClassItem.listClass) {
	    for (ClassProperty p : c.getListProperty()) {
		if (!typeNames.contains(p.getName())) {
		    addField(p.getName(), i);
		    i++;
		}
	    }
	}
    }

    // @FXML
    void addField(String label, int i) {
	listFields.setVisible(true);

	l.add(i, new TextField());

	l.get(i).setPromptText(label);

	listFields.getChildren().add(l.get(i));
	listFields.setMargin(l.get(i), new Insets(5.0, 0, 5, 5));
    }

    @FXML
    void getFields() throws IOException {
	ArrayList<String> classNames = new ArrayList<>();

	for (ClassItem prop : ClassItem.listClass) {
	    classNames.add(prop.getClassName());
	}

	StringBuilder xml = new StringBuilder();
	for (ClassItem c : ClassItem.listClass) {

	    if (c.getListProperty().size() < 2) {

		xml.append("<" + c.getClassName() + ">\n");
		for (int o = 0; o < instanceNumber; o++) {

		    if (getClassItemByName(c.getListProperty().get(0).getName()) != null) {
			xml.append(write(getClassItemByName(c.getListProperty().get(0).getName())));

		    } else {
			System.out.println("non");
		    }
		    xml.append("</" + c.getClassName() + ">\n");
		}
	    }
	}
	List<StringBuilder> lines = Arrays.asList(xml);

	Path file = Paths.get("file.xml");
	Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public StringBuilder write(ClassItem c) throws IOException {
	StringBuilder xml = new StringBuilder();
	try {
	    URLClassLoader classLoader = new URLClassLoader(new URL[] { new File("./").toURI().toURL() });

	    String className = HelperClass.upperCaseFirst(c.getClassName());

	    Class loadedClass = classLoader.loadClass("com.gn." + className);

	    Object obj = loadedClass.newInstance();

	    xml.append("<" + className + ">\n");
	    for (ClassProperty p : c.getListProperty()) {

		try {
		    Class[] paramString = new Class[1];

		    paramString[0] = String.class;
		    String attName = HelperClass.upperCaseFirst(p.getName());

		    Method setteur = loadedClass.getDeclaredMethod("set" + attName, paramString);

		    String val = "";
		    val = l.get(nn).getText();
		    nn++;
		    xml.append("<" + p.getName() + ">" + val + "</" + p.getName() + ">\n");
		    setteur.invoke(obj, val);

		    System.out.println(val);
		} catch (NoSuchMethodException e) {
		    e.printStackTrace();
		} catch (SecurityException e) {
		    e.printStackTrace();
		} catch (IllegalArgumentException e) {
		    e.printStackTrace();
		} catch (InvocationTargetException e) {
		    e.printStackTrace();
		}

	    }
	    xml.append("</" + className + ">\n");

	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (InstantiationException e) {
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	} catch (MalformedURLException e2) {
	    e2.printStackTrace();
	} catch (SecurityException e) {
	    e.printStackTrace();
	}
	return xml;
    }

    public ClassItem getClassItemByName(String name) {
	ClassItem c = null;
	for (ClassItem c1 : ClassItem.listClass) {
	    if (c1.getClassName().equals(name))
		c = c1;
	}

	return c;
    }
}
