(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('MarcaDetailController', MarcaDetailController);

    MarcaDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Marca', 'Producto'];

    function MarcaDetailController($scope, $rootScope, $stateParams, DataUtils, entity, Marca, Producto) {
        var vm = this;

        vm.marca = entity;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('ecofruitApp:marcaUpdate', function(event, result) {
            vm.marca = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
