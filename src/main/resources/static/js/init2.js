var reportConfigUrl = '/bahmni_config/openmrs/apps/reports/reports.json';
var downloadUrl = '/dhis-integration/download?name=NAME&year=YEAR&month=MONTH&isImam=IS_IMAM&isFamily=IS_FAMILY';
var submitUrl = '/dhis-integration/submit-to-dhis';
var getSchedulesUrl = '/dhis-integration/get-schedules';
var createScheduleUrl = '/dhis-integration/create-schedule';
var deleteScheduleUrl='/dhis-integration/delete-schedule';
var disenScheduleUrl='/dhis-integration/disable-enable-schedule';
var submitUrlAtr = '/dhis-integration/submit-to-dhis-atr';
var loginRedirectUrl = '/bahmni/home/index.html#/login?showLoginMessage&from=';
var NUTRITION_PROGRAM = '03-2 Nutrition Acute Malnutrition';
var FAMILYPLANNING_PROGRAM = '07 Family Planning Program';
var logUrl = '/dhis-integration/log';
var fiscalYearReportUrl = '/dhis-integration/download/fiscal-year-report?name=NAME&startYear=START_YEAR&startMonth=START_MONTH&endYear=END_YEAR&endMonth=END_MONTH&isImam=IS_IMAM';
var supportedStartDate = 2033;
var supportedEndDate = 2008;
var approximateNepaliYear = (new Date()).getFullYear() + 56;
var spinner = spinner || {};

var hasReportingPrivilege = true;

$(document).ready(
		function() {

			initTabs();
			// Activate tooltip
			//$('[data-toggle="tooltip"]').tooltip();
			//localStorage.setItem('tab_id', 'hello-world';
			//var last_id = localStorage.getItem('tab_id');
			/*alert('the last active tab was '+last_id);
			if (last_id) {
				$('ul.nav li').removeClass('current');
				$('.tab-pane').removeClass('current');
				$(".nav li").eq(Number(last_id.match(/\d+/)[0])-1).addClass('current');
				$("#" + last_id).addClass('current');
			}
			$('ul.nav li').click(function() {
				var tab_id = $(this).attr('href');
				alert('cliked tab '+tab_id);
				$('ul.nav li').removeClass('current');
				$('.tab-pane').removeClass('current');

				$(this).addClass('active');
				$("#" + tab_id).addClass('current');
				localStorage.setItem('tab_id', tab_id);
			});*/

			// Select/Deselect checkboxes
			var checkbox = $('table tbody input[type="checkbox"]');
			$("#selectAll").click(function(){
				if(this.checked){
					checkbox.each(function(){
						this.checked = true;                        
					});
				} else{
					checkbox.each(function(){
						this.checked = false;                        
					});
				} 
			});
			checkbox.click(function(){
				if(!this.checked){
					$("#selectAll").prop("checked", false);
				}
			});
			
			initSelects();
			renderDHISSchedules();
			
		});


$(function() {
  
			$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
			  localStorage.setItem('lastTab', $(this).attr('href'));
			  alert("Saved active tab id "+ $(this).attr('href'));
			});
			var lastTab = localStorage.getItem('lastTab');
			
			if (lastTab) {
			  alert("Saved active tab id "+ lastTab);
			  $('[href="' + lastTab + '"]').tab('show');
			}
			
});

//populate list of schedules from db
function renderDHISSchedules(){
	getDHISSchedules().then(function(data){
		console.log('[render hmis program schedules]');
		console.log(data);
		//alert(data);
		var weeklySchedulesTable = document.getElementById('weekly-program-schedules');
		var monthlySchedulesTable = document.getElementById('monthly-program-schedules');
		var quarterlySchedulesTable = document.getElementById('quarterly-program-schedules');
		var schedules=JSON.parse(data);
		schedules.forEach(function(object) {
			console.log(object);
			var tr = document.createElement('tr');
			var tempHTML ="<td>"+"<span class='custom-checkbox'>"+
							"<input class='selectSchedule' type='checkbox' id='checkbox1' name='options[]' value='"+object.id+"'/>"+
							"<label for='checkbox1'></label>"+"</span></td>" +
							'<td>' + object.programName + '</td>' +
							'<td>' + object.lastRun + '</td>' +
							'<td>' + object.status + '</td>';
			if(object.frequency=="weekly"){
				tr.innerHTML =tempHTML+
							"<td>"+
							"<label class='switch'><input type='checkbox' id='"+object.id+"' onclick='disenSchedule(this.id)'><span class='slider round'></span></label>"+
							"</td>";
				weeklySchedulesTable.appendChild(tr);
			}
			else if(object.frequency=="monthly"){
				tr.innerHTML =tempHTML+
							"<td>"+
							"<label class='switch'><input type='checkbox' id='"+object.id+"' onclick='disenSchedule(this.id)'><span class='slider round'></span></label>"+
							"</td>";
				monthlySchedulesTable.appendChild(tr);
			}
			else if(object.frequency=="quarterly"){
				tr.innerHTML =tempHTML+
							"<td>"+
							"<label class='switch'><input type='checkbox' id='"+object.id+"' onclick='disenSchedule(this.id)'><span class='slider round'></span></label>"+
							"</td>";
				quarterlySchedulesTable.appendChild(tr);
			}
			document.getElementById(object.id).checked= object.enabled;
		});
	});
}

