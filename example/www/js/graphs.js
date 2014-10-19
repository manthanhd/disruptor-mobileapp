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




    $('#people-link').on('singletap', function() {
        axemas.goto({'url':'www/people.html', 'stackMaintainedElements': 2});
    });

    $('#nest-link').on('singletap', function() {
        axemas.goto({'url':'www/index.html'});
    });

    $('#native-controller-push-link').on('singletap', function() {
        axemas.call('openNativeController');
    });

    $('#guardian-link').on('singletap', function() {
          axemas.goto({'url':'http://www.theguardian.com'});
    });

    $('#openmap-link').on('singletap', function() {
        axemas.call('openMap');
    });

    $('#things-link').on('singletap', function() {
        axemas.dialog('Hello!', 'What do you want from me?', ['Annulla', 'Ok'],function(data) {
            axemas.dialog('Button Pressed', data.button, ['Ok']);
        });
    });

    $('#daily').on('singletap', function(val) {
        alert(val);
    });


    $('#weekly').on('singletap', function(val) {
        alert(val);
    });


    $('#monthly').on('singletap', function(val) {
        alert( document.getElementsByTagId("monthly"));
    });

/*
    APIAccessor.login(email,password,function(response){
        if(response.code == 0){
            // do something
        }else{
            // generic error
        }
    });
*/
/*
    axemas.register("ready", function(data, callback) {
        //alert("Page Ready!");
        //callback({data: data});
    });
    */



});