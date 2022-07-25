var reportConfigUrl = '/bahmni_config/openmrs/apps/reports/reports.json';
var downloadUrl = '/dhis-integration/download?name=NAME&year=YEAR&month=MONTH&isImam=IS_IMAM&isFamily=IS_FAMILY';
var submitUrl = '/dhis-integration/submit-to-dhis';
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

$(document).ready(
		function() {

			initTabs();
			// Activate tooltip
			$('[data-toggle="tooltip"]').tooltip();


			getDHISPrograms().then(function(programs) {
				var myJsonString = JSON.stringify(programs);
				alert(myJsonString[0].name);
				alert(myJsonString[1].name);
			});


			
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