function disenSchedule(toggled_id){
	var scheduleId=toggled_id;
	var enabled = document.getElementById(toggled_id).checked ? 'true' : 'false';
	console.log('Clicked toggle switch element is '+toggled_id);
	console.log('Clicked toggle switch element value is '+document.getElementById(toggled_id).value);
	console.log('Clicked toggle switch element value is '+enabled);

	console.log('Clicked schedule to enable/disable is '+toggled_id);

	var parameters = {
		scheduleId : scheduleId,
		enabled:enabled
	};
	
	var submitTo = disenScheduleUrl;
	return $.get(submitTo,parameters).done(function(data) {
		console.log('[Server result for disenSchedule()]');
		console.log(data);

		
	}).fail(function(response) {
		console.log('[Operation disenSchedule() failed]');
	});

}

//populate list of DHIS-enabled hmis programs into select element
function initSelects(){
	var isYearlyReport = false;
	var canSubmitReport = hasReportingPrivilege;
	getContent(isYearlyReport, canSubmitReport).then(
				function(content) {
					console.log('[populate option element]');
					console.log(content.programs);
					let weekly_dropdown = $('#weekly-progname');
					let monthly_dropdown = $('#monthly-progname');
					let quarterly_dropdown = $('#quarterly-progname');
					weekly_dropdown.empty();
					monthly_dropdown.empty();
					quarterly_dropdown.empty();
					weekly_dropdown.append('<option selected="true" disabled>Choose Program</option>');
					monthly_dropdown.append('<option selected="true" disabled>Choose Program</option>');
					quarterly_dropdown.append('<option selected="true" disabled>Choose Program</option>');
					weekly_dropdown.prop('selectedIndex', 0);
					monthly_dropdown.prop('selectedIndex', 0);
					quarterly_dropdown.prop('selectedIndex', 0);
					$.each(content.programs, function (key, entry) {
						weekly_dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
						monthly_dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
						quarterly_dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
					});
				});
}

function initTabs() {
	$("#tabs").tabs();
}

function getDHISPrograms() {
	return $.getJSON(reportConfigUrl).then(function(reportConfigs) {
		var DHISPrograms = [];
		Object.keys(reportConfigs).forEach(function(reportKey) {
			if (reportConfigs[reportKey].DHISProgram) {
				reportConfigs[reportKey].index = DHISPrograms.length;
				DHISPrograms.push(reportConfigs[reportKey]);
			}
		});
		return DHISPrograms;
	});
}


function getDHISSchedules() {
	return $.get(getSchedulesUrl).done(function(data) {
		console.log('[Get DHIS schedules]');
		//console.log(data);
		
	}).fail(function(response) {
		
	});
}

function removeAllRowsContainingCheckedCheckbox(table) {
    for (var rowi= table.rows.length; rowi-->0;) {
        var row= table.rows[rowi];
        var inputs= row.getElementsByTagName('input');
        for (var inputi= inputs.length; inputi-->0;) {
            var input= inputs[inputi];

            if (input.type==='checkbox' && input.checked && input.className =='selectSchedule') {
                row.parentNode.removeChild(row);
                break;
            }
        }
    }
}

function deleteDHISSchedule(clicked_id){

	var scheduleIds=[];
	$.each($(".selectSchedule:checked"), function(){            
		scheduleIds.push($(this).val());
		console.log('ID of clicked schedule to delete is '+$(this).val());
		var checkbox=this;
		var row_index=checkbox.parentElement.parentElement.rowIndex;
		console.log('Row index of schedule to delete is '+ row_index);
		if(clicked_id == 'deleteWeeklySchedulebtn'){
			//var row_index=checkbox.parentElement.parentElement.rowIndex;
			//document.getElementById("weekly-program-schedules").deleteRow(row_index);
			removeAllRowsContainingCheckedCheckbox(document.getElementById("weekly-program-schedules"));
		}
		else if(clicked_id == 'deleteMonthlySchedulebtn'){
			//var row_index=checkbox.parentElement.parentElement.rowIndex;
			//document.getElementById("monthly-program-schedules").deleteRow(row_index);
			removeAllRowsContainingCheckedCheckbox(document.getElementById("monthly-program-schedules"));
		}
		else if(clicked_id == 'deleteQuarterlySchedulebtn'){
			//var row_index=checkbox.parentElement.parentElement.rowIndex;
			//document.getElementById("quarterly-program-schedules").deleteRow(row_index);
			removeAllRowsContainingCheckedCheckbox(document.getElementById("quarterly-program-schedules"));
		}

	});

	console.log('Clicked schedule to delete is '+scheduleIds);

	var parameters = {
		scheduleIds : scheduleIds
	};
	
	var submitTo = deleteScheduleUrl;
	return $.get(submitTo,parameters).done(function(data) {
		//data = JSON.stringify(data);
		console.log('[Server result for deleteDHISSchedule()]');
		console.log(data);

		window.location.reload();

		
	}).fail(function(response) {
		console.log('[Operation deletDHISSchedule() failed]');
	});

}

