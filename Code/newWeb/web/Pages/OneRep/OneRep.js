
// extract the context path using the window.location data items
function calculateContextPath() {
    var pathWithoutLeadingSlash = window.location.pathname.substring(1);
    var contextPathEndIndex = pathWithoutLeadingSlash.indexOf('/');
    return pathWithoutLeadingSlash.substr(0, contextPathEndIndex)
}

// returns a function that holds within her closure the context path.
// the returned function is one that accepts a resource to fetch,
// and returns a new resource with the context path at its prefix
function wrapBuildingURLWithContextPath() {
    var contextPath = calculateContextPath();
    return function(resource) {
        return "/" + contextPath + "/" + resource;
    };
}

// call the wrapper method and expose a final method to be used to build complete resource names (buildUrlWithContextPath)
var buildUrlWithContextPath = wrapBuildingURLWithContextPath();

var input;
//var openRep;
var refreshRate = 4000; //milli seconds
var EXIT_REP_URL=buildUrlWithContextPath("exitFromRep");
var USER_LIST_URL = buildUrlWithContextPath("usersList");
var REP_LIST_URL = buildUrlWithContextPath("RepsList");
var UPLOAD = buildUrlWithContextPath("uploadFile");
var FORK_REP = buildUrlWithContextPath("forkRep");
var OPEN_REP= buildUrlWithContextPath("openRep");
var CUR_USER=buildUrlWithContextPath("userRep");
var REP_DETAILS = buildUrlWithContextPath("RepDetails");
var CUR_NOTIFICATION=buildUrlWithContextPath("notifications");
var USER_PR_URL=buildUrlWithContextPath("prList");


function exitFromRep(){
    $.ajax(
        {
            url: EXIT_REP_URL,
            success: exitFromOneRepCallback
        }
    );
}
function DeleteBranch() {
    var U = document.getElementById("RepROwner");

    var p = window.prompt("Please enter the branch name for deletion", "");
    if (p != null) {
        $.ajax(
            {
                url: REP_DETAILS,
                data: {
                    action: "DeleteBranch",
                    Bname: p,
                    u: U.innerText
                },
                success: function (message) {
                    finishDeleteBranch(message)
                }
            });
    }
}

function finishDeleteBranch(message) {
    ajaxBranchesList();
    window.alert(message);
}
function newBranch() {
    var p = window.prompt("Please enter the new branch name", "");
    if (p != null) {
        $.ajax(
            {
                url: REP_DETAILS,
                data: {
                    action: "newBranch",
                    Bname: p
                },
                success: function (message) {
                    finishNewBranch(message)
                }
            });
    }
}
function finishNewBranch(message) {
    ajaxBranchesList();
    window.alert(message);
}
function exitFromOneRepCallback() {
    window.location = "../AllRep/AllRep.html";
}

$(function ajaxGetHeadInfo()
{
    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "getHeadBInfo"
        },
        success: function (list) {
            updateHeadCallBack(list)
        }
    });
});

function ajaxGetWC() {
    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "GetWC"
        },
        success: function (files) {
            refreshWCList(files)
        }

    });
}
function updateHeadCallBack(list) {
    document.getElementById("HeadB").append(list);


}

// function refreshWCList(files) {
//     $("#listOfFiles").empty();
//     $.each(files || [], function(index, file) {
//         var oneUser = updateWCCallBack(file,index+1);
//         console.log(oneUser);
//
//         $("#listOfFiles").append(oneUser);

//     });
//
// }
function updateWCCallBack(file,index)
{
return "<li class=\"list-group-item d-sm-flex justify-content-sm-center\"><button class=\"btn btn-primary d-sm-flex flex-fill justify-content-sm-center align-items-sm-end\" id="+index+" type=\"button\">"+file+"</button></li>\n";
}

function createOnClickForFile(com,index,path)
{
console.log(com.name);
console.log(path);
    var fileB = document.getElementById(com.Sha1+"Blob");
console.log(fileB);
    fileB.onclick = function () {
        console.log("checksasas");

        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "OpenFileWC",
                file: com.name,
                path:path
            },
            success: function (files) {
                finishShowFile(files)
            }
        });
    };
}

