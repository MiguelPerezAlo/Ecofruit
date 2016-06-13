(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .controller('MarcaDeleteController',MarcaDeleteController);

    MarcaDeleteController.$inject = ['$uibModalInstance', 'entity', 'Marca'];

    function MarcaDeleteController($uibModalInstance, entity, Marca) {
        var vm = this;

        vm.marca = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Marca.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
