package Classess;


import java.nio.file.Path;
import java.util.Map;

public enum MergeType {
    A("001000"), B("010000"), C("011000"), D("011001"),
    E("100000"), F("101000"), G("101010"),
    H("110000"), I("110100"), J("111000"), K("111001"),
    L("111010"), M("111100"), N("111111");

    private String situationString;

    MergeType(String name) {
        situationString = name;
    }

    Folder.Component decideFile(Path path,Map<Conflict, Folder> conflictMap, Folder.Component ourF, Folder.Component theirF, Folder.Component fatherF, Folder f) {
        switch (this) {
            case A://001000
            case G://101010
            case D://011001
                return null;
            case B://010000
            case L://111010

                return theirF;
            case C://011000
            case F://101000
            case H://110000
            case J://111000
            {
                Conflict con = new Conflict(ourF.getComponentName(),ourF,theirF,fatherF,path);
                conflictMap.put(con,f);
                Folder.Component c = new Folder.Component(con.conflictName,null,FolderType.Blob,null,null);

                return  c;
            }
            case E://100000
            case I://110100
            case N://111111
            case K://111001
            case M://111100
                return ourF;
        }
        return null;
    }
}