function DeleteFolder() {
    var p = window.prompt("Please enter the folder path for deletion", "");
    var n = window.prompt("Please enter the folder name for deletion", "");
    if (p != null && n != null) {
        $.ajax(
            {
                url: REP_DETAILS,
                data: {
                    action: "DeleteFolder",
                    path: p,
                    name: n
                },
                success: function (answer) {
                    finishDeleteFolder(answer)
                }
            });
    }
}

function finishDeleteFolder(answer)
{
    if(answer != "")
    {
        window.alert(answer);
    }
    ajaxGetWC();
}
function AddFolder() {
    var p =  window.prompt("Please enter the new folder path","");
    var n =  window.prompt("Please enter the new folder name","");
    if (p != null && n != null) {

        $.ajax(
            {
                url: REP_DETAILS,
                data: {
                    action: "AddNewFolder",
                    path: p,
                    name: n
                },
                success: function (answer) {
                    finishAddFolder(answer)
                }
            });
    }
}

function finishAddFolder(answer) {
    if(answer != "")
    {
        window.alert(answer);
    }
    ajaxGetWC();

}
function AddNewFile() {
    var p = window.prompt("Please enter the new file path", "");
    var n = window.prompt("Please enter the new file name", "");
    if (p != null && n != null) {

        $.ajax(
            {
                url: REP_DETAILS,
                data: {
                    action: "AddNewFile",
                    path: p,
                    name: n
                },
                success: function (answer) {
                    finishAddFile(p, n, answer)
                }
            });
    }
}
function finishAddFile(p,n,answer) {
    if(answer != "")
    {
        window.alert(answer);
    }
    else {
        ajaxGetWC();
        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "OpenFile",
                file: p,
                name: n
            },
            success: function (files) {
                finishShowFile(files)
            }
        });
    }
}

function DeleteFile() {
    // var p =  window.prompt("Please enter the new file path","");
    // var n =  window.prompt("Please enter the new file name","");

    $.ajax(
        {
            url: REP_DETAILS,
            data: {
                action: "DeleteFile",
            },
            success: function (answer) {
                finishDeleteFile(answer)
            }
        } );
}

function SaveFile()
{
    var fileB = document.getElementById("fileContent");
    var a = fileB.value;

    $.ajax(
        {
            url: REP_DETAILS,
            data: {
                action: "SaveFile",
                content: a
            },
            success: finishUpdateFile

        } );

}

function finishUpdateFile()
{
    $("#FileName").empty();
    $("#fileContent").val('');

   // document.getElementById("fileContent").empty();

    document.getElementById("FileName").append("File Name: ");

    ajaxUpdateLstOfChange();


}
function CreateCommit() {
     var a =  window.prompt("Please enter a description for the commit:","");
    if (a != null) {

        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "CreateCommit",
                desc: a
            },
            success: function (message) {
                finishCommit(message)
            }
        });
    }
}
function finishCommit(message) {
    window.alert(message);

    ajaxCommitsList();
    ajaxUpdateLstOfChange();
}
function finishDeleteFile(answer)
{
    if(answer != "")
    {
        window.alert(answer);
    }
    $("#FileName").empty();
    $("#fileContent").val('');

    document.getElementById("FileName").append("File Name: ");

    ajaxGetWC();
    ajaxUpdateLstOfChange

}
function CreatePR() {
    var U = document.getElementById("RepROwner");

    var a =  window.prompt("Please enter the name of the target branch(local):","");
    var b =  window.prompt("Please enter the name of the base branch:(remote)","");
    var c =  window.prompt("Please enter a description for the PR:","");
    if (a != null && b != null && c != null) {

        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "createPR",
                target: a,
                base: b,
                desc: c,
                u: U.innerText
            },
            success: function (answer) {
                finishCreatePR(answer)
            }

        });
    }
}
function finishCreatePR(answer)
 {
         window.alert(answer);
}
function finishShowFile(files)
{
    $("#FileName").empty();

    document.getElementById("FileName").append("File Name: ");
    $("#fileContent").val(files[1]);

    document.getElementById("FileName").append(files[0]);
    //document.getElementById("fileContent").append(files[1]);
    ajaxUpdateLstOfChange();

}
$(function ajaxGetRemoteInfo()
{
    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "getRemoteInfo"
        },
        success: function (list) {
            updateRepCallBack(list)
        }
    });
    ajaxGetWC();
    ajaxUpdateLstOfChange();
});

