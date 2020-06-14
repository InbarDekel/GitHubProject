
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
var openRep;
var refreshRate = 2000; //milli seconds
var EXITALLREP_URL=buildUrlWithContextPath("exitFromAllRep");
var USER_LIST_URL = buildUrlWithContextPath("usersList");
var REP_LIST_URL = buildUrlWithContextPath("REPList");
//var UPLOAD = buildUrlWithContextPath("uploadFile");
var GET_REP_INFO = buildUrlWithContextPath("getRepInfo");
var OPEN_REP= buildUrlWithContextPath("openRep");

function loadFile(event) {
    $("#errorUploadFileMessage").empty();
    var file = event.target.files[0];
    var reader = new FileReader();

    reader.onload = function () {
        var content = reader.result;
        $.ajax(
            {
                url: UPLOAD,
                data: {
                    file: content
                },
                type: 'POST',
                success: uploadGameCallback
            }
        );
    };
    reader.readAsText(file);
}

function uploadGameCallback(json){
    if(json==null){}
    else{
        $('<span>' + json + '</span>').appendTo($("#errorUploadFileMessage"));
    }
}

function refreshUsersList(users) {
        //clear all current users
    $("#listUfUsers").empty();
    var trHaedline=$('<tr style="background: #62c1ff"></tr>')   ;
    var spanHaedline=$("<span>online Users:</span>");
    spanHaedline.appendTo(trHaedline);
    trHaedline.appendTo($("#listUfUsers")) ;
        // rebuild the list of users: scan all users and add them to the list of users
    $.each(users || [], function(index, username) {
        console.log("Adding user #" + index + ": " + username);
            //create a new <option> tag with a value in it and
            //appeand it to the #userslist (div with id=userslist) element
        var spann=$('<span>' + username + '</span>') ;
        var trr=$('<tr style="alignment: center" class="userTR"></tr>');
        spann.appendTo(trr);
        trr.appendTo($("#listUfUsers"));
        //$('<tr class="userTR"><span>' + username + '</span></tr>').appendTo
    });
}

function ajaxUsersList() {
    $.ajax({
        url: USER_LIST_URL,
        success: function(users) {
            refreshUsersList(users);
            }
    });
}

function refreshRepList(reps) {

    //clear all current users
    $("#gamesTableBody").empty();

//<div class="popupCloseButton" onclick="closeBoard()">X</div>
    reps.forEach(function (rep) {
        var tr = $(document.createElement('tr'));
        var tdGameStatus = $(document.createElement('td'))
        var divGameStatus=$(document.createElement("div"));
        if(rep.status===true)
            divGameStatus.addClass("statusDivGameStarted");
        else
            divGameStatus.addClass("statusDivGameDontStarted");
        var tdGameName = $(document.createElement('td')).text(rep.name);
        var tdCreatorName = $(document.createElement('td')).text(rep.creator);
        var tdPlayerNumber = $(document.createElement('td')).text(rep.currentNumOfPlayers + " / " + game.numOfPlayers);

        tdGameStatus.addClass("tableGame1");
        tdGameName.addClass("tableGame2");
        tdCreatorName.addClass("tableGame3");
        tdPlayerNumber.addClass("tableGame4");

        divGameStatus.appendTo(tdGameStatus);
        tdGameStatus.appendTo(tr);
        tdGameName.appendTo(tr);
        tdCreatorName.appendTo(tr);
        tdPlayerNumber.appendTo(tr);
        tr.appendTo($("#gamesTableBody"));

    });
    var tr = $('#gamesTableBody tr');
    for (var i = 0; i < tr.length; i++) {
        tr[i].onclick = ShowBoard;
    }
    // rebuild the list of users: scan all users and add them to the list of users
}

