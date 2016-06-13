(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('CategoriaDetailController', CategoriaDetailController);

    CategoriaDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Categoria', 'Subcategoria'];

    function CategoriaDetailController($scope, $rootScope, $stateParams, DataUtils, entity, Categoria, Subcategoria) {
        var vm = this;

        vm.categoria = entity;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('ecofruitApp:categoriaUpdate', function(event, result) {
            vm.categoria = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