function ajaxUpdateLstOfChange()
{
    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "listOfChanges"
        },
        success: function (list) {
            updateListOfChange(list)
        }
    });
};

function updateListOfChange(list) {
    $("#listOfChanges").val('');
    $("#listOfChanges").val(list);

}
function updateRepCallBack(list)
{
    document.getElementById("RepRName").append(list[0]);
    document.getElementById("RepROwner").append(list[1]);

}
// $(function ajaxGetBranchesInfo()
// {
//     $.ajax({
//         url: REP_DETAILS,
//         data: {
//             action: "GetBranchesInfo"
//         },
//         success: updateRepCallBack
//     });
// });

$(function() {

    //The users list is refreshed automatically every second
    ajaxBranchesList();
    setInterval(ajaxBranchesList, refreshRate);
    //updateCurUser();
    ajaxCommitsList();
    setInterval(ajaxCommitsList, refreshRate);

});

function ajaxBranchesList() {
    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "GetBranchesInfo"
        },
        success: function(branches) {
            refreshBranchesList(branches);
        }
    });
}
function refreshBranchesList(branches) {
    $("#accordion-1").empty();
    $.each(branches || [], function(index, branch) {
        var oneUser = createBranchLine(branch,index+1);

        $("#accordion-1").append(oneUser);
         createOnClickForBranch(branch,index+1);
    });

}

function createBranchLine(branch,index)
{
    return "<div class=\"card\">\n" +
        "<div class=\"card-header\" role=\"tab\">\n" +
        "\n" +
        "<h6 class=\"mb-0\"><a class=\"visible\" data-toggle=\"collapse\" aria-expanded=\"true\" aria-controls=\"accordion-1 .item-"+index+"\" href=\"#accordion-1 .item-"+index+"\">"+branch.branchName+"</a><button class=\"btn btn-primary\" id="+index+"checkout"+" type=\"button\">Checkout</button></h6>\n" +
        "</div>\n" +
        "<div class=\"collapse show item-"+index+"\" role=\"tabpanel\" data-parent=\"#accordion-1\">\n" +
        "<div class=\"card-body\">\n" +
        "<p class=\"card-text\">"+branch.description+"</p>\n" +
        "</div>\n" +
        "</div>\n" +
        "</div>";
}

function createOnClickForBranch(branch,index)
{
    var checkout = document.getElementById(index+"checkout");
    var push = document.getElementById(branch.branchName+"push");
    var pull = document.getElementById(branch.branchName+"pull");

    checkout.onclick = function () {
        console.log(checkout);

        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "Checkout",
                branch: branch.branchName
            },
            success: function(answer) {
                finishCheckOut(answer);
            }              });
    };
}

function finishCheckOut(answer) {
    if(answer != "") {
        window.alert(answer);
    }
    else {
        window.location = "OneRep.html";

        ajaxBranchesList();
        ajaxCommitsList();
    }
}
function Pull() {
    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "Pull"
        },
        success: function(answer) {
            finishPull(answer);
        }    });
};
function finishPull(answer) {
    window.alert(answer);

}
function Push() {
    var a = document.getElementById("RepROwner");

    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "Push",
            name:a.innerText
        },
        success: function(answer) {
            finishPush(answer);
        }     });
};
function finishPush(answer)
{
ajaxCommitsList();
ajaxUpdateLstOfChange();
ajaxUpdateNotification();
    window.alert(answer);


}
function ajaxCommitsList() {
    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "GetCommitsInfo"
        },
        success: function(commits) {
            refreshCommitsList(commits);
        }
    });
}
function refreshCommitsList(commits) {
    $("#accordion-2").empty();
    $.each(commits || [], function(index, commit) {
        var oneUser = createCommitLine(commit,index+1);

        $("#accordion-2").append(oneUser);
       createOnClickForCommit(commit,index+1);

    });
}
function createCommitLine(commit,index)
{
    return "<div class=\"card\">\n" +
        "<div class=\"card-header flex-fill\" role=\"tab\">\n" +
        "<h5 class=\"mb-0\"><a data-toggle=\"collapse\" aria-expanded=\"true\" aria-controls=\"accordion-2 .item-"+index+"\" href=\"#accordion-2 .item-"+index+"\">"+commit.description+"</a><button class=\"btn btn-primary float-right\" id="+commit.SHA1+" type=\"button\">Show files</button></h5>\n" +
        "</div>\n" +
        "<div class=\"collapse show item-"+index+"\" role=\"tabpanel\" data-parent=\"#accordion-2\">\n" +
        "<div class=\"card-body\">\n" +
        "<p class=\"card-text\">Commit info:\n "+"On "+commit.creationDate+"by " +commit.username+ ". SHA1: "+commit.SHA1+"</p>\n" +
        "</div>\n" +
        "</div>\n" +
        "</div>";
}
function createOnClickForCommit(commit,index)
{
    var currUser = document.getElementById(commit.SHA1);
    //currUser.textContent = json[i];
    currUser.onclick = function () {
        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "showCommitFiles",
                commit: commit.SHA1
            },
            success: function(commitFiles) {
                showCommitFiles(commitFiles);
            }});
    };
}
function  refreshWCList(commitFiles)
{
    console.log("Start");
    $("#listOfFiles").empty();
    // var coms = commitfiles.components;
    // $.each(coms || [], function(index, com) {
    var path = "";
        showCommitFilesRec(commitFiles,"#listOfFiles",path);
    // });
}

