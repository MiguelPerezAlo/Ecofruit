(function() {
    'use strict';
    angular
        .module('ecofruitApp')
        .factory('Marca', Marca);

    Marca.$inject = ['$resource'];

    function Marca ($resource) {
        var resourceUrl =  'api/marcas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
