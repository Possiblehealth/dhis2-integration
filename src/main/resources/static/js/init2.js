var reportConfigUrl = '/bahmni_config/openmrs/apps/reports/reports.json';
var downloadUrl = '/dhis-integration/download?name=NAME&year=YEAR&month=MONTH&isImam=IS_IMAM&isFamily=IS_FAMILY';
var submitUrl = '/dhis-integration/submit-to-dhis';
var loadSchedulesUrl = '/dhis-integration/load-schedules';
var submitSchedulesUrl = '/dhis-integration/save-schedules';
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
			$('[data-toggle="tooltip"]').tooltip();
			
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
			
			//populate list of programs into select element
			var isYearlyReport = false;
			var canSubmitReport = hasReportingPrivilege;
			getContent(isYearlyReport, canSubmitReport).then(
						function(content) {
							console.log('[automation]');
							console.log(content.programs);
							let dropdown = $('#weekly-progname');
							dropdown.empty();
							dropdown.append('<option selected="true" disabled>Choose Program</option>');
							dropdown.prop('selectedIndex', 0);
							$.each(content.programs, function (key, entry) {
								dropdown.append($('<option></option>').attr('value', entry.name).text(entry.name));
							});
						});

			//populate list of schedules from db
			getDHISSchedules().then(function(data){
				console.log(data);
				var table = document.getElementById('weekly-program-schedules');
				data.forEach(function(object) {
					var tr = document.createElement('tr');
					tr.innerHTML ="<td>"+"<span class='custom-checkbox'>"+
									"<input type='checkbox' id='checkbox1' name='options[]' value='1'/>"+
									"<label for='checkbox1'></label>"+"</span></td>" +
									'<td>' + object.programName + '</td>' +
									'<td>' + object.lastRun + '</td>' +
									'<td>' + object.status + '</td>'+
									"<td>"+
									"<a href='#editWeeklyScheduleModal' class='edit' data-toggle='modal'><i class='material-icons' data-toggle='tooltip' title='Edit'>&#xE254;</i></a>"+
									"<a href='#deleteWeeklyScheduleModal' class='delete' data-toggle='modal'><i class='material-icons' data-toggle='tooltip' title='Delete'>&#xE872;</i></a>"+
									"</td>";
					table.appendChild(tr);
				});
			});
		


		});





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
	return $.get(loadSchedulesUrl).done(function(data) {
		//data = JSON.stringify(data);
		//console.log(data);
		
	}).fail(function(response) {
		
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
