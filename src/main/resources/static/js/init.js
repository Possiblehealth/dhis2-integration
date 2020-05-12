var reportConfigUrl = '/bahmni_config/openmrs/apps/reports/reports.json';
var downloadUrl = '/dhis-integration/download?name=NAME&year=YEAR&month=MONTH&isImam=IS_IMAM&isFamily=IS_FAMILY';
var submitUrl = '/dhis-integration/submit-to-dhis';
var submitDailyReportUrl = '/dhis-integration/submit-to-dhis-daily';
var submitUrlAtr = '/dhis-integration/submit-to-dhis-atr';
var loginRedirectUrl = '/bahmni/home/index.html#/login?showLoginMessage&from=';
var NUTRITION_PROGRAM = '03-2 Nutrition Acute Malnutrition';
var FAMILYPLANNING_PROGRAM = '07 Family Planning Program';
var logUrl = '/dhis-integration/log';
var fiscalYearReportUrl = '/dhis-integration/download/fiscal-year-report?name=NAME&startYear=START_YEAR&startMonth=START_MONTH&endYear=END_YEAR&endMonth=END_MONTH&isImam=IS_IMAM';
var dailyReportUrl = '/dhis-integration/download/daily-report?name=NAME&date=DATE';
var supportedStartDate = 2090;
var supportedEndDate = 2065;
var approximateNepaliYear = (new Date()).getFullYear() + 56;
var spinner = spinner || {};

var months = [ {
	number : 12,
	name : "Chaitra"
}, {
	number : 11,
	name : "Falgun"
}, {
	number : 10,
	name : "Mangh"
}, {
	number : 9,
	name : "Paush"
}, {
	number : 8,
	name : "Mangsir"
}, {
	number : 7,
	name : "Kartik"
}, {
	number : 6,
	name : "Ashwin"
}, {
	number : 5,
	name : "Bhadra"
}, {
	number : 4,
	name : "Shrawan"
}, {
	number : 3,
	name : "Ashadh"
}, {
	number : 2,
	name : "Jestha"
}, {
	number : 1,
	name : "Baisakh"
} ];

var years = range(supportedStartDate, supportedEndDate);
var fiscalYears = fiscalYearRange(supportedStartDate, supportedEndDate);
var hasReportingPrivilege = false;

$(document).ready(
		function() {
			isAuthenticated().then(isSubmitAuthorized).then(initTabs).then(
					renderPrograms).then(renderYearlyReport).then(
					selectApproxLatestNepaliYear).then(
					registerOnchangeOnComment).then(getLogStatus);
		});

function isAuthenticated() {
	return $.get("is-logged-in").then(function(response) {
		if (response != 'Logged in') {
			window.location.href = loginRedirectUrl + window.location.href;
		}
	}).fail(function(response) {
		if (response && response.status != 200) {
			window.location.href = loginRedirectUrl;
		}
	});
}

function isSubmitAuthorized() {
	return $.get("hasReportingPrivilege").then(function(response) {
		hasReportingPrivilege = response;
		if (!hasReportingPrivilege) {
			$(".submit").remove();
		}
	});
}

function initTabs() {
	$("#tabs").tabs();
}

function range(start, end) {
	return Array.apply(null, new Array(start - end + 1)).map(
			function(ignore, index) {
				return start - index;
			});
}

function fiscalYearRange(start, end) {
	return Array.apply(null, new Array(start - end + 1)).map(
			function(ignore, index) {
				return (start - index - 1) + '-' + (start - index);
			});
}

function selectApproxLatestNepaliYear() {
	var date = new Date();
	var bsDate = calendarFunctions.getBsDateByAdDate(date.getFullYear(), date
			.getMonth() + 1, date.getDate());
	if (bsDate.bsMonth == 1) {
		bsDate.bsYear = bsDate.bsYear - 1;
		bsDate.bsMonth = 12;
	} else {
		bsDate.bsMonth = bsDate.bsMonth - 1;
	}
	$('[id^="year-"]').val(bsDate.bsYear);
	$('[id^="month-"]').val(bsDate.bsMonth);

	$('[id^="fiscal-year-"]').val((bsDate.bsYear - 1) + '-' + bsDate.bsYear);
}

function renderPrograms() {
	return $.get('html/programs.html').then(
			function(template) {
				var isYearlyReport = false;
				var canSubmitReport = hasReportingPrivilege;
				return getContent(isYearlyReport, canSubmitReport).then(
						function(content) {
							$("#programs").html(
									Mustache.render(template, content));
						});
			});
}

function renderYearlyReport() {
	return $.get('html/programs.html').then(function(template) {
		var isYearlyReport = true;
		return getContent(isYearlyReport).then(function(content) {
			$("#programs-yearly").html(Mustache.render(template, content));
		});
	});
}

