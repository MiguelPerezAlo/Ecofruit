(function() {
    'use strict';
    angular
        .module('ecofruitApp')
        .factory('Subcategoria', Subcategoria);

    Subcategoria.$inject = ['$resource'];

    function Subcategoria ($resource) {
        var resourceUrl =  'api/subcategorias/:id';

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
