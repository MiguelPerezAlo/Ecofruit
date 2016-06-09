(function() {
    'use strict';
    angular
        .module('ecofruitApp')
        .factory('Comentario', Comentario);

    Comentario.$inject = ['$resource', 'DateUtils'];

    function Comentario ($resource, DateUtils) {
        var resourceUrl =  'api/comentarios/:id';

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
            'update': { method:'PUT' }
        });
    }
})();
