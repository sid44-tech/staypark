 function openSupport() {
	 window.open('http://www.websitealive3.com/10167/rRouter.asp?groupid=10167&amp;websiteid=0&amp;departmentid=8759&amp;dl='+escape(document.location.href),'','width=400,height=400');
 }
 
 function openHome() {
	 location.href = "/";
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

 function tomorrow() {
     var today = new Date();
     today.setDate(today.getDate() + 1);
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
         clearBtn: true
     })
     pickup_date_input.datepicker("setDate", date);
 }

 function updateMinValues(datefield1, datefield2, datefield3, datefield4) {
     datefield1.datepicker("setStartDate", new Date());
     datefield2.datepicker("setStartDate", addDaysToDate(new Date(), 1));
     datefield3.datepicker("setStartDate", addDaysToDate(new Date(), 2));
     datefield4.datepicker("setStartDate", addDaysToDate(new Date(), 3));
 }

 function linkDateFields(datefield1, datefield2, datefield3, datefield4) {
     datefield1.on('changeDate', function() {
         var indate = datefield1.datepicker("getDate");
         var outdate = datefield2.datepicker("getDate");

         if (indate > outdate) {
             indate.setDate(indate.getDate() + 1);
             datefield2.datepicker("setDate", indate);
         }
     });

     datefield2.on('changeDate', function() {
         var indate = datefield1.datepicker("getDate");
         var outdate = datefield2.datepicker("getDate");

         if (outdate < indate) {
             indate.setDate(outdate.getDate() - 1);
             datefield1.datepicker("setDate", indate);
         }
     });

     datefield3.on('changeDate', function() {
         var indate = datefield3.datepicker("getDate");
         var outdate = datefield4.datepicker("getDate");

         if (indate > outdate) {
             indate.setDate(indate.getDate() + 1);
             datefield4.datepicker("setDate", indate);
         }
     });

     datefield4.on('changeDate', function() {
         var indate = datefield3.datepicker("getDate");
         var outdate = datefield4.datepicker("getDate");

         if (outdate < indate) {
             indate.setDate(outdate.getDate() - 1);
             datefield3.datepicker("setDate", indate);
         }
     });
 }