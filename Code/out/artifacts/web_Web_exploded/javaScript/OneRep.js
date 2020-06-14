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

var intervalForStatusGame;
var intervalForRunGame;
var refreshRate = 2000; //milli seconds
var chatVersion = 0;
var CHAT_LIST_URL = buildUrlWithContextPath("chat");
var GAME_URL = buildUrlWithContextPath("gameActions");
//variables
//var roundCompenent=$("#roundCounter");
//var playersComponent=$("#playersTable");
var arrayPlayersComponent=new Array();
var arrayYield=new Array();
var arrayTuring=new Array();
//var boardComponent=$("#boardTableComponent");
var arrayBoard=new Array();
var myColor;
var cellOnAction=0;
var arrayForTableBuyQuantity=new Array();

var curIndexMsg=0;

window.onload = function(){
    createHTMLFromGame();
    $(document.getElementById("chatarea")).empty();
    document.getElementById("chat").style.visibility="hidden";
};

function createHTMLFromGame(){
    $.ajax({
        url: GAME_URL,
        data: {
            action: "getGame"
        },
        success: createHTMLFromGameCallBack
    });
}

function createHTMLFromGameCallBack(json) {

    var game=json[0];
    myColor=json[1];

    createRoundComponent(game.numOfRounds);
    createPlayersComponent(game.players);
    createBoard(game.board);
    createNeturalTeritoryActionComponent(game.forces);
    createMyCellTableActionComponent(game.forces);
}

function ajaxGameStatus() {
    $.ajax({
        url: GAME_URL,
        data: {
            action: "getStatus"
        },
        success: checkGameStatus
    });
}

function checkGameStatus(json) {

    if (json[0] === true) {
        document.getElementById("chat").style.visibility="visible";

        clearInterval(intervalForStatusGame);
        document.getElementById("divExitBeforeGameStart").style.visibility="hidden";
        ajaxGameRun();
        setInterval(ajaxUpdateChat,refreshRate);
        intervalForRunGame= setInterval(ajaxGameRun, refreshRate);
        $('#GameStatusColor').attr('class','statusDivGameStarted');
        //$('GameStatusColor').addClass("statusDivGameStarted");
        document.getElementById('gameStatusText').innerText="game start!!!";
        document.getElementById("divLastAction").style.visibility="visible";
        setTimeout(function () {
            document.getElementById('gameStatusText').innerText=" ";
        },5000)
    }
    else{
        createPlayersComponent(json[1],null);
    }
}

function updateBoardColor(cellColors,cellBooleanAction) {
    for(var i=0;i<arrayBoard.length;i++){
        //arrayBoard[i].style.background=cellColors[i];
        arrayBoard[i].style.background=cellColors[i].toString();

        if(cellBooleanAction[i]===true) {
            arrayBoard[i].style.opacity = "1";
            arrayBoard[i].disabled = false;
        }
        else {
            arrayBoard[i].style.opacity="0.4";
            arrayBoard[i].disabled = true;
        }
    }
}

function updatePageCallBack(json) {
    createPlayersComponent(json[0],json[1]);
    updateBoardColor(json[2],json[3]);
    document.getElementById("spanRoundCounter").innerText=json[5];
    document.getElementById("divLastAction").innerText=json[8];
    if(!json[6]) {
        clearInterval(intervalForRunGame);
        updateRightComponent([4]);
        if (json[7] === null)
            document.getElementById("EGattackStatus").innerText = "draw";
        else {
            document.getElementById("EGattackStatus").innerText = "" + json[7].name + " is the winner";
            document.getElementById("EGattackStatus").style.color =json[7].color;
        }
        var popup = document.getElementById("EGframeOfPopup");
        popup.style.display = "block";
    }
    else{if(json[4]) {
        clearInterval(intervalForRunGame);
        document.getElementById("btnSkipTurn").style.visibility="visible";
        document.getElementById("btnExitPlayer").style.visibility="visible";
    }}
}

function ajaxUpdateChat() {
    $.ajax({
        url: GAME_URL,
        data: {
            action: "chatUpdate",
            indexMsg: curIndexMsg
        },
        success: appendToChatArea
    });
}

function ajaxGameRun() {
    $.ajax({
        url: GAME_URL,
        data: {
            action: "updatePage",
        },
        success: updatePageCallBack
    });
}

