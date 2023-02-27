// Home button
function homeAction() {
    loadHomePage($(".home-btn"));
}

const myAudioContext = new AudioContext();

function loadHomePage(button) {
    button.on('click', function (e) {
        e.preventDefault();
    
        $.ajax({
            url: "/home",
            type: "get",
            success: function (response) {
                $("#content_placeholder").html(response);
            },
            error: function (xhr) {
                console.log("error")
            }
        });

        updateCss();
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
        },
        error: function (xhr) {
            console.log("error")
        }
    });
}

function modeSelection(selected) {

    $.ajax({
        url: "/" + selected + "Mode",
        type: "post",
        success: function (response) {
            $("#content_placeholder").html(response);
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

function updateLanguages() {

    $.ajax({
        url: "/languages",
        type: "get",
        
        success: function (response) {
            $.each(response, function(k,v) {
                $("#language").append(`<option value=${k}>
                                        ${v['name']}
                                        </option>`);
            });

            // for setting session language as selected
            $.ajax({
                url: "/language",
                type: "get",

                success: function(response) {
                    $("#language").val(response);
                }
            })
        }
    });
}

function languageChange() {
    var lang = $("#language").val();
    fetch("/language/" + lang).then((response) => response.json()).then((messages) => {
        $.i18n().load(messages, lang);
        $.i18n().locale = lang;
        $('body').i18n();
        document.documentElement.setAttribute('lang', lang);
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
    updateCss();
    updateLanguages();
});