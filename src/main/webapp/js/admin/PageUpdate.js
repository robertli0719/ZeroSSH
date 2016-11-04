/* 
 * Copyright 2016 Robert Li.
 * Released under the MIT license
 * https://opensource.org/licenses/MIT
 */

/* global tinyMCE */

function update(id, title, description, content) {
    $.post("PageUpdate!update", {
        id: id,
        title: title,
        description: description,
        content: content
    }, function (val) {
        var result = JSON.parse(val);
        if (result["result"] === 'success') {
            window.location = 'Page';
        } else {
            var errorStrring = result["errorString"];
            $(".alert").remove();
            var alert_div = $('<div>').addClass("alert alert-danger alert-dismissible").html("<strong>Error!</strong> " + errorStrring).insertAfter(".container hr");
            $('<button>').addClass("close").attr("type", "button").attr("data-dismiss", "alert").attr("aria-label", "Close").html("<span aria-hidden=\"true\">&times;</span>").appendTo(alert_div);
            console.log(result);
        }
    });
}

$(function () {

    $(document).on("click", "[data-cmd=update]", function () {
        var id = $("input[name=id]").val();
        var title = $("input[name=title]").val();
        var description = $("input[name=description]").val();
        var content = tinyMCE.activeEditor.getContent();
        update(id, title, description, content);
    });
});