function exitPlayerAfterGameStarted(){
    $.ajax({
        url: GAME_URL,
        data: {
            action: "exitPlayerAfterGameStarted"
        },
        success: ExitbeforeGameStartCallback
    });
}

//activate the timer calls after the page is loaded
$(function() {
    //The users list is refreshed automatically every second
    intervalForStatusGame = setInterval(ajaxGameStatus, refreshRate);
});

function createRoundComponent(numOfRound){
    $('#roundCounter').empty();
    $('<font size="20px"  id="spanRoundCounter">0/'+numOfRound+'</font>').appendTo($('#roundCounter'));
}

function createPlayersComponent(players,playerIndex) {
    if (playerIndex === null)
        playerIndex = 5;
    $('#playersTable').empty();
    var imgArrow = $(document.createElement("img"));
    imgArrow.attr("src", "../image/arrow.png");
    for (var i = 0; i < players.length; i++) {
        arrayPlayersComponent[i] = createPlayer(players[i], i);
        var tr = $(document.createElement("tr"));
        var th1_1 = $(document.createElement("th"));
        var th1_2 = $(document.createElement("th"));
        if (playerIndex === i)
            imgArrow.appendTo(th1_2);

        arrayPlayersComponent[i].appendTo(th1_1);
        th1_1.appendTo(tr);
        th1_2.appendTo(tr);
        tr.appendTo($('#playersTable'));


    }
}

function createPlayer(player,playaerIndex){
    var trForReturn=$(document.createElement("div"));
    var tableForOnePlayer=$(document.createElement("table"));
    tableForOnePlayer.addClass("tableForOnePlayer");
    var tr1=$(document.createElement("tr"));
    var th1_1=$(document.createElement("th"));//להוסיף תמונה של צבע השחקן
    var th1_2=$(document.createElement("th"));
    var divColor=$(document.createElement("div"));
    divColor.addClass("divColorPlayer");
    divColor.css("background-color",player.color);
    var txtName=$(document.createElement("span"));
    txtName.text(player.name);
    txtName.css("font-size","18px");
    txtName.addClass("nameOfPlayer");
    txtName.id="nameOfPlayer";
    divColor.appendTo(th1_1);
    txtName.appendTo(th1_2);
    th1_1.appendTo(tr1);
    th1_2.appendTo(tr1);
    tr1.appendTo(tableForOnePlayer);

    var tr2=$(document.createElement("tr"));
    var th2_1=$(document.createElement("th"));
    var th2_2=$(document.createElement("th"));
    var txtTuringHead=$(document.createElement("span"));
    txtTuringHead.css("font-size","14px");
    txtTuringHead.text("Turing:");
    arrayTuring[playaerIndex]=$(document.createElement("span"));
    arrayTuring[playaerIndex].css("font-size","14px");
    arrayTuring[playaerIndex].text(player.turing);
    txtTuringHead.appendTo(th2_1);
    arrayTuring[playaerIndex].appendTo(th2_2);
    th2_1.appendTo(tr2);
    th2_2.appendTo(tr2);
    tr2.appendTo(tableForOnePlayer);

    var tr3=$(document.createElement("tr"));
    var th3_1=$(document.createElement("th"));
    var th3_2=$(document.createElement("th"));
    var txtYieldHaed=$(document.createElement("span"));
    txtYieldHaed.css("font-size","14px");
    txtYieldHaed.text("Yield:");
    arrayYield[playaerIndex]=$(document.createElement("span"));
    arrayYield[playaerIndex].css("font-size","14px");
    arrayYield[playaerIndex].text(player.yield);
    txtYieldHaed.appendTo(th3_1);
    arrayYield[playaerIndex].appendTo(th3_2);
    th3_1.appendTo(tr3);
    th3_2.appendTo(tr3);
    tr3.appendTo(tableForOnePlayer);

    tableForOnePlayer.appendTo(trForReturn);
    return trForReturn;
}

function createBoard(board){
    $("#boardTableComponent").empty();
    var boardTable=$("<table id='boardTable'></table>");
    for(var i=0;i < board.numberOfRows ; i++){
        var tr=$(document.createElement("tr"));
        for(var j=0; j<board.numberOfColumns;j++)
        {
            var cellID=i*board.numberOfColumns+j+1;
            var cell = board.board[i][j];
            var cell = createCell(cell);
            cell.appendTo(tr);
        }
        tr.appendTo(boardTable);
    }
    boardTable.appendTo($("#boardTableComponent"));
    var th = $('#boardTable th');
    for (var i = 0; i < th.length; i++) {
        th[i].onclick = cellClicked;
        th[i].style.background = "#ffeeab";
        th[i].style.opacity="0.4";
        th[i].disabled=true;
        th[i].idCell=i;
        arrayBoard[i]=th[i];
    }

}

