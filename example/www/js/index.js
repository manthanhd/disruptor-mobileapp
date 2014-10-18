$(function() {
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

    axemas.register("ready", function(data, callback) {
        //alert("Page Ready!");
        callback({data: data});
    });
});