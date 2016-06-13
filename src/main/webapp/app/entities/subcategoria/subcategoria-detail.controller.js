(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('SubcategoriaDetailController', SubcategoriaDetailController);

    SubcategoriaDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Subcategoria', 'Producto', 'Categoria'];

    function SubcategoriaDetailController($scope, $rootScope, $stateParams, DataUtils, entity, Subcategoria, Producto, Categoria) {
        var vm = this;

        vm.subcategoria = entity;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('ecofruitApp:subcategoriaUpdate', function(event, result) {
            vm.subcategoria = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
