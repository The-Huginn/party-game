// Home button
function homeAction() {
    loadHomePage($(".home-btn"));
}

const myAudioContext = new AudioContext();

// Confirm about page
function confirmAction() {
    loadHomePage($("#confirm"));
}

function categoriesAction() {
    $("#categories_form").submit(function (e) {
        e.preventDefault();

        var array = [];
        $("input:checkbox:checked").each(function() {
            array.push($(this).val());
        });

        $.ajax({
            url: "/categories",
            type: "post",
            data: {categories: array},
            success: function (response) {
                $("#content_placeholder").html(response);
                updateListeners();
            },
            error: function (xhr) {
                console.log(xhr);
            }
        })
    })
}

function loadHomePage(button) {
    button.on('click', function (e) {
        e.preventDefault();
    
        $.ajax({
            url: "/home",
            type: "get",
            success: function (response) {
                $("#content_placeholder").html(response);
                updateListeners();
            },
            error: function (xhr) {
                console.log("error")
            }
        });

        updateCss();
    });
}

// Game start
function gameStart() {
    $("#game_form").submit(function (e) {
        e.preventDefault();
    
        var name = $("#gameName").val();
    
        gameModeSelection("post", name);
    });
}

function continueGame() {
    nextMove();
}

function nextMove() {
    $.ajax({
        url: "/nextMove",
        type: "get",
        
        success: function (response) {

            
            $("#content_placeholder").html(response);
            
            if ($("#startTimer").length > 0) {
                $("#startTimer").focus();
            } else {
                $("#nextMove").focus();
            }
            updateCss(false);
        },
        error: function (xhr) {
            //Do Something to handle error
            console.log("error")
        }
    });

}

function discardOldGame() {
    gameModeSelection("get", getCookie('gameID'));
}

function gameModeSelection(method, gameID) {
    $.ajax({
        url: "/gameMode",
        type: method,
        data: { gameID: gameID },
        success: function (response) {
            $("#content_placeholder").html(response);
            updateListeners();
        },
        error: function (xhr) {
            console.log("error")
        }
    });
}

function updateListeners() {
    gameStart();
    categoriesAction();
    confirmAction();
}

function modeSelection(selected) {

    $.ajax({
        url: "/" + selected + "Mode",
        type: "post",
        success: function (response) {
            $("#content_placeholder").html(response);
            updateListeners();
        },
        error: function (xhr) {
            console.log("error")
        }
    });
}

function updateCss(defaultCss=true) {
    
    $.ajax({
        url: "/css",
        type: "get",
        data: {defaultCss: defaultCss},

        success: function (response) {
            $('#area').css(JSON.parse(response))
        },
        error: function (xhr) {
            console.log("error" + xhr.responseType);
        }
    });

}

function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for(let i = 0; i <ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) == ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
      }
    }
    return "";
  }

$(document).ready(function() {
    homeAction();
    updateListeners();
    updateCss();
});