function createCell(cell){
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

    return thForReturn;
}

function updateBuySelect(turing) {
    var selectArray = $('#buyTableDivActionComponent select');
    selectArray.empty();
    for (var i = 0; i < selectArray.length; i++) {
        var max=turing/parseInt(selectArray[i].cost);
        for(var j=0;j<max+1;j++) {
            var option = document.createElement("option");
            option.text = j;
            selectArray[i].add(option);
        }
        arrayForTableBuyQuantity[i]=0;
    }

}

function buyArmyInNewTeritory() {
    var cost=document.getElementById("buyArmyTableCost");
    var power=document.getElementById("buyArmyTablePower");
    var stringQuantity="";
    for(var i=0;i<arrayForTableBuyQuantity.length;i++)
        stringQuantity+=arrayForTableBuyQuantity[i].toString()+" ";
    $.ajax({
        url: GAME_URL,
        data: {
            action: "buyArmyNewTerritory",
            cellID:cellOnAction.idCell,
            quantitiesArray: stringQuantity,
            totalPrice:cost.innerText,
            totalPower: power.innerText
        },
        success: buyArmyNewTerritoryCallBack
    });
}

function clearBuyTable() {
    document.getElementById("errorBuyArmyForNewTeritory").innerText="";
    document.getElementById("buyArmyTableCost").innerText=0;
    document.getElementById("buyArmyTablePower").innerText=0;
}

function buyArmyNewTerritoryCallBack(json) {
    if(json[0]===""){
        json[0]=0;
        updateRightComponent(json);
        ajaxGameRun();
    }
    else{
        var errorSpan=document.getElementById("errorBuyArmyForNewTeritory");
        errorSpan.innerText=json[0];
    }
}

function updateArmyInTableDivAction(army) {
    var arrayTH=$(".quantity");
    for(var i=0;i<arrayTH.length;i++)
        arrayTH[i].innerText=army.army[i].numOfSoldier;
    arrayTH=$(".fitness");
    for(var i=0;i<arrayTH.length;i++) {
        if (army.army[i].numOfSoldier === 0)
            arrayTH[i].innerText = "0/100";
        else
            arrayTH[i].innerText = "" + army.army[i].competence + "/100";
    }
    var arrayTH=$(".power");
    for(var i=0;i<arrayTH.length;i++)
        arrayTH[i].innerText=""+army.army[i].power+"/"+army.army[i].maxPower;

}

function updateRightComponent(json) {
    $('#buyTableDivActionComponent').appendTo($('#containers'));
    $('#unitTableDivActionComponent').appendTo($('#containers'));
    $('#myCellDivActionsComponent').appendTo($('#containers'));
    $('#AttackDivActionComponent').appendTo($('#containers'));
    $('#trainArmy').appendTo($('#containers'));
    document.getElementById("errormessegeForTrainArmy").style.visibility="hidden";
    document.getElementById("messegeForTrainArmy").style.visibility="hidden";

    switch (json[0]) {
        case 0:
            $('#unitTableDivActionComponent').appendTo($('#rightDiv'));
            updateArmyInTableDivAction(json[1]);
            break;
        case 1:
            $('#buyTableDivActionComponent').appendTo($('#rightDiv'));

            document.getElementById("buttonForBuyArmy").onclick = buyArmyInNewTeritory;
            clearBuyTable();
            updateBuySelect(json[3]);
            break;
        case 2:
            $('#unitTableDivActionComponent').appendTo($('#rightDiv'));
            $('#myCellDivActionsComponent').appendTo($('#rightDiv'));

            document.getElementById("myCellDivActionsComponent").style.left = "40px";
            document.getElementById("trainArmy").style.left = "60px";
            clearBuyTable();
            updateBuySelect(json[3]);
            updateArmyInTableDivAction(json[1]);
            document.getElementById("messegeForTrainArmy").innerText = "Price for train is " + json[4]+" turing";
            break;
        case 3:
            $('#AttackDivActionComponent').appendTo($('#rightDiv'));
            $('#buyTableDivActionComponent').appendTo($('#rightDiv'));
            document.getElementById("buttonForBuyArmy").onclick = luckyAttack;
            clearBuyTable();
            updateBuySelect(json[3]);
            break;
        case 4:
            if (cellOnAction !== 0)
                cellOnAction.style.borderColor = "#000000";
            cellOnAction=0;
            break;
    }
}

