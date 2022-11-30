$(function () {
	// Initialize localStorage
	(function () {
		var defaultRoom;
		defaultRoom = localStorage.getItem("defaultRoom");
		if (typeof defaultRoom === "undefined" || defaultRoom === null) {
			localStorage.setItem("defaultRoom", JSON.stringify({
				id: $("html").data("id"),
				name: $("html").data("name")
			}));
		}
	})();
	// Clock and upcoming schedule
	(function () {
		var clock, id, url, statusContainer, status, time, additionalInformation, timeout;
		clock = $("#clock");
		id = $("html").data("id");
		url = "/sqills/api/reservations?roomId=" + id;
		statusContainer = $("#statusContainer");
		status = $("#status");
		time = $("#time");
		additionalInformation = $("#additionalInformation");
		(function loop() {
			clock.html(new Date().toString().substring(16, 21));
			$.getJSON(url, function (data) {
				var object, toIntervalString, toAdditionalInformationString, reservation, currentTime, startTime, endTime;
				toIntervalString = function (startTime = new Date().getTime(), endTime = new Date().getTime()) {
					return new Date(startTime).toString().substring(16, 21) + " - " + new Date(endTime).toString().substring(16, 21);
				};
				toAdditionalInformationString = function (reservation) {
					var name, numberOfAttendees, additionalInformation;
					if (reservation.isVisible) {
						name = reservation.employee.firstName + " " + reservation.employee.lastName;
						numberOfAttendees = reservation.attendeesCount;
						additionalInformation = reservation.title + "<br>" + name;
						if (numberOfAttendees > 0) {
							additionalInformation += " and " + numberOfAttendees + (numberOfAttendees === 1 ? " other" : " others");
						}
						return additionalInformation;
					} else {
						return "Private";
					}
				};
				object = {
						a: "status-success",
						b: "Available",
						c: "",
						d: ""
				};
				if (data.length > 0) {
					reservation = data[0];
					currentTime = new Date().getTime();
					startTime = reservation.startTime;
					endTime = reservation.endTime;
					if (currentTime < startTime) {
						if (startTime - currentTime > 300000) {
							object.c = toIntervalString(startTime, endTime);
							object.d = toAdditionalInformationString(reservation);
						} else {
							// statusContainer.prop("class", "status-warning");
							object.a = "status-warning";
							// status.html("Occupied Soon");
							object.b = "Occupied Soon";
							// time.html(toIntervalString(startTime, endTime));
							object.c = toIntervalString(startTime, endTime);
							// additionalInformation.html(getAdditionalInformation(reservation));
							object.d = toAdditionalInformationString(reservation);
						}
					} else {
						if (endTime - currentTime > 300000) {
							object.a = "status-danger";
							object.b = "Occupied";
							object.c = toIntervalString(startTime, endTime);
							object.d = toAdditionalInformationString(reservation);
						} else {
							object.a = "status-warning";
							object.b = "Available Soon";
							object.c = toIntervalString(startTime, endTime);
							object.d = toAdditionalInformationString(reservation);
						}
					}
				}
				statusContainer.prop("class", object.a);
				status.html(object.b);
				time.html(object.c);
				additionalInformation.html(object.d);
			}).fail(function () {
				clearTimeout(timeout);
				console.log("Failed to get data from server. Please refresh page when server is online.");
			});
			timeout = setTimeout(loop, 1000);
		})();
	})();
	// More schedules
	(function () {
		$(".open").click(function () {
			var arrow, x, y, z;
			arrow = $("#arrow");
			$(".footerDrawer .content").slideToggle();
		    if (arrow.css( "transform" ) === "none"){
		        arrow.css("transform", "rotate(180deg)");
		    } else {
		        arrow.css("transform", "");
		    }
		    x = $("#makeReservationButton");
		    y = $("#other");
		    z = $("#status-box");
		    if (x.css("display") === "none") {
		      x.css("display", "block");
		      y.css("display", "block");
		      z.fadeIn();
		    } else {
		      x.css("display", "none");
		      y.css("display", "none");
		      z.fadeOut();
		    }
		});
	})();
	(function () {
		var regex, validateEmail, numberOfAdditionalAttendees;
		regex = /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/;
		validateEmail = function (s) {
			if (!regex.test($(s).val())) {
				$(s).addClass("invalid");
			} else {
				$(s).removeClass("invalid");
			}
		};
		$("#email").change(function () {
			validateEmail("#email");
		});
		numberOfAdditionalAttendees = 0;
		$("#addAdditionalAttendee").click(function () {
			var id, col;
			id = "additionalAttendee" + numberOfAdditionalAttendees + "Email";
			col = "<div class=\"col\"><div class=\"md-form\"><input type=\"email\" id=" + id + " class=\"form-control white-text invalid\"><label for=" + id + ">Additional attendee email</label></div></div>";
			if (numberOfAdditionalAttendees % 2 === 0) {
				$("#name").parents(".form-row").before("<div class=\"form-row\">" + col + "</div>");
			} else {
				$("#additionalAttendee" + (numberOfAdditionalAttendees - 1) + "Email").parents(".col").after(col);
			}
			$("#" + id).change(function () {
				validateEmail("#" + id);
			});
			numberOfAdditionalAttendees++;
		});
		$("#removeAdditionalAttendee").click(function () {
			var id;
			if (numberOfAdditionalAttendees === 0) {
				return;
			}
			id = "#additionalAttendee" + (numberOfAdditionalAttendees - 1) + "Email";
			if (numberOfAdditionalAttendees % 2 === 0) {
				$(id).parents(".col").remove();
			} else {
				$(id).parents(".form-row").remove();
			}
			numberOfAdditionalAttendees--;
		});
		$("#date").pickadate({
			clear: "",
			format: "mmm dd yyyy",
			min: new Date()
		});
		$("#startTime").change(function () {
			var value, endTime;
			value = $(this).val();
			endTime = $("#endTime").val();
			if (value === "") {
				$(this).addClass("invalid");
			} else {
				if (endTime !== "") {
					if (value >= endTime) {
						$(this).addClass("invalid");
						$("#endTime").addClass("invalid");
					} else {
						$(this).removeClass("invalid");
						$("#endTime").removeClass("invalid");
					}
				} else {
					$(this).removeClass("invalid");
				}
			}
		});
		$("#startTime").pickatime({
			afterDone: function () {
				$("#startTime").trigger("change");
			}
		});
		$("#endTime").change(function () {
			var value, startTime;
			value = $(this).val();
			startTime = $("#startTime").val();
			if (value === "") {
				$(this).addClass("invalid");
			} else {
				if (startTime !== "") {
					if (value <= startTime) {
						$(this).addClass("invalid");
						$("#startTime").addClass("invalid");
					} else {
						$(this).removeClass("invalid");
						$("#startTime").removeClass("invalid");
					}
				} else {
					$(this).removeClass("invalid");
				}
			}
		});
		$("#endTime").pickatime({
			afterDone: function () {
				$("#endTime").trigger("change");
			}
		});
		$("#makeReservation").click(function () {
			var date, additionalAttendees, i, reservation;
			if ($("#darkModalForm").find(".invalid").length > 0) {
				return;
			}
			date = $("#date").val();
			additionalAttendees = [];
			for (i = 0; i < numberOfAdditionalAttendees; i++) {
				additionalAttendees.push($("#additionalAttendee" + i + "Email").val());
			}
			reservation = {
					room: {
						id: parseInt($("#room").val())
					},
					startTime: new Date(date + " " + $("#startTime").val()).getTime(),
					endTime: new Date(date + " " + $("#endTime").val()).getTime(),
					employee: {
						email: $("#email").val()
					},
					title: $("#name").val(),
					attendees: additionalAttendees,
					isVisible: $("#visible").prop("checked")
			};
			$("#makeReservation").html("<span class=\"spinner-border spinner-border-sm mr-2\" role=\"status\" aria-hidden=\"true\"></span>");
			$.post({
				url: "/sqills/api/reservations",
				data: JSON.stringify(reservation),
				success: function (data) {
					console.log(data);
					$("#makeReservation").html("<strong class=\"black-text\">Make reservation</strong>");
					$("#darkModalForm").modal("hide");
					return;
				},
				error: function (data) {
					alert(data.responseText);
					$("#makeReservation").html("<strong class='black-text'>Make reservation</strong>>");
				},
				contentType: "application/json"
			});
		});
		$("#makeReservationButton").click(function () {
			$(this).html("<span class=\"spinner-border spinner-border-sm mr-2\" role=\"status\" aria-hidden=\"true\"></span>");
			$.getJSON("/sqills/api/rooms", function (data) {
				var select, options, i, room, name;
				select = $("#room");
				options = select.prop("options");
				for (i = 0; i < data.length; i++) {
					room = data[i];
					options.add(new Option(room.name, room.id));
				}
				$("#room").materialSelect();
				name = $("html").data("name");
				$(options).each(function (index, element) {
					if (name === $(element).text()) {
						select.siblings("ul").children("li:eq(" + index + ")").trigger("click");
						return false;
					}
				});
				select.siblings("input").addClass("white-text");
				$("#date").pickadate("picker").set("min", new Date());
				$("#date").val(new Date().toString().substring(4, 15)).trigger("change");
				$("#startTime").val(new Date(Math.ceil(new Date().getTime() / 1800000) * 1800000).toString().substring(16, 21)).trigger("change");
				$("#endTime").val(new Date(Math.ceil(new Date().getTime() / 1800000) * 1800000 + 1800000).toString().substring(16, 21)).trigger("change");
				$("#makeReservationButton").html("Make reservation");
				$("#darkModalForm").modal("show");
			});
		});
		$("#darkModalForm").on("hidden.bs.modal", function () {
			$("#darkModalForm").find(".invalid").removeClass("invalid");
			$("#email").val("").trigger("change");
			while (numberOfAdditionalAttendees > 0) {
				$("#removeAdditionalAttendee").trigger("click");
			}
			$("#name").val("").trigger("change");
			$("#visible").prop("checked", true);
			$("#room").parents(".md-form").html("<select class=\"mdb-select colorful-select dropdown-warning\" searchable=\"Search room..\" id=\"room\"><option value=\"\" disabled selected>Choose room</option></select>");
		});
	})();
	(function () {
		var opened, calendar;
		opened = false;
		$(".open").click(function () {
			if (!opened) {
				$(".content").height(function () {
					return Math.min($(document).height() * 0.75, 578);
				});
				$.getJSON("/sqills/api/reservations?roomId=" + $("html").data("id"), function (data) {
					var events = [];
					$(data).each(function (index, element) {
						events.push({
							title: element.title,
							start: element.startTime,
							end: element.endTime
						});
					});
					calendar = new FullCalendar.Calendar($("#calendar").get(0), {
						schedulerLicenseKey: 'GPL-My-Project-Is-Open-Source',
						contentHeight: 520,
						plugins: [ 'dayGrid', 'timeGrid' ],
						defaultView: 'timeGridDay',
						header: {
							left: 'prev,next today',
							center: 'title',
							right: 'dayGridMonth,timeGridWeek,timeGridDay'
						},
						allDaySlot: false,
						minTime: "08:00:00",
						maxTime: "17:00:00",
						events: events
					});
					calendar.render();
					opened = true;
				});
			} else {
				calendar.destroy();
				opened = false;
			}
		});
	})();
});