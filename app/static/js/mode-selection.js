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