function luckyClicked(){
    document.getElementById("buttonForBuyArmy").onclick = luckyAttack;

}

function goodTimingClicked(){
    document.getElementById("buttonForBuyArmy").onclick = goodTimingAttack;
}

function goodTimingAttack() {
    var cost=document.getElementById("buyArmyTableCost");
    var power=document.getElementById("buyArmyTablePower");
    var stringQuantity="";
    for(var i=0;i<arrayForTableBuyQuantity.length;i++)
        stringQuantity+=arrayForTableBuyQuantity[i].toString()+" ";
    $.ajax({
        url: GAME_URL,
        data: {
            action: "goodTimming",
            cellID:cellOnAction.idCell,
            quantitiesArray: stringQuantity,
            totalPrice:cost.innerText,
            totalPower: power.innerText
        },
        success: goodTimingAttackCallBack
    });
}

function goodTimingAttackCallBack(json) {
    if (json[0] === "") {
        $("#GTunitTableBody").empty();
        if (json[1] === null)
            json[0] = 4
        else
            json[0] = 0;
        updateRightComponent(json);
        ajaxGameRun();
        if (json[7] === true)
            $("#GTattackStatus").text("You Are The Winner!!!");
        if (json[7] === null)
            $("#GTattackStatus").text("It's a Draw!!!");
        if (json[7] === false)
            $("#GTattackStatus").text("You Loose!!!");

        var forces = json[8]
        var forcesTable = $("#GTunitTableBody");
        for (var i = 0; i < forces.length; i++) {
            var tr = $(document.createElement("tr"));
            var th1 = $(document.createElement("th"));
            th1.addClass("headResult");
            th1.text(forces[i].name);
            var th2 = $(document.createElement("th"));
            th2.text(json[2][i]);
            th2.css("background-color",myColor);
            var th3 = $(document.createElement("th"));
            th3.text(json[4].army[i].numOfSoldier);
            th3.css("background-color",json[6]);
            var th4 = $(document.createElement("th"));
            th4.css("background-color",myColor);
            var th5 = $(document.createElement("th"));
            th5.css("background-color",json[6]);
            var sub = parseInt(json[2][i]) - parseInt(json[4].army[i].numOfSoldier)
            if (sub > 0) {
                th4.text(sub);
                th5.text(0);
            } else {
                th4.text(0);
                th5.text(-sub);
            }

            th1.appendTo(tr);
            th2.appendTo(tr);
            th3.appendTo(tr);
            th4.appendTo(tr);
            th5.appendTo(tr);

            tr.appendTo(forcesTable);
            var popup = document.getElementById("GTframeOfPopup");
            popup.style.display = "block";
        }
    } else {
        var errorSpan = document.getElementById("errorBuyArmyForNewTeritory");
        errorSpan.innerText = json[0];
    }
}

function luckyAttack() {
    var cost=document.getElementById("buyArmyTableCost");
    var power=document.getElementById("buyArmyTablePower");
    var stringQuantity="";
    for(var i=0;i<arrayForTableBuyQuantity.length;i++)
        stringQuantity+=arrayForTableBuyQuantity[i].toString()+" ";
    $.ajax({
        url: GAME_URL,
        data: {
            action: "luckyAttack",
            cellID:cellOnAction.idCell,
            quantitiesArray: stringQuantity,
            totalPrice:cost.innerText,
            totalPower: power.innerText
        },
        success: luckyAttackCallBack
    });
}

