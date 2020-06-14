package Classess;

import java.awt.image.ByteLookupTable;
import java.nio.file.Path;

public class Conflict {

    public String getConflictName() {
        return conflictName;
    }

    String conflictName;
    //    Folder folderOfFile;
    Folder.Component our;
    Folder.Component their;

    public Path getPathInFolder() {
        return pathInFolder;
    }

    Path pathInFolder;
    Folder.Component father;

    public Folder.Component getOur() {
        return our;
    }

    public Folder.Component getTheir() {
        return their;
    }

    public Folder.Component getFather() {
        return father;
    }

    public Conflict(String conflict, Folder.Component ourIn, Folder.Component theirIn, Folder.Component fatherIn, Path path) {
        conflictName = conflict;
        our =  ourIn;
        their =  theirIn;
        father = fatherIn;
        pathInFolder = path;
//pathInFolder = pathInFolderIn;
    }
}

