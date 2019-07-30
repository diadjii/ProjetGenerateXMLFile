import java.util.ArrayList;

public class ClassItem {
    private String className;
    private ArrayList<ClassProperty> listProperty = new ArrayList<>();

    static ArrayList<ClassItem> listClass = new ArrayList<>();

    public ClassItem() {
    }

    public ClassItem(String className, ArrayList<ClassProperty> listPropertyItem) {
	this.className = className;
	this.listProperty = listPropertyItem;
    }

    public String getClassName() {
	return className;
    }

    public void setClassName(String className) {
	this.className = className;
    }

    public ArrayList<ClassProperty> getListProperty() {
	return listProperty;
    }

    public void setListProperty(ArrayList<ClassProperty> listProperty) {
	this.listProperty = listProperty;
    }

    public void addProperty(ClassProperty prop) {
	this.listProperty.add(prop);

    }

    @Override
    public String toString() {
	System.out.println("Nom Class : *" + this.className + "*");

	this.listProperty.forEach(item -> {
	    System.out.println("Nom Propriete : " + item.getName() + " Type Propriete : " + item.getType());
	});

	return "-------------Fin---------------";
    }
}
