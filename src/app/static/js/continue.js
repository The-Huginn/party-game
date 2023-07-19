function currentMove() {
    $.ajax({
        url: "/currentMove",
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
            console.log("error")
        },
        timeout: 5000
    });
}