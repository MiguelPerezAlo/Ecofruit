(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .factory('RecetaSearch', RecetaSearch);

    RecetaSearch.$inject = ['$resource'];

    function RecetaSearch($resource) {
        var resourceUrl =  'api/_search/recetas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
