package Classess;

public class FileTypeForUI {

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    private String path;

    public FileTypeForUI(String path, String type) {
        this.path = path;
        this.type = type;
    }

    private  String type;
}
