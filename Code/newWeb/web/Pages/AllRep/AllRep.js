
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
var refreshRate = 2000; //milli seconds
var EXITALLREP_URL=buildUrlWithContextPath("exitFromAllRep");
var USER_LIST_URL = buildUrlWithContextPath("usersList");
var REP_LIST_URL = buildUrlWithContextPath("RepsList");
var UPLOAD = buildUrlWithContextPath("uploadFile");
var FORK_REP = buildUrlWithContextPath("forkRep");
var OPEN_REP= buildUrlWithContextPath("openRep");
var CUR_USER=buildUrlWithContextPath("userName");
var CUR_REP_LIST_URL = buildUrlWithContextPath("CurRepsList");
var CUR_NOTIFICATION=buildUrlWithContextPath("notifications");


function loadFile(event) {
    $("#errorUploadFileMessage").empty();
    var file = event.target.files[0];
    var reader = new FileReader();

    reader.onload = function () {
        var content = reader.result;
        console.log(content);
        $.ajax(
            {
                url: UPLOAD,
                data: {
                    file: content
                },
                type: 'POST',
                success: function(message) {

                    uploadRepCallback(message);
                }
            }
        );
    };
    reader.readAsText(file);
}

function uploadRepCallback(message){
    if (message !="") {
        window.alert(message);
    }
}

function refreshUsersListAll(users) {
        $("#usersList").empty();
    $.each(users || [], function(index, username) {
   var oneUser = createUserLine(username,index+1);
   console.log(oneUser);

   $("#usersList").append(oneUser);
        createOnClickForUser(username,index+1);
    });
}
function createOnClickForUser(username,index)
{
    console.log(index+"U");

    var currUser = document.getElementById(index+"U");
    //currUser.textContent = json[i];
    console.log(currUser);
    currUser.onclick = function () {
        $.ajax({
            url: REP_LIST_URL,
            data: {
                username: username
            },
            success: function(reps) {
                refreshOtherRepList(reps,username);
            }});
    };
}


function createUserLine(userName,index)
{
return "<li class=\"list-group-item float-right\"><button class=\"btn btn-primary\" id="+index+"U"+" type=\"button\">"+userName+"</button></li>";
}

function refreshCurRepList(reps) {
    $("#curReps").empty();
    $.each(reps || [], function (index, rep) {
        var oneRep = createRepLine(rep);
        console.log(oneRep);
        $("#curReps").append(oneRep);
        createOnClickForCurRep(rep);
    });

}

function createOnClickForCurRep(rep) {
    var currRep = document.getElementById(rep.activeBranch+"OB");
    //currUser.textContent = json[i];
    currRep.onclick = function () {
        $.ajax({
            url: OPEN_REP,
            data: {
                repName: rep.name
            },
            success: OpenRepClickedCallback
        });
    };
}


function refreshOtherRepList(reps,username) {
    $("#otherRep").empty();
    $.each(reps || [], function (index, rep) {
        var oneRep = createOtherRepLine(rep);
        console.log(oneRep);
        $("#otherRep").append(oneRep);
        createOnClickForOtherRep(rep,username);
    });

}
    function createOnClickForOtherRep(rep,username) {
        var currUser = document.getElementById(rep.activeBranch+"REP");
        //currUser.textContent = json[i];
        currUser.onclick = function () {
            $.ajax(
                {
                    url: FORK_REP,
                    data: {
                        repName: rep.name,
                        user:username,
                    },
                    type: 'GET',
                    success: function(answer) {
                        finishFork(answer);
                    }
                });
        }
    }