function createDHISSchedule(clicked_id, frequency){
	console.log('Creating new schedule, clicked_id='+clicked_id+' frequency='+frequency);
	var programName;
	var scheduleFrequency=frequency;
	var scheduleTime;
	var weeklySchedulesTable = document.getElementById('weekly-program-schedules');
	var monthlySchedulesTable = document.getElementById('monthly-program-schedules');
	var quarterlySchedulesTable = document.getElementById('quarterly-program-schedules');
	var tr = document.createElement('tr');
	var tempHTML ="<td>"+"<span class='custom-checkbox'>"+
				  "<input type='checkbox' class='selectSchedule' id='checkbox1' name='options[]' value='1'/>"+
				  "<label for='checkbox1'></label>"+"</span></td>";

	if(clicked_id == 'addWeeklySchedulebtn'){
		programName=document.getElementById('weekly-progname').value;
		scheduleTime=document.getElementById('weekly-time').value;
	}
	else if(clicked_id == 'addMonthlySchedulebtn'){
		programName=document.getElementById('monthly-progname').value;
		scheduleTime=document.getElementById('monthly-time').value;
	}
	else if(clicked_id == 'addQuarterlySchedulebtn'){
		programName=document.getElementById('quarterly-progname').value;
		scheduleTime=document.getElementById('quarterly-time').value;
	}

	if(frequency=='weekly'){
		tr.innerHTML =tempHTML+
					  '<td>' + programName + '</td>' +
					  '<td>' + '-' + '</td>' +
					  '<td>' + 'Ready' + '</td>'+
					  "<td>"+
					  "<label class='switch'><input type='checkbox' checked><span class='slider round'></span></label>"+
					  "</td>";
		weeklySchedulesTable.appendChild(tr);
	}
	else if(frequency=='monthly'){
		tr.innerHTML =tempHTML+
					  '<td>' + programName + '</td>' +
					  '<td>' + '-' + '</td>' +
					  '<td>' + 'Ready' + '</td>'+
					  "<td>"+
					  "<label class='switch'><input type='checkbox' checked><span class='slider round'></span></label>"+
					  "</td>";
		monthlySchedulesTable.appendChild(tr);
	}
	else if(frequency=='quarterly'){
		tr.innerHTML =tempHTML+
					  '<td>' + programName + '</td>' +
					  '<td>' + '-' + '</td>' +
					  '<td>' + 'Ready' + '</td>'+
					  "<td>"+
					  "<label class='switch'><input type='checkbox' checked><span class='slider round'></span></label>"+
					  "</td>";
		quarterlySchedulesTable.appendChild(tr);
	}

	var parameters = {
		programName : programName,
		scheduleFrequency : scheduleFrequency,
		scheduleTime : scheduleTime
	};

	var submitTo = createScheduleUrl;
	return $.get(submitTo,parameters).done(function(data) {
		//data = JSON.stringify(data);
		console.log('[Server result for submitNewSchedule()]');
		console.log("URL:"+submitTo);
		console.log(data);
		if(data==true){

		}
		else{
			
		}
		window.location.reload();
		
	}).fail(function(response) {
		console.log('[Operation submitNewSchedule() failed]');
	});

}


function element(name, index) {
	var id = name + '-' + index;
	return $('[id="' + id + '"]');
}

function enableBtn(btn) {
	return btn.attr('disabled', false).removeClass('btn-disabled');
}

function disableBtn(btn) {
	return btn.attr('disabled', true).addClass('btn-disabled');
}

function disableAllSubmitBtns() {
	disableBtn($("[id*='submit-']"));
}

function registerOnchangeOnComment() {
	disableAllSubmitBtns();
	$("[id*='comment-']").on('change keyup paste', function(event) {
		var index = $(event.target).attr('index');
		if ($(event.target).val().trim() != "") {
			enableBtn(element('submit', index));
		} else {
			disableBtn(element('submit', index));
		}
	});
}

function getLogStatus() {
	$('#programs .month-selector').each(function(index) {
		getStatus(index);
	});
}

function getDHISPrograms() {
	return $.getJSON(reportConfigUrl).then(function(reportConfigs) {
		var DHISPrograms = [];
		Object.keys(reportConfigs).forEach(function(reportKey) {
			if (reportConfigs[reportKey].DHISProgram) {
				reportConfigs[reportKey].index = DHISPrograms.length;
				DHISPrograms.push(reportConfigs[reportKey]);
			}
		});
		return DHISPrograms;
	});
}

function getContent(isYearlyReport, canSubmitReport) {
	return getDHISPrograms().then(function(programs) {
		if (isYearlyReport) {
			return {
				programs : programs,
				isYearlyReport : isYearlyReport,
				canSubmitReport : canSubmitReport
			};
		} else {
			return {
				programs : programs,
				isYearlyReport : isYearlyReport,
				canSubmitReport : canSubmitReport
			};
		}
	});
}

