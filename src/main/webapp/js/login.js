$(function () {
    $("#form").submit(function (e) {
        $.post({
            url: "/sqills/login",
            data: {
                username: this.username.value,
                password: this.password.value
            },
            success: function () {
                location.reload();
            },
            error: function (xhr) {
                alert(xhr.responseText);
            }
        });
        return false;
    });
});