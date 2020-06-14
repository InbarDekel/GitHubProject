package Classess;





import generated.*;

import java.io.*;
import java.util.*;
import java.util.ArrayList;

public class Folder implements FileObject{

    //members
    private ArrayList<Component> components;



    private  boolean isRoot;
    //con
//    Folder(Component c)
//    {
//        components = new ArrayList<>();
//        components.add(c);
//    }
    public Folder(ArrayList<Component> list) {
        components = list;
    }


    public static class Component implements Comparable<Component>, FileObject {
        //members
        private FolderType type;


        private String Sha1;
        private String name;



        private String lastUpdater;




        private String lastUpdateDate;
        private FileObject directObject;

        //con
        Component(String name, String sha1, FolderType type, String lastUpdater, String lastUpdateDate) {

            this.type = type;
            this.name = name;
            this.lastUpdateDate = lastUpdateDate;
            this.Sha1 = sha1;
            this.lastUpdater = lastUpdater;

        }       // from text/xml file


        //get\set
        public void setLastUpdateDate(String lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
        }
        public void setLastUpdater(String lastUpdater) {
            this.lastUpdater = lastUpdater;
        }
        public String getLastUpdateDate() {
            return lastUpdateDate;
        }

        public FileObject getDirectObject() {
            return directObject;
        }

        public void setDirectObject(FileObject directObject) {
            this.directObject = directObject;
        }

        public FolderType getComponentType() {
            return type;
        }

        public String getComponentSHA1() {
            return Sha1;
        }

        public void setSha1(String sha1) {
            Sha1 = sha1;
        }

        public String getComponentName() {
            return name;
        }

        public String getFolderSHA1() {
            return Sha1;
        }

        //methods
        //comperator
        public int compareTo(Component folderComponent) {
            return this.name.compareTo(folderComponent.name);
        }


        String getComponentsStringFromComponent() {
            StringBuilder content = new StringBuilder();
            content.append(name);
            content.append(",");
            content.append(Sha1);
            content.append(",");
            content.append(type);
            content.append(",");
            content.append(lastUpdater);
            content.append(",");
            content.append(lastUpdateDate);
            return content.toString();

        }

        static Component getComponentFromString(String s) {
            String[] st = s.split(",");
            Component c = new Component(st[0], st[1], getTypeFromString(st[2]), st[3], st[4]);
            return c;
        }

    }

    //con
    public Folder() // creating by XML or new empty root
    {
        components = new ArrayList<>();

    }

    public Folder(File file) //creating by text file
    {

    }

    //set\get

    public boolean isRoot() {
        return isRoot;
    }

    public void setComponents(ArrayList<Component> components) {
        this.components = components;
    }

    public ArrayList<Component> getComponents() {
        return this.components;
    }

    //methods
    static FolderType getTypeFromString(String s) {
        if ("Folder".equals(s)) {
            return FolderType.Folder;
        } else {
            return FolderType.Blob;
        }
    }


    public String stringComponentsToString() {
        StringBuilder content = new StringBuilder();
        for (Component a : components) {
            content.append(a.getComponentsStringFromComponent());
            content.append(System.lineSeparator());
        }
        return content.toString();
    }



    ArrayList<Component> setComponentsFromString(String compomemtString) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(compomemtString));
        ArrayList<Component> st = new ArrayList<>();
        String a;
        int i = 0;
        while ((a = br.readLine()) != null) {
            st.add(Component.getComponentFromString(a));
            i++;
        }
        br.close();
        return st;
    }

    String getFolderContentString() {
        StringBuilder sb = new StringBuilder();

        for (Component c : components) {
            sb.append(c.getComponentsStringFromComponent());
            sb.append(System.lineSeparator());
        }


        return sb.toString();
    }

    public static Map<String, Component> getAllBlobsToMap(MagitBlobs blobs) throws Exception {
        List<MagitBlob> oldlist = blobs.getMagitBlob();
        Map<String, Component> newMap = new HashMap<>();
        for (MagitBlob blob : oldlist) {
            String sha1 = GitManager.generateSHA1FromString(blob.getContent());
            Component c = new Component(blob.getName(), sha1, FolderType.Blob, blob.getLastUpdater(), blob.getLastUpdateDate());
            if (!newMap.containsKey(blob.getId())) {
                newMap.put(blob.getId(), c);
                Blob b = new Blob(blob.getContent());
                c.setDirectObject(b);
            } else {
                throw new Exception("The ID of the blob already exist");
                //return exception key of blob is already exist in map(two similar id for two blobs)
            }
        }
        return newMap;
    }

    public static Map<String, Component> getAllFoldersToMap(MagitFolders folders) throws Exception {
        List<MagitSingleFolder> oldlist = folders.getMagitSingleFolder();
        Map<String, Component> newMap = new HashMap<>();
        for (MagitSingleFolder folder : oldlist) {
            //String sha1 = GitManager.generateSHA1FromString(folder);
            Component c = new Component(folder.getName(), null, FolderType.Folder, folder.getLastUpdater(), folder.getLastUpdateDate());
            if (!newMap.containsKey(folder.getId())) {
                newMap.put(folder.getId(), c);
            } else {
                //return exception key of folder is already exist in map(two similar id for two folder)
                throw new Exception("The ID of the folder already exist");
            }
        }
        return newMap;

    }

    public static void createListOfComponents(Map<String, Component> folderList, Map<String, Component> blobList, MagitFolders folders) throws Exception//create list of component to each folder
    {
        List<MagitSingleFolder> oldlist = folders.getMagitSingleFolder();
        for (MagitSingleFolder f : oldlist) {
            Folder folder = new Folder();
            folder.components = new ArrayList<>();
            for (Item i : f.getItems().getItem()) {
                if (i.getType().equals("folder")) {
                    if(folderList.containsKey(i.getId())) {
                        if(f.getId() != i.getId()) {
                            folder.components.add(folderList.get(i.getId()));
                        }
                        else {
                            //return folder is inside itself
                            throw new Exception("Folder is inside itself");
                        }
                    }
                    else
                    {
                        //return exception id not exit(component id of folder to attach to folder does not exist)
                        throw new Exception("The id of the folder does not exist");
                    }
                } else {
                    if(blobList.containsKey(i.getId())) {
                        folder.components.add(blobList.get(i.getId()));
                    }
                    else
                    {
                        //return exception id not exit(component id of blob to attach to folder does not exist)
                        throw new Exception("The id of the blob does not exist");


                    }
                }
            }
            folder.isRoot = f.isIsRoot();
            folderList.get(f.getId()).setDirectObject(folder);
            Collections.sort(folder.components);
        }
    }
    public Folder.Component getComponentByName(String s)
    {
        for(Folder.Component c:this.getComponents()) {
        if(c.getComponentName().equals(s))
            return c;
        }
        return null;
    }
}
//    public void exportToFile() // check if the sha1 exist
//    {
//
//    }


