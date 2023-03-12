$("#submit_form").submit(function (e) {
    e.preventDefault();

    var name = $("#gameName").val();

    $.ajax({
        url: "/login",
        type: "post",
        data: { gameID: name },
        success: function (response) {
            $("#content_placeholder").html(response);
        },
        error: function (xhr) {
            //Do Something to handle error
            console.log("error")
        },
        timeout: 5000
    });
});