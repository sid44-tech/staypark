 function openSupport() {
	 window.open('http://www.websitealive3.com/10167/rRouter.asp?groupid=10167&amp;websiteid=0&amp;departmentid=8759&amp;dl='+escape(document.location.href),'','width=400,height=400');
 }
 
 function openHome() {
	 location.href = "/";
 }

 function dateToStringMDY(date) {
     var dd = date.getDate();
     var mm = date.getMonth()+1; //January is 0!

     var yyyy = date.getFullYear();
     if(dd<10){
         dd='0'+dd;
     }
     if(mm<10){paparrd
         mm='0'+mm;
     }
     var dateStr = mm+'/'+dd+'/'+yyyy;
     return dateStr;
 }

 function todayString() {
     var today = new Date();
     var dd = today.getDate();
     var mm = today.getMonth()+1; //January is 0!

     var yyyy = today.getFullYear();
     if(dd<10){
         dd='0'+dd;
     }
     if(mm<10){
         mm='0'+mm;
     }
     var today = mm+'/'+dd+'/'+yyyy;
     return today;
 }

 function tomorrowString() {
     var today = new Date();
     today.setDate(today.getDate() + 1);
     var dd = today.getDate();
     var mm = today.getMonth()+1; //January is 0!

     var yyyy = today.getFullYear();
     if(dd<10){
         dd='0'+dd;
     }
     if(mm<10){
         mm='0'+mm;
     }
     var today = mm+'/'+dd+'/'+yyyy;
     return today;
 }

 function dayAfterTomorrowString() {
     var today = new Date();
     today.setDate(today.getDate() + 2);
     var dd = today.getDate();
     var mm = today.getMonth()+1; //January is 0!

     var yyyy = today.getFullYear();
     if(dd<10){
         dd='0'+dd;
     }
     if(mm<10){
         mm='0'+mm;
     }
     var today = mm+'/'+dd+'/'+yyyy;
     return today;
 }

 function tomorrow() {
     var today = new Date();
     today.setDate(today.getDate() + 1);
     return today;
 }

 function dayAfterTomorrow() {
     var today = new Date();
     today.setDate(today.getDate() + 2);
     return today;
 }

 function addDaysToDate(date, days) {
     date.setDate(date.getDate() + days);
     return date;
 }

 function initDatePicker(pickup_date_input, date) {
     pickup_date_input.datepicker({
         format: 'mm/dd/yyyy',
         todayHighlight: true,
         autoclose: true,
         disableTouchKeyboard: true,
         clearBtn: true,
         enableOnReadonly: false
     })
     pickup_date_input.datepicker("setDate", date);
 }

 function daysDifference(first, second) {
     return Math.round((second-first)/(1000*60*60*24));
 }

 function convertCurrentTimeToSelectIndex() {
     var d = new Date();
     var h = d.getHours();
     var m = d.getMinutes();

     var index = Math.floor(m/15);
     if (h != 0) {
         index = (h*4) + Math.floor(m/15);
     }
     if (index < 92) {
         index += 4;
     } else {
         index = 4;
         var dropoff_date_input=$('input[name="drop_off_date"]'); //our date input has the name "date"
         dropoff_date_input.val(tomorrowString());
         dropoff_date_input.datepicker("setDate", tomorrow());

         var pickup_date_input=$('input[name="pick_up_date"]'); //our date input has the name "date"
         pickup_date_input.val(dayAfterTomorrowString());
         pickup_date_input.datepicker("setDate", dayAfterTomorrow());
     }

     return index;
 }

 function validatePassword(password) {
     if (password.length < 8 || !containsDigit(password)) {
         return false;
     }
     return true;
 }

 function validateConfirmPassword(password, confirm) {
     if (password != confirm) {
         return false;
     }

     return true;
 }

 function containsDigit(s) {
     var containsDigit = false;
     var matches = s.match(/\d+/g);
     if (matches != null) {
         containsDigit = true;
     }
     return containsDigit;
 }

 function appendRequestParameter(str, name, value) {
    var str = str + name + "=" + value + "&";
    return str;
 }

 function parse_query_string(query) {
     var vars = query.split("&");
     var query_string = {};
     for (var i = 0; i < vars.length; i++) {
         var pair = vars[i].split("=");
         var key = decodeURIComponent(pair[0]);
         var value = decodeURIComponent(pair[1]);
         // If first entry with this name
         if (typeof query_string[key] === "undefined") {
             query_string[key] = decodeURIComponent(value);
             // If second entry with this name
         } else if (typeof query_string[key] === "string") {
             var arr = [query_string[key], decodeURIComponent(value)];
             query_string[key] = arr;
             // If third or later entry with this name
         } else {
             query_string[key].push(decodeURIComponent(value));
         }
     }
     return query_string;
 }