function  showCommitFilesRec(com,id,path)
{
    // $("#accordion-2").empty();
    // var coms = commitfiles.components;
    // $.each(coms || [], function(index, com) {
    var coms = com.components;
    $.each(coms || [], function(index, com) {
        // console.log(com.name);
        // console.log(path);

        if(com.directObject.content != null) {
            var oneUser = createFileLineBlob(com);
             console.log(oneUser);
            $(id).append(oneUser);
               createOnClickForFile(com,index+1,path);

        }
        else
        {

            var b = "<li class=\"list-group-item d-sm-flex align-items-sm-center\">\n" +
                "<div class=\"container\"><button class=\"btn btn-primary text-left d-sm-flex flex-fill justify-content-sm-center\" id="+index+"Folder"+" type=\"button\" style=\"width: inherit;\">"+com.name+"</button>\n" +
                "<ul id="+com.name+"SubFolder"+" class=\"list-group\" style=\"overflow-y: scroll;\">\n" +
                "</ul>\n" +
                "</div>\n" +
                "</li>";
            // var a = "<li class=\"list-group-item d-sm-flex\"><button class=\"btn btn-primary text-left d-sm-flex flex-fill justify-content-sm-center\" id="+index+"Folder"+" style=\"width: inherit;\">"+com.name+"</button>\n" +
            //     "<ul id="+com.name+"SubFolder"+" class=\"list-group d-sm-flex\">\n" +
            //     "</ul>\n" +
            //     "</li>";
            // console.log(a);
            $(id).append(b);
            showCommitFilesRec(com.directObject,"#"+com.name+"SubFolder",path+"\\"+com.name)

        }

    });
    // });
}
function  showCommitFiles(commitfiles)
{
    $("#CommitFiles").empty();

    $("#CommitFiles").append(commitfiles);

}
function createFileLineBlob(com) {

    return "<li class=\"list-group-item d-sm-flex align-items-sm-center\">\n" +
        "<div class=\"container\"><button class=\"btn btn-primary text-left d-sm-flex flex-fill justify-content-sm-center\" id="+com.Sha1+"Blob"+" type=\"button\" style=\"width: inherit;\">"+com.name+"</button>\n" +
        "</div>\n" +
        "</li>";

    // return "<li class=\"list-group-item d-sm-flex\"><button class=\"btn btn-primary text-left d-sm-flex flex-fill justify-content-sm-center\" type=\"button\" id="+com.Sha1+"Blob"+" style=\"width: inherit;\">"+com.name+"</button>\n" +
    //     "</li>";
}
function createFileLineFolder() {
    return "<li class=\"list-group-item d-sm-flex\"><button class=\"btn btn-primary text-left d-sm-flex flex-fill justify-content-sm-center\" id=\"filePath\" type=\"button\" style=\"width: inherit;\">file path</button>\n" +
        "                    <ul class=\"list-group d-sm-flex\">\n" +
        "                        <li class=\"list-group-item d-sm-flex\" id=\"fileInDirPath\" type=\"button\"><button class=\"btn btn-primary text-left d-sm-flex flex-fill justify-content-sm-center\">file path</button></li>\n" +
        "                    </ul>\n" +
        "                </li>";
}
function ajaxUpdateNotification() {
    $.ajax({
        url: CUR_NOTIFICATION,
        success: function (list) {
            finishUpdate(list);
        }
    });
}
function finishUpdate(list)
{
    $("#forkNot").append(list[0]);
    $("#UpdateNot").append(list[2]);
    $("#PRNot").append(list[1]);

}
function ajaxPRList() {
    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "prList",
        },
        success: function(PRs) {
            refreshPRList(PRs);

        }
    });
}
function refreshPRList(PRs) {
    $("#listOfPR").empty();
    $.each(PRs || [], function(index, pr) {
        var oneUser = createPRLine(pr,index+1);
        console.log(oneUser);

        $("#listOfPR").append(oneUser);
        createOnClickForRP(pr,index+1);

    });

}
function createOnClickForRP(pr,index)
{
    var currUser = document.getElementById(index+"PR");
    console.log(currUser);
    //currUser.textContent = json[i];

    currUser.onclick = function () {
        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "GetPR",
                pr:pr
            },
            success: function(pr) {
                ShowPRDetailes(pr);
            }});
        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "PRCommit",
                pr:pr
            },
            success: function(files) {
                RefreshelistOfCommitChanges(files);
            }});
    };
}
function ShowPRDetailes(pr)
{
    var currUser = document.getElementById("PRDesc");
currUser.append(pr);

}

