$(function () {
	(function loadReservations() {
		$("#addButton").show();
		$("#editButton").show();
		$("#deleteButton").show();
		$("#dataTable").html("<table id=\"dt-select\" class=\"table table-striped table-bordered\" style=\"width: 100%\">" +
			"<thead><tr id=\"thead\"></tr></thead>" +
			"<tfoot><tr id=\"tfoot\"></tr></tfoot>" +
			"</table>");
		$("#thead").append("<th>Room</th>").append("<th>Start time</th>").append("<th>End time</th>")
			.append("<th>Employee</th>").append("<th>Attendees</th>").append("<th>Title</th>")
			.append("<th>Visible</th>");
		$("#tfoot").append("<th>Room</th>").append("<th>Start time</th>").append("<th>End time</th>")
			.append("<th>Employee</th>").append("<th>Attendees</th>").append("<th>Title</th>")
			.append("<th>Visible</th>");
		$.get({
			url: "/sqills/administrator/reservations",
			success: function (data) {
				var dataSet;
				dataSet = [];
				$.each(data, function (index, value) {
					value.startTime = new Date(value.startTime).toString().substring(4, 21);
					value.endTime = new Date(value.endTime).toString().substring(4, 21);
					dataSet.push(value);
				});
				$("#dt-select").DataTable({
					columns: [
						{
							data: "room.name",
							title: "Room"
						}, {
							data: "startTime",
							title: "Start time",
							type: "date"
						}, {
							data: "endTime",
							title: "End time",
							type: "date"
						}, {
							data: "employee.email",
							title: "Employee"
						}, {
							data: "attendees",
							orderable: false,
							title: "Attendees"
						}, {
							data: "title",
							orderable: false,
							title: "Title"
						}, {
							data: "isVisible",
							title: "Visible"
						}
					],
					data: dataSet,
					dom: "ftipr",
					order: [[1, "asc"]],
					select: {
						blurable: false,
						info: false,
						style: "single"
					}
				}).on("deselect", function () {
					$("#editButton").prop("disabled", true);
					$("#deleteButton").prop("disabled", true);
				}).on("select", function () {
					$("#editButton").prop("disabled", false);
					$("#deleteButton").prop("disabled", false);
				});
			},
			error: function (xhr) {
				if (xhr.status === 401) {
					alert("This account was logged in from another device. You will be redirected to the login page.");
					location.reload();
				} else {
					console.log(xhr);
				}
			}
		});
		$("#navReservations").click(function () {
			loadReservations();
		});
	})();
	(function () {
		$("#navRooms").click(function () {
			$("#addButton").hide();
			$("#editButton").hide();
			$("#deleteButton").hide();
			$("#dataTable").html("<table id=\"dt-select\" class=\"table table-striped table-bordered\" style=\"width: 100%\">" +
				"<thead><tr id=\"thead\"></tr></thead>" +
				"<tfoot><tr id=\"tfoot\"></tr></tfoot>" +
				"</table>");
			$("#thead").append("<th>Id</th>").append("<th>Name</th>");
			$("#tfoot").append("<th>Id</th>").append("<th>Name</th>");
			$.get({
				url: "/sqills/administrator/rooms",
				success: function (data) {
					$("#dt-select").DataTable({
						columns: [
							{
								data: "id",
								title: "Id"
							}, {
								data: "name",
								title: "Name"
							}
						],
						data: data,
						dom: "ftipr",
						order: [[0, "asc"]],
						select: {
							blurable: false,
							info: false,
							style: "single"
						}
					}).on("deselect", function () {
						$("#editButton").prop("disabled", true);
						$("#deleteButton").prop("disabled", true);
					}).on("select", function () {
						$("#editButton").prop("disabled", false);
						$("#deleteButton").prop("disabled", false);
					});
				},
				error: function (xhr) {
					if (xhr.status === 401) {
						alert("This account was logged in from another device. You will be redirected to the login page.");
						location.reload();
					} else {
						console.log(xhr);
					}
				}
			});
		});
	})();
	(function () {
		$("#navEmployees").click(function () {
			$("#addButton").hide();
			$("#editButton").hide();
			$("#deleteButton").hide();
			$("#dataTable").html("<table id=\"dt-select\" class=\"table table-striped table-bordered\" style=\"width: 100%\">" +
				"<thead><tr id=\"thead\"></tr></thead>" +
				"<tfoot><tr id=\"tfoot\"></tr></tfoot>" +
				"</table>");
			$("#thead").append("<th>Id</th>").append("<th>Email</th>").append("<th>First name</th>")
				.append("<th>Last name</th>");
			$("#tfoot").append("<th>Id</th>").append("<th>Email</th>").append("<th>First name</th>")
				.append("<th>Last name</th>");
			$.get({
				url: "/sqills/administrator/employees",
				success: function (data) {
					$("#dt-select").DataTable({
						columns: [
							{
								data: "id",
								title: "Id"
							}, {
								data: "email",
								title: "Email"
							}, {
								data: "firstName",
								title: "First name"
							}, {
								data: "lastName",
								title: "Last name"
							}
						],
						data: data,
						dom: "ftipr",
						order: [[0, "asc"]],
						select: {
							blurable: false,
							info: false,
							style: "single"
						}
					}).on("deselect", function () {
						$("#editButton").prop("disabled", true);
						$("#deleteButton").prop("disabled", true);
					}).on("select", function () {
						$("#editButton").prop("disabled", false);
						$("#deleteButton").prop("disabled", false);
					});
				},
				error: function (xhr) {
					if (xhr.status === 401) {
						alert("This account was logged in from another device. You will be redirected to the login page.");
						location.reload();
					} else {
						console.log(xhr);
					}
				}
			});
		});
	})();
	(function () {
		$("#navLogout").click(function () {
			$.ajax("/sqills/administrator", {
				method: "DELETE",
				success: function () {
					$(location).prop("href", "/sqills");
				}
			});
		})
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
		}
		$("#email").change(function () {
			validateEmail("#email");
		});
		numberOfAdditionalAttendees = 0;
		$("#addAdditionalAttendee").click(function () {
			var id, col;
			id = "additionalAttendee" + numberOfAdditionalAttendees + "Email";
			col = "<div class=\"col\"><div class=\"md-form\"><input type=\"email\" id=" + id + " class=\"form-control invalid\"><label for=" + id + ">Additional attendee email</label></div></div>";
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
				$("#startTime").change();
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
				$("#endTime").change();
			}
		});
		$("#addReservationButton").click(function () {
			var date, additionalAttendees, i, reservation;
			$("#modalRegisterForm").find("input").change();
			if ($("#modalRegisterForm").find(".invalid").length > 0 || $("#room").val() === null) {
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
					attendees: additionalAttendees,
					title: $("#name").val(),
					isVisible: $("#visible").prop("checked")
			};
			$("#addReservationButton").html("<span class=\"spinner-border spinner-border-sm mr-2\" role=\"status\" aria-hidden=\"true\"></span>");
			$.post({
				url: "/sqills/administrator/reservations",
				data: JSON.stringify(reservation),
				success: function (data) {
					var reservation;
					$("#addReservationButton").html("<strong class=\"black-text\">Add reservation</strong>");
					$("#modalRegisterForm").modal("hide");
					reservation = data;
					reservation.startTime = new Date(reservation.startTime).toString().substring(4, 21);
					reservation.endTime = new Date(reservation.endTime).toString().substring(4, 21);
					$("#dt-select").DataTable().row.add(reservation).draw();
				},
				error: function (xhr) {
					$("#addReservationButton").html("<strong class=\"black-text\">Add reservation</strong>");
					if (xhr.status === 401) {
						alert("This account was logged in from another device. You will be redirected to the login page.");
						location.reload();
					} else {
						alert(xhr.responseText);
					}
				},
				contentType: "application/json"
			});
		});
		$("#modalRegisterForm").on("hidden.bs.modal", function () {
			$("#modalRegisterForm").find(".invalid").removeClass("invalid");
			$("#email").val("").change();
			while (numberOfAdditionalAttendees > 0) {
				$("#removeAdditionalAttendee").click();
			}
			$("#name").val("").change();
			$("#visible").prop("checked", true);
			$("#room").parents(".md-form").html("<select class=\"mdb-select colorful-select dropdown-warning\" searchable=\"Search room..\" id=\"room\"><option value=\"\" disabled selected>Choose room</option></select>");
		});
		$("#addButton").click(function () {
			$(this).html("<span class=\"spinner-border spinner-border-sm mr-2\" role=\"status\" aria-hidden=\"true\"></span>");
			$.getJSON("/sqills/api/rooms", function (data) {
				var select, options, i, room;
				select = $("#room");
				options = select.prop("options");
				for (i = 0; i < data.length; i++) {
					room = data[i];
					options.add(new Option(room.name, room.id));
				}
				$("#room").materialSelect();
				$("#date").pickadate("picker").set("min", new Date());
				$("#date").val(new Date().toString().substring(4, 15)).change();
				$("#startTime").val(new Date(Math.ceil(new Date().getTime() / 1800000) * 1800000).toString().substring(16, 21)).change();
				$("#endTime").val(new Date(Math.ceil(new Date().getTime() / 1800000) * 1800000 + 1800000).toString().substring(16, 21)).change();
				$("#addButton").html("Add<i class=\"fas fa-plus-square ml-1\"></i>");
				$("#modalRegisterForm").modal("show");
			});
		});
	})();
	(function () {
		$("#date2").pickadate({
			clear: "",
			format: "mmm dd yyyy",
			min: new Date()
		});
		$("#date2").val(new Date().toString().substring(4, 15)).change();
		$("#startTime2").val(new Date(Math.ceil(new Date().getTime() / 1800000) * 1800000).toString().substring(16, 21)).change().change(function () {
			var value, endTime;
			value = $(this).val();
			endTime = $("#endTime2").val();
			if (value === "") {
				$(this).addClass("invalid");
			} else {
				if (endTime !== "") {
					if (value >= endTime) {
						$(this).addClass("invalid");
						$("#endTime2").addClass("invalid");
					} else {
						$(this).removeClass("invalid");
						$("#endTime2").removeClass("invalid");
					}
				} else {
					$(this).removeClass("invalid");
				}
			}
		});
		$("#startTime2").pickatime({
			afterDone: function () {
				$("#startTime2").change();
			}
		});
		$("#endTime2").val(new Date(Math.ceil(new Date().getTime() / 1800000) * 1800000 + 1800000).toString().substring(16, 21)).change().change(function () {
			var value, startTime;
			value = $(this).val();
			startTime = $("#startTime2").val();
			if (value === "") {
				$(this).addClass("invalid");
			} else {
				if (startTime !== "") {
					if (value <= startTime) {
						$(this).addClass("invalid");
						$("#startTime2").addClass("invalid");
					} else {
						$(this).removeClass("invalid");
						$("#startTime2").removeClass("invalid");
					}
				} else {
					$(this).removeClass("invalid");
				}
			}
		});
		$("#endTime2").pickatime({
			afterDone: function () {
				$("#endTime2").change();
			}
		});
		$("#editReservation").click(function () {
			var date, reservation;
			$("#modalEditReservation").find("input:not([readonly])").change();
			if ($("#modalEditReservation").find(".invalid").length > 0) {
				return;
			}
			date = $("#date2").val();
			reservation = {
					id: $("#dt-select").DataTable().row().data().id,
					room: {
						id: parseInt($("#room2").val())
					},
					startTime: new Date(date + " " + $("#startTime2").val()).getTime(),
					endTime: new Date(date + " " + $("#endTime2").val()).getTime(),
					attendees: $("#dt-select").DataTable().row().data().attendees,
					title: $("#name2").val(),
					isVisible: $("#visible2").prop("checked")
			};
			$("#editReservation").html("<span class=\"spinner-border spinner-border-sm mr-2\" role=\"status\" aria-hidden=\"true\"></span>");
			$.ajax("/sqills/administrator/reservations", {
				contentType: "application/json",
				data: JSON.stringify(reservation),
				method: "PUT",
				success: function (data) {
					var reservation, row;
					$("#editReservation").html("<strong class=\"black-text\">Edit reservation</strong>");
					reservation = data;
					reservation.startTime = new Date(reservation.startTime).toString().substring(4, 21);
					reservation.endTime = new Date(reservation.endTime).toString().substring(4, 21);
					row = $("#dt-select").DataTable().row(".selected");
					row.data(reservation).draw();
					row.deselect();
					$("#modalEditReservation").modal("hide");
				},
				error: function (xhr) {
					$("#editReservation").html("<strong class=\"black-text\">Edit reservation</strong>");
					if (xhr.status === 401) {
						alert("This account was logged in from another device. You will be redirected to the login page.");
						location.reload();
					} else {
						alert(xhr.responseText);
					}
				}
			});
		});
		$("#modalEditReservation").on("hidden.bs.modal", function () {
			$("#modalEditReservation").find(".invalid").removeClass("invalid");
			$("#email2").val("").change();
			$("#name2").val("").change();
			$("#visible2").prop("checked", true);
			$("#room2").parents(".md-form").html("<select class=\"mdb-select colorful-select dropdown-warning\" searchable=\"Search room..\" id=\"room2\"><option value=\"\" disabled selected>Choose room</option></select>");
		});
		$("#editButton").click(function () {
			$(this).html("<span class=\"spinner-border spinner-border-sm mr-2\" role=\"status\" aria-hidden=\"true\"></span>");
			$.getJSON("/sqills/api/rooms", function (data) {
				var select, options, i, room, reservation;
				select = $("#room2");
				options = select.prop("options");
				for (i = 0; i < data.length; i++) {
					room = data[i];
					options.add(new Option(room.name, room.id));
				}
				$("#room2").materialSelect();
				reservation = $("#dt-select").DataTable().row(".selected").data();
				$(options).each(function (index, element) {
					if (reservation.room.id === parseInt($(element).val())) {
						select.siblings("ul").children("li:eq(" + index + ")").click();
						return false;
					}
				});
				$("#email2").val(reservation.employee.email).change();
				$("#name2").val(reservation.title).change();
				$("#visible2").prop("checked", reservation.isVisible).change();
				$("#date2").val(new Date(reservation.startTime).toString().substring(4, 15)).change();
				$("#startTime2").val(new Date(reservation.startTime).toString().substring(16, 21)).change();
				$("#endTime2").val(new Date(reservation.endTime).toString().substring(16, 21)).change();
				$("#editButton").html("Edit<i class=\"fas fa-pencil-square-o ml-1\"></i>");
				$("#modalEditReservation").modal("show");
			});
		});
	})();
	(function () {
		$("#btnYes").click(function () {
			var row;
			row = $("#dt-select").DataTable().row(".selected");
			$.ajax("/sqills/administrator/reservations?id=" + row.data().id, {
				method: "DELETE",
				success: function () {
					row.deselect();
					row.remove().draw();
				},
				error: function () {
					alert("This account was logged in from another device. You will be redirected to the login page.");
					location.reload();
				}
			});
		});
		$("#deleteButton").click(function () {
			$("#modalDelete").modal("show");
		});
	})();
});