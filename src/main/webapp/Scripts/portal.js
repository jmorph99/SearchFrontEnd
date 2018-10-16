/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var app = angular.module('app',['ngTagsInput','ngSanitize']);
/*
$http.get('/my-url-that-does-not-exist')
.then(function (result0) {
console.log('1');
}, function (result0) {
console.log('2');
})
.then(function (result1) {
console.log('A')
}, function (result1) {
console.log('B')
});
*/
var main = app.controller("main", ['$scope','$log','$http','$sanitize',function($scope,$log,$http,$sanitize){
    $scope.results = null;
    $scope.showsuccess=false;
    $scope.showfailure=false;
    $scope.show=false;
    $scope.success="";
    $scope.failure="";
    
    $scope.doSearch = function(){
      
      
      var req = {
        method: 'POST',
        url: 'http://localhost:9200/blogs/_search'
        ,
        headers: {
          'Content-Type': 'application/json',
          'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept, Authorization',
          'Access-Control-Allow-Origin':'*',
          'Access-Control-Allow-Methods': 'GET,HEAD,OPTIONS,POST,PUT'
          
        },
        data:{ "query": "{\"match_phrase\": {\"content\": \"elastic stack\"}},highlight\": {\"fields\": {\"title\" : {},\"content\" : {}},\"require_field_match\": true,\"pre_tags\": [\"<b>\"],\"post_tags\": [\"</b>\"]}" }
        //data:{ "query": "{\"match_all\": {}}" }
       };
      
      $log.info(req);
      var sendDate = (new Date()).getTime();
      $http(req).then(
              function(response)
                    { 
                        //a = 0;
                        //for(var name in response.data.hits.hit){
                        //   response.data.highlighting[response.data.response.docs[name].id].score =  response.data.response.docs[name].score;
                        //    
                        //}
                            
                        
                            
                        $log.info(response);
                        $scope.showsuccess=true;
                        $scope.showfailure=false;  
                        $scope.show = true;
                        $scope.results = response;
                        $log.info($scope.results);
                        var receiveDate = (new Date()).getTime();
                        var responseTimeMs = receiveDate - sendDate;
                        //$log.info($sanitize.trustAsHtml('Total Docs:' + response.data.total + "<br/>Response Time" + responseTimeMs + "MS"));
                        $scope.message = ('Total Docs:' + $scope.results.data.hits.total + "<br/>Response :" + responseTimeMs + "MS");
                        //$scope.tags.push({text:$scope.query});
                        
                        
                  }, 
              function(response)
                    {   
                        $log.error(response);
                        $scope.showsuccess=false;
                        $scope.showfailure=true; 
                        $scope.show = true;
                        $scope.results = null;
                        var receiveDate = (new Date()).getTime();
                        var responseTimeMs = receiveDate - sendDate;
                        $scope.message = ('Unable to parse query "' + $scope.query + '" <br/>Response Time' + responseTimeMs + 'MS');
                    }
              );
      
    };
    
}]);