; (function(win, undefined) {
    "use strict";
   
    var APIAccessor = win['APIAccessor'] = (win['APIAccessor'] || {} );

    APIAccessor.BASE_URL = "";
    APIAccessor.BASE_TIMEOUT = 15000;
    APIAccessor.LONG_POLL_TIMEOUT = 40000;

    var emptyStorageModel = {idToken : "",
                             journey : "",
                             journeyType : "",
                             QRCode : "",
                             latitude : "",
                             longitude : "",
                             canRescheduleLongPoll : false,
                             isLongPollRunning : false};

    APIAccessor.saveApplicationStorage = function(value){
        axemas.storeData("APPLICAION___STORAGE",JSON.stringify(value));
        if(axemas.getPlatform() != 'ios'){
            axemas.call('applicationStorageChanged',value); //only used in android for now to sync with the local storage
        }
    }

    APIAccessor.clearApplicationStorage = function(){
        axemas.removeData("APPLICAION___STORAGE");
        axemas.storeData("APPLICAION___STORAGE",JSON.stringify(emptyStorageModel));
        if(axemas.getPlatform() != 'ios'){
            axemas.call('applicationStorageChanged',emptyStorageModel); //only used in android for now to sync with the local storage
        }
    }

    APIAccessor.getApplicationStorage = function(dataFoundCallback){
        var result = "";
        axemas.fetchData("APPLICAION___STORAGE",function(res){
            result = res;
        });
        if(result == null){
            dataFoundCallback(emptyStorageModel);
        }else{
            var parsedResult = JSON.parse(result);
            if(axemas.getPlatform() == 'ios'){
                dataFoundCallback(parsedResult);
            }else{
                axemas.call("getStorageFromAndroid","",function(response){
                    if(response["idToken"] != parsedResult.idToken){
                        APIAccessor.saveApplicationStorage(parsedResult);
                        dataFoundCallback(parsedResult);
                    }else{
                        APIAccessor.saveApplicationStorage(response);
                        dataFoundCallback(response);
                    }
                });
            }
        }
    }



    APIAccessor._ajax = function(api,timeout,showErrors,resultCallback) {
        console.log("CALLING: "+ APIAccessor.BASE_URL+api);
        $.ajaxSetup({
            timeout: timeout
        });
        $.getJSON(APIAccessor.BASE_URL+api)
            .error(function(result) {
                console.log("RESULT '"+api+"': "+JSON.stringify(result));
                if(showErrors){
                    if(result.status == "0"){
                        axemas.alert("Errore","Impossibile contattare il server");
                    }else if(result.responseJSON.code == -1 && result.responseJSON.errors != "undefined" &&
                            result.responseJSON.errors.id_token != "undefined" &&
                            result.responseJSON.errors.id_token == "You have provided an invalid 'id_token'"){
                        //APIAccessor.clearApplicationStorage(); // do not clear it yet!!
                   if (axemas.getPlatform() == 'ios') {
                            axemas.replace({"url":"www/login.html?message=invalid_token","title":"LOGIN"});
                            //axemas.alert("Errore", "La sessione è scaduta, per favore rilogga.");
                            //
                            resultCallback(result.responseJSON);
                        }else{
                            axemas.call("invalidTokenOperations");
                            axemas.gotoFromSidebar({"url":"www/login.html?message=invalid_token","title":"LOGIN","stackMaintainedElements": 0});
                        }
                    }else{
                        resultCallback(result.responseJSON);
                    }
                }else{
                    resultCallback(result.responseJSON);
                }
            })
            .success(function(result) { 
                console.log("RESULT '"+api+"': "+JSON.stringify(result));
                resultCallback(result);
            });
    }
   
    APIAccessor.login = function(email,password,resultCallback) {
        APIAccessor._ajax("login?email="+email+"&password="+password,APIAccessor.BASE_TIMEOUT,true,function(res){
            resultCallback(res);
        });
    }

})(window);