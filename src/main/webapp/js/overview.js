(function () {
	// Clock on top right corner
	(function loop() {
	    $("#clock").html(new Date().toString().substring(4, 21));
	    setTimeout(loop, 1000);
	})();
	// Display room buttons
	$.getJSON("/sqills/api/rooms", function (data) {
	    var rooms;
	    rooms = $("#rooms");
	    $(data).each(function () {
	        var id, name;
	        id = this.id;
	        name = this.name;
	        rooms.append("<div class=\"col-lg-3 col-md-4 col-sm-6 mb-4\" style=\"display: flex;\">" +
	        		"<button class=\"col-12 btn btn-success btn-lg\" id=\"room" + id + "\" value=\"" + id + "\">" +
					"<h1>" + name + "</h1><hr><div class=\"information\"><h2>Available</h2></div></button></div>");
		    $("#room" + id).click(function () {
		    	$(location).prop("href", "/sqills/room/" + id);
		    });
	    });
	    // Display reservations
	    (function loop() {
	        $.getJSON("/sqills/api/reservations/overview", function (data) {
	            $(data).each(function () {
	            	var buttonClass, information, currentTime, startTime, endTime;
	                buttonClass = "col-12 btn btn-success btn-lg";
	                information = "<h2>Available</h2>";
	                if (this.startTime !== 0 && this.endTime !== 0) {
	                	currentTime = new Date().getTime();
	                    startTime = this.startTime;
	                    endTime = this.endTime;
	                    if (currentTime < startTime) {
	                        if (startTime - currentTime <= 300000) {
	                            buttonClass = "col-12 btn btn-danger btn-lg";
	                            information = "<h2>Occupied Soon</h2>";
	                        }
	                    } else {
	                        if (endTime - currentTime > 300000) {
	                            buttonClass = "col-12 btn btn-warning btn-lg";
	                            information = "<h2>Occupied</h2>";
	                        } else {
	                            buttonClass = "col-12 btn btn-danger btn-lg";
	                            information = "<h2>Available Soon</h2>";
	                        }
	                    }
	                    startTime = new Date(startTime).toString().substring(16, 21);
	                    endTime = new Date(endTime).toString().substring(16, 21);
	                    information += "<h3>" + startTime + "-" + endTime + "</h3>";
	                    if (this.isVisible) {
	                    	information += "<hr><h4>" + this.title + "</h4>";
	                    	information += "<h5>" + this.employee.firstName + " " + this.employee.lastName;
	                    	if (this.attendeesCount > 0) {
								information += "<br>and " + this.attendeesCount + " other" + (this.attendeesCount > 1 ? "s" : "") + "</h5>";
							}
	                    } else {
	                    	information += "<hr><h4>Private Meeting</h4>";
	                    }
	                }
	                $("button#room" + this.room.id).prop("class", buttonClass).children(".information").html(information);
	            });
	        });
	        setTimeout(loop, 1000);
	    })();
	    (function () {
	    	// Configure modal
	    	$("#dropdown").change(function () {
	    		var value;
	    		value = $(this).val();
	    		if (typeof value !== "undefined" && value !== null) {
	    			$("#submitbtn").prop("disabled", false);
	    		} else {
	    			$("#submitbtn").prop("disabled", true);
	    		}
	    	});
	    	$("#submitbtn").click(function () {
	    		var options, option;
	    		options = $("#dropdown").prop("options");
	    		option = $(options[options.selectedIndex]);
	    		localStorage.setItem("defaultRoom", JSON.stringify({
	    			id: option.val(),
	    			name: option.text()
	    		}));
	    	});
	    	$("#exampleModal").on("shown.bs.modal", function () {
	    		$.getJSON("/sqills/api/rooms", function (data) {
	    			var dropdown, options, i, room;
	    			dropdown = $("#dropdown");
	    			options = dropdown.prop("options");
	    			for (i = 0; i < data.length; i++) {
	    				room = data[i];
	    				options.add(new Option(room.name, room.id));
	    			}
	    			dropdown.change();
	    		});
	    	}).on("hidden.bs.modal", function () {
	    		var defaultRoom;
	    		$("#dropdown").html("<option value=\"\" disabled selected>Choose a default room</option>");
		    	defaultRoom = localStorage.getItem("defaultRoom");
		    	if (typeof defaultRoom === "undefined" || defaultRoom === null) {
		    		$("#exampleModal").modal("show");
		    	} else {
		    		defaultRoom = JSON.parse(defaultRoom);
		    		$("#standardRoom").html("Back to room: <b>" + defaultRoom.name + "</b>");
				    $("#standardRoom").click(function () {
				    	$(location).prop("href", "./room/" + defaultRoom.id);
				    });
		    	}
	    	})
	    	$("#exampleModal").trigger("hidden.bs.modal");
	    })();
	    (function () {
	    	// Scrolls the rooms to the other side and rotates the arrow on click
		    $("#arrowright").data("right", true).click(function () {
		    	var scrollable, maxLeftScroll;
		    	scrollable = $(".scrollable");
		    	var maxLeftScroll = scrollable.prop("scrollWidth") - scrollable.prop("clientWidth");
		    	if ($(this).data("right")) {
		    		scrollable.animate({
		    			scrollLeft: maxLeftScroll
		    		}, 500);
		    	} else {
		    		scrollable.animate({
		    			scrollLeft: 0
		    		}, 500);
		    	}
		    });
		    $(".scrollable").scroll(function () {
		    	var arrowright;
		    	arrowright = $("#arrowright");
		    	if ($(this).prop("scrollLeft") > ($(this).prop("scrollWidth") - $(this).prop("clientWidth")) / 2) {
		    		if (arrowright.data("right")) {
		    			arrowright.css("transform", "rotate(180deg)");
		    			arrowright.data("right", false);
		    		}
		    	} else {
		    		if (!arrowright.data("right")) {
		    			arrowright.css("transform", "");
		    			arrowright.data("right", true);
		    		}
		    	}
		    });
	    })();
	});
})();