function luckyAttackCallBack(json) {
    if (json[0] === "") {
        $("#LAunitTableBody").empty();
        if (json[1] === null)
            json[0] = 4
        else
            json[0] = 0;
        updateRightComponent(json);
        ajaxGameRun();
        if (json[7] === true)
            $("#LAattackStatus").text("You Are The Winner!!!");
        else
            $("#LAattackStatus").text("You Loose!!!");

        $("#LAattackerName").text(json[3]);
        $("#LAdefenderName").text(json[5]);
        var forces = json[8]
        var forcesTable = $("#LAunitTableBody");
        for (var i = 0; i < forces.length; i++) {
            var tr = $(document.createElement("tr"));
            var th1 = $(document.createElement("th"));
            th1.addClass("headResult");
            th1.text(forces[i].name);
            var th2 = $(document.createElement("th"));
            th2.text(json[2][i]);
            th2.css("background-color",myColor);
            var th3 = $(document.createElement("th"));
            th3.text(json[2][i] * forces[i].power);
            th3.css("background-color",myColor);
            var th4 = $(document.createElement("th"));
            th4.text(json[4].army[i].numOfSoldier);
            th4.css("background-color",json[6]);
            var th5 = $(document.createElement("th"));
            th5.css("background-color",json[6]);
            th5.text(json[4].army[i].power);

            th1.appendTo(tr);
            th2.appendTo(tr);
            th3.appendTo(tr);
            th4.appendTo(tr);
            th5.appendTo(tr);

            tr.appendTo(forcesTable);
            var popup=document.getElementById("LAframeOfPopup");
            popup.style.display= "block";
        }
    } else {
        var errorSpan = document.getElementById("errorBuyArmyForNewTeritory");
        errorSpan.innerText = json[0];
    }
}

function LAclosePopup(){
    var divForShowBoard=document.getElementById("LAframeOfPopup");
    divForShowBoard.style.display= "none";
}

function GTclosePopup(){
    var divForShowBoard=document.getElementById("GTframeOfPopup");
    divForShowBoard.style.display= "none";
}

function EGclosePopup(){
    $.ajax({
        url: GAME_URL,
        data: {
            action: "endGame"
        },
        success: EGclosePopupCallback
    });
}

function EGclosePopupCallback() {
    window.location = "lobby.html";
}

function openTrainArmyInMyCell(){
    $('#buyTableDivActionComponent').appendTo($('#containers'));
    $('#trainArmy').appendTo($('#rightDiv'));
    document.getElementById("errormessegeForTrainArmy").style.visibility="hidden";
    document.getElementById("messegeForTrainArmy").style.visibility="visible";
}

function trainArmyInMyCell(){
    $.ajax({
        url: GAME_URL,
        data: {
            action: "trainArmy",
            cellID:cellOnAction.idCell
        },
        success: trainArmyCallBack
    });
}

function trainArmyCallBack(json){
    document.getElementById("messegeForTrainArmy").style.visibility="hidden";
    if(json[0]===""){
        json[0]=0;
        updateRightComponent(json);
        ajaxGameRun();
        document.getElementById("errormessegeForTrainArmy").style.visibility="hidden";
    }
    else{
        document.getElementById("errormessegeForTrainArmy").innerText=json[0];
        document.getElementById("errormessegeForTrainArmy").style.visibility="visible";
    }
}

function buyArmyInMyCell() {
    var cost=document.getElementById("buyArmyTableCost");
    var stringQuantity="";
    for(var i=0;i<arrayForTableBuyQuantity.length;i++)
        stringQuantity+=arrayForTableBuyQuantity[i].toString()+" ";
    $.ajax({
        url: GAME_URL,
        data: {
            action: "buyArmyInMyCell",
            cellID:cellOnAction.idCell,
            quantitiesArray: stringQuantity,
            totalPrice:cost.innerText,
        },
        success: buyArmyNewTerritoryCallBack
    });
}

function openBuyArmyInMyCell(){
    $('#trainArmy').appendTo($('#containers'));
    $('#buyTableDivActionComponent').appendTo($('#rightDiv'));
    document.getElementById("errormessegeForTrainArmy").style.visibility="hidden";
    document.getElementById("messegeForTrainArmy").style.visibility="hidden";
    document.getElementById("buttonForBuyArmy").onclick=buyArmyInMyCell;
}

function cellClicked(event){
    if(event.currentTarget.disabled===false) {
        if (cellOnAction !== 0)
            cellOnAction.style.borderColor = "#000000";
        cellOnAction = event.currentTarget;
        var cellIndex = cellOnAction.idCell;
        cellOnAction.style.borderColor = "#ffffff";
        $.ajax({
            url: GAME_URL,
            data: {
                action: "getCellClickedInfo",
                cellID: cellIndex
            },
            success: updateRightComponent
        });
    }
}

