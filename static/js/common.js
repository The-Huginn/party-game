// Home button
function home() {
    loadHomePage($(".home-btn"));
}

// Confirm about page
function confirm() {
    loadHomePage($("#confirm"));
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
    
        $.ajax({
            url: "/login",
            type: "post",
            data: { gameID: name },
            success: function (response) {
                $("#content_placeholder").html(response);
                updateListeners();
            },
            error: function (xhr) {
                console.log("error")
            }
        });
    });
}

function updateListeners() {
    home();
    gameStart();
    confirm();
}

function updateCss(defaultCss=true) {
    
    $.ajax({
        url: "/css",
        type: "get",
        data: {defaultCss: defaultCss},

        success: function (response) {
            $("#background").attr("href", response);
        },
        error: function (xhr) {
            console.log("error" + xhr.responseType);
        }
    });

}

$(document).ready(function() {
    console.log("gere");
    updateListeners();
    updateCss();
});