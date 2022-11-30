// Returns to default room after 3 minutes
$(function () {
	(function (seconds) {
		var defaultRoom;
		defaultRoom = localStorage.getItem("defaultRoom");
		if (typeof defaultRoom !== "undefined" && defaultRoom !== null) {
			defaultRoom = JSON.parse(defaultRoom);
			if (defaultRoom.id != $("html").data("id")) {
				var reset, timeout;
				reset = function() {
            		clearInterval(timeout);
            		timeout = setTimeout(function() {
            			$(location).prop("href", "/sqills/room/" + defaultRoom.id);
            		}, seconds * 1000);
        		};
        		$(document).click(function () {
        			reset();
        		}).keypress(function () {
        			reset();
        		});
        		reset();
			}
		}
	})(180);
});