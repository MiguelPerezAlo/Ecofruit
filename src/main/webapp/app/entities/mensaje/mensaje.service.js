(function() {
    'use strict';
    angular
        .module('ecofruitApp')
        .factory('Mensaje', Mensaje);

    Mensaje.$inject = ['$resource', 'DateUtils'];

    function Mensaje ($resource, DateUtils) {
        var resourceUrl =  'api/mensajes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.fecha = DateUtils.convertDateTimeFromServer(data.fecha);
                    }
                    return data;
                }
            },
            'getMensajesEnviados':{
                method :'GET',
                isArray:true,
                url:'api/mensajesEnviados'
            },
            'update': { method:'PUT' }
        });
    }
})();
