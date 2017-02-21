/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var app = angular.module('app',['ngTagsInput','ngSanitize']);

var main = app.controller("main", ['$scope','$log','$http','$sanitize',function($scope,$log,$http,$sanitize){
    $scope.results = null;
    $scope.showsuccess=false;
    $scope.showfailure=false;
    $scope.show=false;
    $scope.success="";
    $scope.failure="";
    $scope.tags = [];  
    $scope.doSearch = function(){
      
      
      var req = {
        method: 'POST',
        url: 'http://100.6.103.222:8983/solr/collection1/query?fl=*,score&hl=on&hl.method=fastVector&hl.fl=content&&json.facet.content_type={type:terms,field:content_type,limit:15}'
        ,
        headers: {
          'Content-Type': 'application/json'
        },
        data:{ 'query': 'content:' + $scope.query + ''}
       };
      
      $log.info(req);
      var sendDate = (new Date()).getTime();
      $http(req).then(
              function(response)
                    { 
                        for(var name in response.data.response.docs){
                           response.data.highlighting[response.data.response.docs[name].id].score =  response.data.response.docs[name].score;
                            
                        }
                            
                        
                            
                        $log.info(response);
                        $scope.showsuccess=true;
                        $scope.showfailure=false;  
                        $scope.show = true;
                        $scope.results = response;
                        $log.info($scope.results);
                        var receiveDate = (new Date()).getTime();
                        var responseTimeMs = receiveDate - sendDate;
                        //$log.info($sanitize.trustAsHtml('Total Docs:' + response.data.total + "<br/>Response Time" + responseTimeMs + "MS"));
                        $scope.message = ('Total Docs:' + response.data.response.numFound + "<br/>Response :" + responseTimeMs + "MS");
                        $scope.tags.push({text:$scope.query});
                        
                        
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