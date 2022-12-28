// Home button
function homeAction() {
    loadHomePage($(".home-btn"));
}

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
        if (array.length < 1) {
            alert("Please select at least one category.")
            return false;
        }

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
    homeAction();
    gameStart();
    categoriesAction();
    confirmAction();
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
    updateListeners();
    updateCss();
});