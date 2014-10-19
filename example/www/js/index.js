$(function() {
        var datum = [{
        'title':'Tomatoes',
        'alert_class_one': 'alert-success',
        'message_alert_one': 'adequate',
        'alert_class_two': 'alert-warning',
        'message_alert_two': 'low',
        '_id':'farm_id'
        },{
                  'title':'Aubergines',
                  'alert_class_one': 'alert-danger',
                  'message_alert_one': 'too low',
                  'alert_class_two': 'alert-warning',
                  'message_alert_two': 'low',
                  '_id':'farm_id'
                  },{
                                     'title':'Cabbage',
                                     'alert_class_one': 'alert-success',
                                     'message_alert_one': 'adequate',
                                     'alert_class_two': 'alert-success',
                                     'message_alert_two': 'normal',
                                     '_id':'farm_id'
                                     }];

       var ractive = new Ractive({
            el: "container",
            template: "#template",
            data: { items: datum }
        });


    $('#daily').on('singletap', function(val) {
        axemas.goto({"url":"www/graphs.html"});
        //document.getElementById("daily").getAttribute("value")
    });


    $('#weekly').on('singletap', function(val) {
        axemas.goto({"url":"www/graphs.html"});
        //document.getElementById("weekly").getAttribute("value")
    });


    $('#monthly').on('singletap', function(val) {
        axemas.goto({"url":"www/graphs.html"});
        //document.getElementById("monthly").getAttribute("value")
    });




});