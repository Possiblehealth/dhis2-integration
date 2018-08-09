var reportConfigUrl = '/bahmni_config/openmrs/apps/reports/reports.json';
var downloadUrl = '/dhis-integration/download?name=NAME&year=YEAR&month=MONTH&isImam=IS_IMAM';
var submitUrl = '/dhis-integration/submit-to-dhis';
var submitUrlAtr = '/dhis-integration/submit-to-dhis-atr';
var loginRedirectUrl = '/bahmni/home/index.html#/login?showLoginMessage&from=';
var NUTRITION_PROGRAM = '03-2 Nutrition Acute Malnutrition';
var logUrl = '/dhis-integration/log';
var supportedStartDate = 2090;
var supportedEndDate = 2065;
var approximateNepaliYear = (new Date()).getFullYear() + 56;

var months = [
    {number: 12, name: "Chaitra"},
    {number: 11, name: "Falgun"},
    {number: 10, name: "Mangh"},
    {number: 9, name: "Paush"},
    {number: 8, name: "Mangsir"},
    {number: 7, name: "Kartik"},
    {number: 6, name: "Ashwin"},
    {number: 5, name: "Bhadra"},
    {number: 4, name: "Shrawan"},
    {number: 3, name: "Ashadh"},
    {number: 2, name: "Jestha"},
    {number: 1, name: "Baisakh"}
];

var years = range(supportedStartDate, supportedEndDate);

$(document).ready(function () {
    isAuthenticated()
        .then(renderPrograms)
        .then(selectApproxLatestNepaliYear)
        .then(registerOnchangeOnComment)
        .then(getLogStatus);
    
});

function isAuthenticated() {
    return $.get("is-logged-in").then(function(response){
        if(response!='Logged in'){
            window.location.href = loginRedirectUrl + window.location.href;
        }
    }).fail(function(response){
        if(response && response.status != 200){
            window.location.href = loginRedirectUrl;
        }
    });
}

function range(start, end) {
    return Array.apply(null, new Array(start - end + 1)).map(function (ignore, index) {
        return start - index;
    });
}

function selectApproxLatestNepaliYear() {
    $('[id^="year-"]').val(approximateNepaliYear);
}

function renderPrograms() {
    return $.get('html/programs.html').then(function (template) {
        return getContent().then(function (content) {
            $("#programs").html(Mustache.render(template, content));
        });
    });
}

function getContent() {
    return getDHISPrograms().then(function (programs) {
        return {months: months, years: years, programs: programs};
    });
}

function getDHISPrograms() {
    return $.getJSON(reportConfigUrl).then(function (reportConfigs) {
        var DHISPrograms = [];
        Object.keys(reportConfigs).forEach(function (reportKey) {
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
    element('status', index).find('.status-failure').on('click', function(){
        alert(data.message);
        console.log(data.message);
    });
}

function download(index) {
    var year = element('year', index).val();
    var month = element('month', index).val();
    var programName = element('program-name', index).html();
    var isImam = programName.toLowerCase() === NUTRITION_PROGRAM.toLowerCase();
    var url = downloadUrl.replace('NAME', programName).replace('YEAR', year).replace('MONTH', month).replace('IS_IMAM', isImam);
    var a = document.createElement('a');
    a.href = url;
    a.target = '_blank';
    a.click();
    return false;
}

function submit(index, attribute) {
    var year = element('year', index).val();
    var month = element('month', index).val();
    var programName = element('program-name', index).html();
    var comment = element('comment', index).val();
    var isImam = programName.toLowerCase() === NUTRITION_PROGRAM.toLowerCase();
    var parameters = {
        year: year,
        month: month,
        name: programName,
        comment: comment,
        isImam: isImam
    };

    disableBtn(element('submit', index));
    var submitTo = submitUrl;
    if(attribute == true){
    	submitTo = submitUrlAtr;
    }
    $.get(submitTo, parameters).done(function (data) {
    	data = JSON.parse(data)
    	if (!$.isEmptyObject(data)) {
    		putStatus(data, index);
    	}
    }).fail(function (response) {
        if(response.status == 403){
            putStatus({status:'Failure', exception: 'Not Authenticated'}, index);
        }
        putStatus({status:'Failure', exception: response}, index);
    }).always(function () {
        enableBtn(element('submit', index));
    });
}

function confirmAndSubmit(index, attribute) {
    if (confirm("This action cannot be reversed. Are you sure, you want to submit?")) {
        submit(index, attribute);
    }
}

function getStatus(index) {
	var programName = element('program-name', index).html();
    var year = element('year', index).val();
    var month = element('month', index).val();
	
	var parameters = {
		programName: programName, 
		month: month,
		year: year
	};
	
	 $.get(logUrl, parameters).done(function (data) {
		 	data = JSON.parse(data);
			 if ($.isEmptyObject(data)) {
				 console.log(element)
				 element('comment', index).html('');
				 element('status', index).html('');
			 } else {
				 putStatus(data, index);
			 }
	    }).fail(function (response) {
	    	 console.log("failure response");
	        if(response.status == 403){
	            putStatus({status:'Failure', exception: 'Not Authenticated'}, index);
	        }
	        putStatus({status:'Failure', exception: response}, index);
	    });
}

function element(name,index){
    var id = name +'-' + index;
    return $('[id="'+id+'"]');
}

function enableBtn(btn){
    return btn.attr('disabled', false).removeClass('btn-disabled');
}

function disableBtn(btn){
    return btn.attr('disabled', true).addClass('btn-disabled');
}

function disableAllSubmitBtns(){
    disableBtn($("[id*='submit-']"));
}

function registerOnchangeOnComment(){
    disableAllSubmitBtns();
    $("[id*='comment-']").on('change keyup paste',function(event){
        var index = $(event.target).attr('index');
        if($(event.target).val().trim()!=""){
            enableBtn(element('submit',index));
        } else {
            disableBtn(element('submit',index));
        }
    });
}

function getLogStatus(){
	$('.month-selector').each(function(index) {
		getStatus(index);
   });
}