(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .factory('ComentarioSearch', ComentarioSearch);

    ComentarioSearch.$inject = ['$resource'];

    function ComentarioSearch($resource) {
        var resourceUrl =  'api/_search/comentarios/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