function RefreshelistOfCommitChanges(files) {
    $("#FileslistOfPR").empty();
    $.each(files || [], function(index, file) {
        var oneUser = createChangedFileLine(file,index+1);
        console.log(oneUser);

        $("#FileslistOfPR").append(oneUser);
        createOnClickForRPChange(file,index+1);

    });
}


function finishShowChagedFiles(files)
{
    $("#PRfileContentFor").val('');
    $("#PRfileContentFor").val(files[1]);
    $("#PRFileName").empty();
    $("#PRFileName").append("File name: ");
    $("#PRFileName").append(files[0]);
}
function createOnClickForRPChange(file,index) {
    var currUser = document.getElementById(index+"Change");
    //currUser.textContent = json[i];
    currUser.onclick = function () {
        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "OpenChangedFile",
                file:file
            },
            success: function(files) {
                finishShowChagedFiles(files);
            }});
    };

}
function createChangedFileLine(file,index)
{
return "<li class=\"list-group-item d-sm-flex justify-content-sm-center\"><button class=\"btn btn-primary text-left float-left d-sm-flex flex-fill justify-content-sm-center align-items-sm-end\" id="+index+"Change"+" type=\"button\">"+file+"</button></li>\n";
}


function createPRLine(pr,index)
{
return "<li class=\"list-group-item d-sm-flex justify-content-sm-center\"><button class=\"btn btn-primary text-left float-left d-sm-flex flex-fill justify-content-sm-center align-items-sm-end\" id="+index+"PR"+" type=\"button\">"+pr+"</button></li>\n";
}

$(function updateCurRep()
{
    $.ajax({
        url: CUR_USER,
        success: function(rep) {
            updateRep(rep);
        }
    });

    ajaxPRList
    setInterval(ajaxPRList, refreshRate);

    ajaxUpdateNotification();
    setInterval(ajaxUpdateNotification, refreshRate);

});

function  updateRep(rep){
    // $("#RepName").empty();
    // var a  = ""
    // $("#RepName").append(creator);
    $("#RepName").append(rep);

}
function AcceptPR() {
    $.ajax({
        url: REP_DETAILS,
        data: {
            action: "AcceptPR"
        },
        success: function(rep) {
            finishAcceptPR(rep);
        }    });
}
function finishAcceptPR(rep) {
    if (rep != "") {
        window.alert(rep);
    } else {

    ajaxGetWC();
    ajaxCommitsList();
    ajaxBranchesList();
}
}
function DeclinePR() {
    var a = window.prompt("Please enter the Description of the decline:", "");
    if (a != null) {
        $.ajax({
            url: REP_DETAILS,
            data: {
                action: "DeclinePR",
                Desc: a
            },
            success: function (answer) {
                finishDeclinePR(answer);
            }
        });
    }
}
function finishDeclinePR(answer) {
    if(answer != "")
    {
        window.alert(answer);
    }
}