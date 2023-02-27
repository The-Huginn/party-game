$("#game_form").submit(function (e) {
    e.preventDefault();

    var name = $("#gameName").val();

    gameModeSelection("post", name);
});