function getContent(isYearlyReport, canSubmitReport) {
	return getDHISPrograms().then(function(programs) {
		if (isYearlyReport) {
			return {
				years : fiscalYears,
				programs : programs,
				isYearlyReport : isYearlyReport,
				canSubmitReport : canSubmitReport
			};
		} else {
			return {
				months : months,
				years : years,
				programs : programs,
				isYearlyReport : isYearlyReport,
				canSubmitReport : canSubmitReport
			};
		}
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

function putStatus(data, index) {
	element('comment', index).html(data.comment).html();
	if (data.status == 'Success' || data.status == 'Complete') {
		var template = $('#success-status-template').html();
		Mustache.parse(template);
		element('status', index).html(Mustache.render(template, data));
		return;
	}
	var template = $('#failure-status-template').html();
	Mustache.parse(template);
	data.message = JSON.stringify(data.exception || data.response);
	element('status', index).html(Mustache.render(template, data));
	element('status', index).find('.status-failure').on('click', function() {
		alert(data.message);
		console.log(data.message);
	});
}

function download(index) {
	var year = element('year', index).val();
	var month = element('month', index).val();
	var programName = element('program-name', index).html();
	var isImam = programName.toLowerCase() === NUTRITION_PROGRAM.toLowerCase();
	var isFamily = programName.toLowerCase() === FAMILYPLANNING_PROGRAM
			.toLowerCase();
	var url = downloadUrl.replace('NAME', programName).replace('YEAR', year)
			.replace('MONTH', month).replace('IS_IMAM', isImam).replace('IS_FAMILY', isFamily);
	downloadCommon(url);
}

function downloadFiscalYearReport(index) {
	var yearRange = element('fiscal-year', index).val();
	var years = yearRange.split('-');
	var startYear = years[0];
	var startMonth = 4; //Shrawan
	var endYear = years[1];
	var endMonth = 3; //Ashadh
	var programName = element('program-name', index).html();
	var isImam = programName.toLowerCase() === NUTRITION_PROGRAM.toLowerCase();
	var url = fiscalYearReportUrl.replace('NAME', programName).replace(
			'START_YEAR', startYear).replace('START_MONTH', startMonth)
			.replace('END_YEAR', endYear).replace('END_MONTH', endMonth)
			.replace('IS_IMAM', isImam);
	downloadCommon(url);
}

function downloadDailyReport() {
	var date = $('#datepicker').val();
	//alert(date);
	var programName = 'EWARS Plus';
	var url = dailyReportUrl.replace('NAME', programName).replace(
			'DATE', date);
	downloadCommon(url);
}

function downloadCommon(url) {
	var a = document.createElement('a');
	a.href = url;
	a.target = '_blank';
	a.click();
	return false;
}


function submitDailyReport() {
	spinner.show();
	var date = $('#datepicker').val();
	var programName = 'EWARS Plus';
	var parameters = {
		name : programName,
		date : date
	};
	//alert(JSON.stringify(parameters));
	
	disableBtn(element('submitDailyReport'));
	var submitTo = submitDailyReportUrl;
	var index = 100 //TODO: hardcoded index as 1 to get element as status-100
	$.get(submitTo, parameters).done(function(data) {
		data = JSON.parse(data)
		if (!$.isEmptyObject(data)) {
			putStatus(data, index);
		}
	}).fail(function(response) {
		if (response.status == 403) {
			putStatus({
				status : 'Failure',
				exception : 'Not Authenticated'
			}, index);
		}
		putStatus({
			status : 'Failure',
			exception : response
		}, index);
	}).always(function() {
		enableBtn(element('submit', index));
		spinner.hide();
	});
}


function submit(index, attribute) {
	spinner.show();
	var year = element('year', index).val();
	var month = element('month', index).val();
	var programName = element('program-name', index).html();
	var comment = element('comment', index).val();
	var isImam = programName.toLowerCase() === NUTRITION_PROGRAM.toLowerCase();
	var isFamily = programName.toLowerCase() === FAMILYPLANNING_PROGRAM.toLowerCase();

	var parameters = {
		year : year,
		month : month,
		name : programName,
		comment : comment,
		isImam : isImam,
		isFamily : isFamily
	};

	disableBtn(element('submit', index));
	var submitTo = submitUrl;
	if (attribute == true) {
		submitTo = submitUrlAtr;
	}
	$.get(submitTo, parameters).done(function(data) {
		data = JSON.parse(data)
		if (!$.isEmptyObject(data)) {
			putStatus(data, index);
		}
	}).fail(function(response) {
		if (response.status == 403) {
			putStatus({
				status : 'Failure',
				exception : 'Not Authenticated'
			}, index);
		}
		putStatus({
			status : 'Failure',
			exception : response
		}, index);
	}).always(function() {
		enableBtn(element('submit', index));
		spinner.hide();
	});
}

function confirmAndSubmit(index, attribute) {
	if (confirm("This action cannot be reversed. Are you sure, you want to submit?")) {
		submit(index, attribute);
	}
	
}

function confirmAndSubmitDailyReport() {
	if (confirm("This action cannot be reversed. Are you sure, you want to submit?")) {
		submitDailyReport();
	}
}


function getStatus(index) {
	var programName = element('program-name', index).html();
	var year = element('year', index).val();
	var month = element('month', index).val();
	var date = $('#datepicker').val();
    //console.log("getting log for "+ date);
    if(year){
    	date="";
    }
    if (date){
		programName = 'EWARS Plus';
		month = "";
		year = "";
	}
	var parameters = {
		programName : programName,
		month : month,
		year : year,
		date : date
	};
	
	
	spinner.show();
	$.get(logUrl, parameters).done(function(data) {
		data = JSON.parse(data);
		//console.log("logs :: "+data);
		if ($.isEmptyObject(data)) {
			element('comment', index).html('');
			element('status', index).html('');
		} else {
			putStatus(data, index);
		}
	}).fail(function(response) {
		console.log("failure response");
		if (response.status == 403) {
			putStatus({
				status : 'Failure',
				exception : 'Not Authenticated'
			}, index);
		}
		putStatus({
			status : 'Failure',
			exception : response
		}, index);
	}).always(function() {
		spinner.hide();
	})
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
