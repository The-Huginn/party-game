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
    });
});