function createNeturalTeritoryActionComponent(forces) {
    var forcesTable = $("#buyTableBody");
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
        var th6 =$(document.createElement("th"));
        var quantityCB=$(document.createElement("select"));
        //quantityCB.id=i;
        quantityCB.prop("idQuery",i);
        quantityCB.prop("power",forces[i].power);
        quantityCB.prop("cost",forces[i].cost);
        quantityCB.change(function (event) {
            var quantitySelect=event.currentTarget.value;
            var indexForcesArray=event.currentTarget.idQuery;
            arrayForTableBuyQuantity[indexForcesArray]=quantitySelect;
            updateCostAndPowerBuyTable();
        })
        quantityCB.appendTo(th6);

        th1.appendTo(tr);
        th2.appendTo(tr);
        th3.appendTo(tr);
        th4.appendTo(tr);
        th5.appendTo(tr);
        th6.appendTo(tr);

        tr.appendTo(forcesTable);
    }
}

function createMyCellTableActionComponent(forces) {
    var myCellTable = $("#myCellTableBody");
    for (var i = 0; i < forces.length; i++) {
        var tr = $(document.createElement("tr"));
        var th1 =$(document.createElement("th"));
        th1.text(forces[i].rank);
        var th2 =$(document.createElement("th"));
        th2.text(forces[i].name);

        var th3 =$(document.createElement("th"));
        th3.text("");
        th3.addClass("quantity");
        var th4 =$(document.createElement("th"));
        th4.text("");
        th4.addClass("fitness");
        var th5 =$(document.createElement("th"));
        th5.text("");
        th5.addClass("power");

        th1.appendTo(tr);
        th2.appendTo(tr);
        th3.appendTo(tr);
        th4.appendTo(tr);
        th5.appendTo(tr);

        tr.appendTo(myCellTable);
    }
}

function updateCostAndPowerBuyTable(){
    var cost=document.getElementById("buyArmyTableCost");
    var power=document.getElementById("buyArmyTablePower");
    var sumCost=0;
    var sumPower=0;
    var selectArray = $('#buyTableDivActionComponent select');
    for (var i = 0; i < selectArray.length; i++) {
        sumCost+=(parseInt(selectArray[i].cost)*arrayForTableBuyQuantity[i]);
        sumPower+=(parseInt(selectArray[i].power)*arrayForTableBuyQuantity[i]);
    }
    cost.innerText=sumCost;
    power.innerText=sumPower;
}

function skipTurn() {
    $.ajax({
        url: GAME_URL,
        data: {
            action: "skipTurn"
        },
        success: skipTurnCallBack
    });
}

function skipTurnCallBack() {
    updateRightComponent([4]);
    document.getElementById("btnSkipTurn").style.visibility="hidden";
    document.getElementById("btnExitPlayer").style.visibility="hidden";
    ajaxGameRun();
    intervalForRunGame= setInterval(ajaxGameRun, refreshRate);
}

function ExitbeforeGameStart(){
    $.ajax(
        {
            url: GAME_URL,
            data: {
                action : "exitPlayerBeforeGameStart"
            },
            type: 'GET',
            success: ExitbeforeGameStartCallback
        }
    );
}

function ExitbeforeGameStartCallback(json) {
    if (json)
        window.location = "lobby.html";
    else
        document.getElementById("errorMassageExitPlayerBeforeGameStar").innerText = "The game has already started ";
}

function addMSG(){
    var textMsg=document.getElementById("userstring").value;
    document.getElementById("userstring").value="";
    if(textMsg!=="") {
        $.ajax(
            {
                url: GAME_URL,
                data: {
                    action: "addMessage",
                    content: textMsg
                },
                type: 'GET'
            }
        );
    }
}

function appendToChatArea(entries) {
//    $("#chatarea").children(".success").removeClass("success");

    // add the relevant entries
    $.each(entries || [], appendChatEntry);

    // handle the scroller to auto scroll to the end of the chat area
    var scroller = $("#chatarea");
    var height = scroller[0].scrollHeight - $(scroller).height();
    $(scroller).stop().animate({ scrollTop: height }, "slow");
}

function appendChatEntry(index, entry){
    curIndexMsg++;
    var entryElement = createChatEntry(entry);
    $("#chatarea").append(entryElement).append("<br>");
}

function createChatEntry (entry){
    return $("<span class=\"success\">").append(entry.name + "> " + entry.data);
}