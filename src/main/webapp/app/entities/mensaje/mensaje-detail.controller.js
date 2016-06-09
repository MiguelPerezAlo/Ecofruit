(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('MensajeDetailController', MensajeDetailController);

    MensajeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Mensaje', 'User'];

    function MensajeDetailController($scope, $rootScope, $stateParams, DataUtils, entity, Mensaje, User) {
        var vm = this;

        vm.mensaje = entity;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('ecofruitApp:mensajeUpdate', function(event, result) {
            vm.mensaje = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
