(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .factory('MensajeSearch', MensajeSearch);

    MensajeSearch.$inject = ['$resource'];

    function MensajeSearch($resource) {
        var resourceUrl =  'api/_search/mensajes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