function BuildForces(forces){

    var forcesTable = $("#unitTableBody");
    for (var i = 0; i < forces.length; i++) {
        var tr = $(document.createElement("tr"));
        var th1 =$(document.createElement("th"));
        th1.text(forces[i].rank);
        var th2 =$(document.createElement("th"));
        th2.text(forces[i].name);
        var th3 =$(document.createElement("th"));
        th3.text(forces[i].power);
        var th4 =$(document.createElement("th"));
        th4.text(forces[i].lessCompetence);
        var th5 =$(document.createElement("th"));
        th5.text(forces[i].cost);

        th1.appendTo(tr);
        th2.appendTo(tr);
        th3.appendTo(tr);
        th4.appendTo(tr);
        th5.appendTo(tr);

        tr.appendTo(forcesTable);
    }
}

function BuildBoard(game){
    var boardTable=$(document.createElement("table"));
    boardTable.attr('id','tableBoard');
    for(var i=0;i < game.board.numberOfRows ; i++){
        var tr=$(document.createElement("tr"));
        for(var j=0; j<game.board.numberOfColumns;j++)
        {
            var cellID=i*game.board.numberOfColumns+j+1;
            var cell = game.board.board[i][j];
            var thCell=BuildCell(cell);
            thCell.appendTo(tr);
        }
        tr.appendTo(boardTable);
    }
    return boardTable;
}

function BuildCell(cell) {
    var minPower = cell.minimalPower;
    var yield = cell.yield;
    var thForReturn = $("<th class='cell'></th>");
    var tableCell = $("<table></table>");
    var trMinPowerH = $(" <tr></tr>");
    trMinPowerH.text("min power")
    var trMinPowerT = $("<tr></tr>");
    trMinPowerT.text(minPower)
    var trYieldH = $(" <tr></tr>");
    trYieldH.text("yield")
    var trYieldT = $("<tr></tr>");
    trYieldT.text(yield)


    trMinPowerH.appendTo(tableCell);
    trMinPowerT.appendTo(tableCell);
    trYieldH.appendTo(tableCell);
    trYieldT.appendTo(tableCell);

    tableCell.appendTo(thForReturn);

    return thForReturn
}

function joinGame(){
    $.ajax(
        {
            url: JOIN_GAME,
            data: {
                gameName: openGame
            },
            type: 'GET',
            success: joinGameClickedCallback
        }
    );
}

function joinGameClickedCallback() {
        window.location = "gameRoom.html";
}

function exitFromUser(){
    $.ajax(
        {
            url: EXITLOBBY_URL,
            type: 'GET',
            success: exitFromLobbyCallback
        }
    );
}

function exitFromLobbyCallback() {
    window.location = "../index.html";
}

function closeBoard() {
    var divForShowBoard=document.getElementById("frameOfPopup");
    divForShowBoard.style.display= "none";
}

function ShowBoard(event) {
    var th = event.currentTarget.children[1];
    var nameGame = th.innerText;
    $.ajax(
        {
            url: GET_GAME_INFO,
            data: {
                gameName: nameGame
            },
            type: 'GET',
            success: ShowBoardCallBack
        }
    );
}

function ShowBoardCallBack(game) {
    openGame = game.name;
    $("#divBoard").empty();
    $("#unitTableBody").empty();
    BuildForces(game.forces);
    var boardTable = BuildBoard(game);
    boardTable.appendTo($("#divBoard"));
    var popup = document.getElementById("frameOfPopup");
    popup.style.display = "block";
    if (game.status === false)
        document.getElementById("joinBTN").style.visibility = "visible";
    else
        document.getElementById("joinBTN").style.visibility = "hidden";
}

function ajaxRepList() {
    $.ajax({
        url: REP_LIST_URL,
        success: function(reps) {
            refreshRepList(reps);
        }
    });
}

//activate the timer calls after the page is loaded
$(function() {

        //The users list is refreshed automatically every second
    ajaxUsersList();
    setInterval(ajaxUsersList, refreshRate);

    //  ajaxRepList();
    // setInterval(ajaxRepList, refreshRate);


});