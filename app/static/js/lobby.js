$("#player_form").submit(function (e) {
    e.preventDefault();

    var name = $("#playerName").val();

    $.ajax({
        url: "/addPlayer",
        type: "post",
        data: { name: name },
        success: function (response) {
            $("#content_placeholder").html(response);
        },
        error: function (xhr) {
            //Do Something to handle error
            console.log("error")
        }
    });
});

$("#startGame").submit(function (e) {
    e.preventDefault();

    $.ajax({
        url: "/start",
        type: "post",

        success: function (response) {
            $("#content_placeholder").html(response);
            
            if ($("#startTimer").length > 0) {
                $("#startTimer").focus();
            } else {
                $("#nextMove").focus();
            }

            // We moved from lobby to game
            if ($("#nextMove").length > 0)
                updateCss(false);
        },
        error: function (xhr) {
            //Do Something to handle error
            console.log("error")
        }
    });
});

$(".removeButton").on('click', function (e) {
    console.log("clicled")
    e.preventDefault();

    var obj = $(this);
    var id = $(this).attr('id');
    
    $.ajax({
        url: "/removePlayer",
        type: "delete",
        data: { id: id },
        success: function (response) {
            $("#content_placeholder").html(response);
        },
        error: function (xhr) {
            //Do Something to handle error
            console.log("error")
        }
    });
});