function finishFork(answer) {
    if(answer != "")
    {
        window.alert(answer);
    }
    ajaxUpdateNotification();
}
function createOtherRepLine(rep)
{
    return " <div class=\"card flex-shrink-1\">\n" +
        "<div class=\"card-header\" role=\"tab\">\n" +
        "<h5 class=\"mb-0\"><a data-toggle=\"collapse\" aria-expanded=\"true\" aria-controls=\"otherRep .item-1\" href=\"#otherRep .item-1\">"+rep.name+"</a><button class=\"btn btn-primary btn-sm float-right\" id="+rep.activeBranch+"REP"+" type=\"button\">Fork</button></h5>\n" +
        "</div>\n" +
        "<div class=\"collapse show item-1\" role=\"tabpanel\" data-parent=\"#otherRep\">\n" +
        "<div class=\"card-body\"><small class=\"d-sm-flex align-items-sm-start\" style=\"max-width: inherit;\">Active Branch: "+rep.activeBranch+"</small><small style=\"max-width: inherit;\">Amount Of Branches: "+rep.amountOfBranches+"</small><small class=\"d-sm-flex align-items-sm-start\" style=\"max-width: inherit;\">Date of Last commit: "+rep.dateOfLastCommit+"</small>\n" +
        "<small style=\"max-width: inherit;\">Description of last commit: "+rep.descriptionOfLastCommit+"</small>\n" +
        "</div>\n" +
        "</div>\n" +
        "</div>";
}
function createRepLine(rep)
{
// return "<div class=\"card-header\" role=\"tab\">\n" +
//     "<h5 class=\"mb-0\"><a data-toggle=\"collapse\" aria-expanded=\"true\" aria-controls=\"curReps .item-1\" href=\"#curReps .item-1\">"+rep.name+"</a></h5><button class=\"btn btn-primary float-right\" id="+rep.activeBranch+"OB"+" type=\"button\">Open</button></div>\n" +
//     "<div class=\"collapse show item-1\"\n" +
//     "role=\"tabpanel\" data-parent=\"#curReps\">\n" +
//     "<div class=\"card-body\"><small class=\"d-sm-flex align-items-sm-start\" style=\"max-width: inherit;\">Active Branch: "+rep.activeBranch+"</small><small style=\"max-width: inherit;\">Amount Of Branches:"+rep.amountOfBranches+"</small><small class=\"d-sm-flex align-items-sm-start\" style=\"max-width: inherit;\">Date of Last commit: "+rep.dateOfLastCommit+"</small>\n" +
//     "<small\n" +
//     "style=\"max-width: inherit;\">Description of last commit: "+rep.descriptionOfLastCommit+"</small>\n" +
//     "</div>";

return " <div class=\"card-header\" role=\"tab\"><button class=\"btn btn-primary btn-sm float-right\" id="+rep.activeBranch+"OB"+" type=\"button\">Open</button>\n" +
    "\n" +
    "<h5 class=\"mb-0\"><a data-toggle=\"collapse\" aria-expanded=\"true\" aria-controls=\"curReps .item-1\" href=\"#curReps .item-1\">"+rep.name+"</a></h5>\n" +
    "</div>\n" +
    "<div class=\"collapse show item-1\" role=\"tabpanel\" data-parent=\"#curReps\">\n" +
    "<div class=\"card-body\"><small class=\"d-sm-flex align-items-sm-start\" style=\"max-width: inherit;\">Active Branch: "+rep.activeBranch+"</small><small style=\"max-width: inherit;\">Amount Of Branches:"+rep.amountOfBranches+"</small><small class=\"d-sm-flex align-items-sm-start\" style=\"max-width: inherit;\">Date of Last commit: "+rep.dateOfLastCommit+"</small>\n" +
    "<small style=\"max-width: inherit;\">Description of last commit: "+rep.descriptionOfLastCommit+"</small>\n" +
    "</div>\n" +
    "</div>";
}

function ajaxUsersListAll() {
    console.log("aaaa");
    $.ajax({
        url: USER_LIST_URL,
                success: function(users) {
            refreshUsersListAll(users);
            }
    });
}


function openRep() {

    $.ajax(
        {
            url: OPEN_REP,
            data: {
                repName: openRep
            },
            type: 'GET',
            success: OpenRepClickedCallback
        }
    );

}

function OpenRepClickedCallback() {
    console.log("dfff");
        window.location = "../OneRep/OneRep.html";
}

function exitFromUser(){
    $.ajax(
        {
            url: EXITALLREP_URL,
            type: 'GET',
            success: exitFromAllRepCallback
        }
    );
}

function exitFromAllRepCallback() {
    window.location = "../sign/sign.html";
}




function ajaxRepList() {
    $.ajax({
        url: CUR_REP_LIST_URL,
        success: function(reps) {
            refreshCurRepList(reps);
        }
    });
}
// function forkRep(event) {
//     var th = event.currentTarget.children[1];
//     var repName = th.innerText;
//     var u = event.currentTarget;//rededeefewfewfwefcwef
//     $.ajax(
//         {
//             url: FORK_REP,
//             data: {
//                 name: repName,
//                 user: u
//             },
//             type: 'GET',
//             success: function (reps) {
//                 refreshCurRepList(reps);
//             }
//         } );
// }
//activate the timer calls after the page is loaded
$(function() {

    ajaxUpdateNotification();
    setInterval(ajaxUpdateNotification, refreshRate);

    //The users list is refreshed automatically every second
    ajaxUsersListAll();
    setInterval(ajaxUsersListAll, refreshRate);
    //updateCurUser();
     ajaxRepList();
    setInterval(ajaxRepList, refreshRate);


});

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
    console.log(list[0]);
    $("#forkNot").append(list[0]);
    $("#UpdateNot").append(list[1]);
    $("#PRNot").append(list[2]);

}
$(function updateCurUser()
{
    $.ajax({
        url: CUR_USER,
        success: function(creator) {
            updateUser(creator);
        }
    });
    // $("#userNameLabel").append();

});

function  updateUser(creator){
$("#userNameLabel").empty();
    $("#userNameLabel").append(creator);

}