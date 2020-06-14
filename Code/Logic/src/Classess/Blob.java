package Classess;

public class  Blob  implements FileObject{

    //members
    private String content;

    //con
    public Blob(String Content)
    {
        this.content=Content;
    }

    //get\set
    public String getContent() { return content; }

    //methods
//    public void exportToFile()
//    {
//
//    }
}
