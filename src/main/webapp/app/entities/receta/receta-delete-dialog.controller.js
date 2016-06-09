(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('RecetaDeleteController',RecetaDeleteController);

    RecetaDeleteController.$inject = ['$uibModalInstance', 'entity', 'Receta'];

    function RecetaDeleteController($uibModalInstance, entity, Receta) {
        var vm = this;

        vm.receta = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Receta.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
