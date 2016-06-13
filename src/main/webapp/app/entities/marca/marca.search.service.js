(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .factory('MarcaSearch', MarcaSearch);

    MarcaSearch.$inject = ['$resource'];

    function MarcaSearch($resource) {
        var resourceUrl =  'api/_search/marcas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
