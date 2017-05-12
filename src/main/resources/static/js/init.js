var reportConfigUrl = '/bahmni_config/openmrs/apps/reports/reports.json';
var downloadUrl = '/dhis-integration/download?name=NAME&year=YEAR&month=MONTH';
var submitUrl = '/dhis-integration/submit-to-dhis?name=NAME&year=YEAR&month=MONTH';
var loginRedirectUrl = '/bahmni/home/index.html#/login?showLoginMessage&from=';
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
        .then(selectApproxLatestNepaliYear);
});

function isAuthenticated() {
    return $.get("is-logged-in").then(function(status){
        if(status!='Logged in'){
            window.location.href = loginRedirectUrl + window.location.href;
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
function download(index) {
    var year = $('[id="year-'+index+'"]').val();
    var month = $('[id="month-'+index+'"]').val();
    var programName = $('[id="program-name-'+index+'"]').html();
    var url = downloadUrl.replace('NAME', programName).replace('YEAR', year).replace('MONTH', month);
    var a = document.createElement('a');
    a.href = url;
    a.target = '_blank';
    a.click();
    return false;
}
function submit(index) {
    var year = element('year',index).val();
    var month = element('month',index).val();
    var programName = element('program-name',index).html();
    var url = submitUrl.replace('NAME', programName).replace('YEAR', year).replace('MONTH', month);

    element('submit',index)
        .attr('disabled',true)
        .addClass('btn-disabled');
    $.get(url).done(function(data){
        console.log(data);
    }).fail(function(data){
        console.log(data.responseText);
    }).always(function(){
        element('submit',index)
            .attr('disabled',false)
            .removeClass('btn-disabled');
    });
}
function confirmAndSubmit(index) {
    if (confirm("This action cannot be reversed. Are you sure, you want to submit?")) {
        submit(index);
    }
}
function element(name,index){
    var id = name +'-' + index;
    return $('[id="'+id+'"]');
}